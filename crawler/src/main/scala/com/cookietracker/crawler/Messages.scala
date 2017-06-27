package com.cookietracker.crawler

import java.net.{InetAddress, URL}

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future

case class Start(url: URL)

case class Scrap(url: URL)

case class Index(url: URL, content: Content)

case class Content(title: String, meta: String, urls: List[URL])

case class ScrapFinished(url: URL)

case class IndexFinished(url: URL, urls: List[URL])

case class ScrapFailure(url: URL, reason: Throwable)

case class ReadyToProcess(url: URL)

case class Process(url: URL)

case class Fetch(request: HttpRequest)

case class FetchResult(response: Future[HttpResponse])

case class DnsResolve(hostName: String)

case class DnsResolved(address: Option[InetAddress])

case class Enqueue(urls: List[URL])

case class StoreUrlTask()

case class EnqueueResult()

case class Dequeue(previousUrl: URL)

case class LoadUrlTask()

case class DequeueResult(urlLoaded: URL)