package com.cookietracker.crawler

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.ActorMaterializer

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
}

class HttpFetcher extends Actor with HaveLogger {
  // Needed by Http module
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case Fetch(url) =>
      logger.info(s"Fetching $url")
      sender() ! FetchResult(Http().singleRequest(HttpRequest(uri = Uri(url))))
  }
}
