package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model._
import akka.pattern._
import akka.routing.BalancingPool
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.cookietracker.crawler.CookieManager.{GetCookie, GetCookieResult, RecordCookie}
import com.cookietracker.crawler.HttpFetcher.{Fetch, FetchFailure, FetchResult}
import com.cookietracker.crawler.LinkExtractor.{ExtractLink, ExtractResult}
import com.cookietracker.crawler.RelationManager.RecordRelation
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

class WebCrawler extends Actor with ActorReporting with ActorLogging {
  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher

  val httpFetcher: ActorRef = context.actorOf(HttpFetcher.props)
  val linkExtractors: ActorRef = context.actorOf(BalancingPool(5).props(LinkExtractor.props), "link-extractor-router")
  val urlFrontier: ActorRef = context.actorOf(UrlFrontier.props, "url-frontier")
  val urlFilter: ActorRef = context.actorOf(UrlFilter.props, "url-filter")
  val urlDeduplicator: ActorRef = context.actorOf(UrlDeduplicator.props, "url-deduplicator")
  val cookieManager: ActorRef = context.actorOf(CookieManager.props, "cookie-recorder")
  val relationManager: ActorRef = context.actorOf(RelationManager.props, "relation-manager")

  var pressure = 0
  var urlCount: Long = 0
  val pressureThreshold = 200

  override def monitoredReceive: Receive = {
    case DequeueResult(url) =>
      implicit val timeout = Timeout(3.seconds)
      cookieManager ? GetCookie(url) onSuccess {
        case GetCookieResult(_, cookies) =>
          Try(buildRequest(url, cookies)) match {
            case Success(v) =>
              httpFetcher ! Fetch(url, v)
              increasePressure()
              urlCount += 1
            case Failure(t) =>
              log.error("error when creating HTTP request: " + t.getCause)
          }
      }
      continue()
    case EmptyOrBusyQueue =>
      log.info("URL queue is empty, retry in 1 second.")
      context.system.scheduler.scheduleOnce(1.second, urlFrontier, Dequeue)
    // Having the fetch result, we extract links from it
    case FetchResult(url, response) =>
      decreasePressure()
      if (response.status.equals(StatusCodes.OK)) {
        val cookies = response.headers.filter {
          case _: headers.`Set-Cookie` => true
          case _ => false
        }.asInstanceOf[Seq[headers.`Set-Cookie`]].map(_.cookie)
        if (cookies.nonEmpty) {
          cookieManager ! RecordCookie(url, cookies)
        }
        linkExtractors ! ExtractLink(url, response.entity)
      } else {
        log.warning(s"HTTP fetch return bad status code ${response.status} on $url")
        /** Consuming (or discarding) the Entity of a request is mandatory!
          * If accidentally left neither consumed or discarded Akka HTTP will assume the incoming data should remain back-pressured,
          * and will stall the incoming data via TCP back-pressure mechanisms.
          * A client should consume the Entity regardless of the status of the HttpResponse.
          */
        response.entity.dataBytes.runWith(Sink.ignore)(ActorMaterializer())
      }
    case FetchFailure(_, _) =>
      decreasePressure()
    case ExtractResult(url, links) =>
      relationManager ! RecordRelation(url, links.srcLinks)
      urlFilter ! FilterUrl(url, links.hrefLinks)
    case FilterResult(baseUrl, urls) =>
      urlDeduplicator ! Deduplicate(baseUrl, urls)
    case DeduplicateResult(_, urls) =>
      urlFrontier ! Enqueue(urls)
    case Start =>
      urlFrontier ! Dequeue
    case x => log.warning(s"Unknown message $x")
  }

  private def decreasePressure() = {
    pressure -= 1
    continue()
  }

  private def increasePressure() = pressure += 1

  private def continue(): Unit = {
    if (pressure < pressureThreshold) {
      urlFrontier ! Dequeue
    } else {
      //      log.info("Http fetching under pressure, will feed after 1 second")
      //      context.system.scheduler.scheduleOnce(1.second, new Runnable {
      //        override def run(): Unit = continue()
      //      })
    }
  }

  private def buildRequest(url: URL, cookies: Seq[headers.HttpCookie]): HttpRequest = {
    val requestHeaders = {
      val userAgentHeader: HttpHeader = headers.`User-Agent`("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
      if (cookies.nonEmpty) {
        val cookieHeader: HttpHeader = headers.Cookie(cookies.map(_.pair()).toIndexedSeq)
        List(userAgentHeader, cookieHeader)
      }
      else {
        List(userAgentHeader)
      }
    }
    HttpRequest(uri = Uri(url.toExternalForm), headers = requestHeaders)
  }

  override def postStop(): Unit = {
    super.postStop()
    val endTime = System.currentTimeMillis()
    val s = (endTime - startTime) / 1000
    log.info(s"Url consume rate ${urlCount / s} per second, total $urlCount")
    log.info(s"Pressure: $pressure")
  }
}
