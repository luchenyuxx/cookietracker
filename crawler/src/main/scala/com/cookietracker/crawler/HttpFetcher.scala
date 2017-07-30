package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

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
  *   Fetch(url) ~> FetchResult(responseFuture)
  * }}}
  */
object HttpFetcher {
  def props = Props(new HttpFetcher)

  case class Fetch(baseUrl: URL, request: HttpRequest)

  case class FetchResult(baseUrl: URL, response: HttpResponse)
}

class HttpFetcher extends Actor with ActorLogging {

  import HttpFetcher._
  // Needed by Http module
  implicit val system: ActorSystem = context.system
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case WorkAvailable => sender() ! GimmeWork
    case Fetch(url, request) =>
      implicit val executionContext = context.dispatcher
      log.info(s"Fetching $request")
      val futureSender = sender()
      val fetchFuture = Http().singleRequest(request)
      fetchFuture onSuccess {
        case r =>
          val statusOk = r.status.equals(StatusCodes.OK)
          if (statusOk)
            futureSender ! FetchResult(url, r)
          else {
            log.warning(s"HTTP fetch return bad status code ${r.status} on $url")
            /** Consuming (or discarding) the Entity of a request is mandatory!
              * If accidentally left neither consumed or discarded Akka HTTP will assume the incoming data should remain back-pressured,
              * and will stall the incoming data via TCP back-pressure mechanisms.
              * A client should consume the Entity regardless of the status of the HttpResponse.
              */
            r.entity.dataBytes.runWith(Sink.ignore)
          }
          futureSender ! GimmeWork
      }
      fetchFuture onFailure {
        case t => log.error(s"Fail to fetch url $url", t)
          futureSender ! GimmeWork
      }
  }
}
