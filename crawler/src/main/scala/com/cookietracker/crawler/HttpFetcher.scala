package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

/**
  * Fetch document using HTTP protocol.
  * When receiving a URL, it should fetch it and return a document input stream.
  *
  * Implemented using Akka Http.
  * Consuming (or discarding) the Entity of a response is mandatory! If accidentally left
  * neither consumed or discarded Akka HTTP will assume the incoming data should remain
  * back-pressured, and will stall the incoming data via TCP back-pressure mechanisms.
  * A client should consume the Entity regardless of the status of the HttpResponse.
  *
  * {{{
  *   Fetch(url) ~> FetchResult(baseUrl, response)
  * }}}
  */
object HttpFetcher {
  def props = Props(new HttpFetcher)

  case class Fetch(baseUrl: URL, request: HttpRequest)

  case class FetchResult(baseUrl: URL, response: HttpResponse)

  case class FetchFailure(url: URL, t: Throwable)

}

class HttpFetcher extends Actor with ActorLogging {

  import HttpFetcher._

  // Needed by Http module
  implicit val materializer = ActorMaterializer()
  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher

  lazy val httpExt = Http(context.system)

  override def receive: Receive = {
    case Fetch(url, request) =>
      log.info(s"Fetching $request")
      val futureSender = sender()
      val fetchFuture = httpExt.singleRequest(request)
      fetchFuture onSuccess {
        case r =>
          futureSender ! FetchResult(url, r)
      }
      fetchFuture onFailure {
        case t =>
          log.error(s"Fail to fetch url $url", t)
          futureSender ! FetchFailure(url, t)
      }
  }
}
