package com.cookietracker.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.pattern._
import akka.routing.BalancingPool
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * This class is an overall supervisor of all the modules of a web crawler.
  * It is responsible for reacting to module failures.
  */
object WebCrawler {
  def props = Props(new WebCrawler)
}

class WebCrawler extends Actor with ActorLogging {
  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher

  val httpFetchers: Seq[ActorRef] = List.fill(5)(context.actorOf(HttpFetcher.props))
//  lazy val dnsResolver: ActorRef = context.actorOf(DnsResolver.props, "dns-resolver")
val linkExtractors: ActorRef = context.actorOf(BalancingPool(5).props(LinkExtractor.props), "link-extractor-router")
  val urlFrontier: ActorRef = context.actorOf(UrlFrontier.props, "url-frontier")
  val urlFilter: ActorRef = context.actorOf(UrlFilter.props, "url-filter")
  val urlDeduplicator: ActorRef = context.actorOf(UrlDeduplicator.props, "url-deduplicator")

  override def receive: Receive = {
    /** When receive a URL from UrlFrontier, we send it to DnsResolver
      * Here we don't pass the URL to dns resolver, because Akka HTTP will deal with
      * the DNS cache for us.
      */
    case GimmeWork =>
      val fetcher = sender()
      implicit val timeout = Timeout(3 seconds)
      urlFrontier ? Dequeue onSuccess {
        case DequeueResult(url) =>
          Try(Fetch(url, HttpRequest(uri = Uri(url.toExternalForm)))) match {
            case Success(v) => fetcher ! v
            case Failure(t) =>
              log.error("error when creating HTTP request", t)
              self.tell(GimmeWork, fetcher)
          }
        case EmptyOrBusyQueue =>
          log.info("Empty or busy queue, will retry in 1 second")
          context.system.scheduler.scheduleOnce(1 second)(self.tell(GimmeWork, fetcher))(contextExecutor)
      }
    //    case DequeueResult(url) =>
    //      httpFetchers ! Fetch(url, HttpRequest(uri = Uri(url)))
    // Having the fetch result, we extract links from it
    case FetchResult(url, response) =>
      linkExtractors ! ExtractLink(url, response.entity)
    case ExtractResult(url, links) =>
      urlFilter ! FilterUrl(url, links)
    case FilterResult(baseUrl, urls) =>
      urlDeduplicator ! Deduplicate(baseUrl, urls)
    case DeduplicateResult(baseUrl, urls) =>
      urlFrontier ! Enqueue(urls)
    case Start => httpFetchers.foreach(_ ! WorkAvailable)
    case x => log.warning(s"Unknown message $x")
  }
}
