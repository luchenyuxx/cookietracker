package com.cookietracker.crawler

import java.net.{InetAddress, URL}

import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}

import scala.concurrent.Future

case class Fetch(baseUrl: URL, request: HttpRequest)

case class FetchResult(baseUrl: URL, response: Future[HttpResponse])

case class ExtractLink(baseUrl: URL, entity: Future[HttpEntity])

case class ExtractResult(baseUrl: URL, links: Seq[URL])

case class ExtractFailure(bastUrl: URL, throwable: Throwable)

case class DnsResolve(url: URL)

case class DnsResolved(baseUrl: URL, address: Option[InetAddress])

case class Enqueue(urls: List[URL])

case class StoreUrlTask()

case class EnqueueResult()

case class Dequeue(previousUrl: URL)

case class LoadUrlTask()

case class DequeueResult(urlLoaded: URL)