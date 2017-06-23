package com.cookietracker.crawler

import java.net.URL

import akka.http.scaladsl.model.HttpResponse

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

case class Fetch(url: URL)

case class FetchResult(response: Future[HttpResponse])

case class DnsResolve(url: URL)

case class DnsResolved(newUrl: Option[URL])