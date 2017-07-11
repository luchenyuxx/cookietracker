package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.routing.BalancingPool

/**
  * This class is an overall supervisor of all the modules of a web crawler.
  * It is responsible for reacting to module failures.
  */
object WebCrawler {
  def props = Props(new WebCrawler)
}

class WebCrawler extends Actor with ActorLogging{
  implicit val contextExecutor = context.dispatcher

  lazy val httpFetchers: ActorRef = context.actorOf(BalancingPool(5).props(HttpFetcher.props), "http-fetcher-router")
  lazy val dnsResolver: ActorRef = context.actorOf(DnsResolver.props, "dns-resolver")
  lazy val linkExtractors: ActorRef = context.actorOf(BalancingPool(5).props(LinkExtractor.props), "link-extractor-router")
  lazy val urlFrontier: ActorRef = context.actorOf(UrlFrontier.props, "url-frontier")
  lazy val urlFilter: ActorRef = context.actorOf(UrlFilter.props, "url-filter")
  lazy val urlDeduplicator: ActorRef = context.actorOf(UrlDeduplicator.props, "url-deduplicator")

  override def receive: Receive = {
    // When receive a URL from UrlFrontier, we send it to DnsResolver
    case DequeueResult(url) =>
      dnsResolver ! DnsResolve(url)
    // After DNS is resolved, we fetch the URL
    case DnsResolved(url, address) =>
      val newUrl = address match {
        case Some(ip) => new URL(url.getProtocol, ip.getHostAddress, url.getPort, url.getFile)
        case None => url
      }
      httpFetchers ! Fetch(url, HttpRequest(uri = Uri(newUrl)))
    // Having the fetch result, we extract links from it and get a new URL to fetch
    case FetchResult(url, responseFuture) =>
      linkExtractors ! ExtractLink(url, responseFuture.map(_.entity))
    // Enqueue the extracted links to UrlFrontier
    case ExtractResult(url, links) =>
      urlFrontier ! Enqueue(links.toList)
    case x => log.warning(s"Unknown message $x")
  }
}
