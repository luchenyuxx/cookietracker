package com.cookietracker.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, Uri, headers}
import akka.pattern._
import akka.routing.BalancingPool
import akka.util.Timeout
import com.cookietracker.crawler.CookieManager.{GetCookie, GetCookieResult, RecordCookie}
import com.cookietracker.crawler.HtmlResourceFetcher.FetchResource
import com.cookietracker.crawler.HttpFetcher.{Fetch, FetchResult}
import com.cookietracker.crawler.LinkExtractor.{ExtractLink, ExtractResult}
import com.cookietracker.crawler.UrlDeduplicator.{Deduplicate, DeduplicateResult}
import com.cookietracker.crawler.UrlFilter.{FilterResult, FilterUrl}
import com.cookietracker.crawler.UrlFrontier.{Dequeue, DequeueResult, EmptyOrBusyQueue, Enqueue}

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
  val linkExtractors: ActorRef = context.actorOf(BalancingPool(5).props(LinkExtractor.props), "link-extractor-router")
  val urlFrontier: ActorRef = context.actorOf(UrlFrontier.props, "url-frontier")
  val urlFilter: ActorRef = context.actorOf(UrlFilter.props, "url-filter")
  val urlDeduplicator: ActorRef = context.actorOf(UrlDeduplicator.props, "url-deduplicator")
  val cookieManager: ActorRef = context.actorOf(CookieManager.props, "cookie-recorder")
  val htmlResourceFetcher: ActorRef = context.actorOf(HtmlResourceFetcher.props, "html-resource-fetcher")

  override def receive: Receive = {
    /** When receive a URL from UrlFrontier, we send it to DnsResolver
      * Here we don't pass the URL to dns resolver, because Akka HTTP will deal with
      * the DNS cache for us.
      */
    case GimmeWork =>
      val fetcher = sender()
      implicit val timeout = Timeout(3.seconds)
      urlFrontier ? Dequeue onSuccess {
        case DequeueResult(url) =>
          cookieManager ? GetCookie(url) onSuccess {
            case GetCookieResult(_, cookies) =>
              Try {
                val userAgentHeader: HttpHeader = headers.`User-Agent`("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                val cookieHeader: HttpHeader = headers.Cookie(cookies.map(_.pair()).toIndexedSeq)
                val request = HttpRequest(uri = Uri(url.toExternalForm), headers = List(userAgentHeader, cookieHeader))
                Fetch(url, request)
              } match {
                case Success(v) => fetcher ! v
                case Failure(t) =>
                  log.error("error when creating HTTP request", t)
                  self.tell(GimmeWork, fetcher)
              }
          }
        case EmptyOrBusyQueue =>
          log.info("Empty or busy queue, will retry in 1 second")
          context.system.scheduler.scheduleOnce(1.second)(self.tell(GimmeWork, fetcher))(contextExecutor)
      }
    //    case DequeueResult(url) =>
    //      httpFetchers ! Fetch(url, HttpRequest(uri = Uri(url)))
    // Having the fetch result, we extract links from it
    case FetchResult(url, response) =>
      val cookies = response.headers.filter {
        case _: headers.`Set-Cookie` => true
        case _ => false
      }.asInstanceOf[Seq[headers.`Set-Cookie`]].map(_.cookie)
      if (cookies.nonEmpty) {
        cookieManager ! RecordCookie(url, cookies)
      }
      linkExtractors ! ExtractLink(url, response.entity)
    case ExtractResult(url, links) =>
      htmlResourceFetcher ! FetchResource(url, links.srcLinks)
      urlFilter ! FilterUrl(url, links.hrefLinks)
    case FilterResult(baseUrl, urls) =>
      urlDeduplicator ! Deduplicate(baseUrl, urls)
    case DeduplicateResult(_, urls) =>
      urlFrontier ! Enqueue(urls)
    case Start => httpFetchers.foreach(_ ! WorkAvailable)
    case x => log.warning(s"Unknown message $x")
  }
}
