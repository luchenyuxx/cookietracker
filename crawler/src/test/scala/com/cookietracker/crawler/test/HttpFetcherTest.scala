package com.cookietracker.crawler.test

import java.net.URL

import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.pattern.ask
import akka.util.Timeout
import com.cookietracker.crawler.{Fetch, FetchResult, HttpFetcher}

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpFetcherTest extends AkkaTest {
  "A HttpFetcher" must {
    "successfully fetch google.com" in {
      implicit val timeout = Timeout(3 seconds)
      val httpFetcher = system.actorOf(HttpFetcher.props, "http-fetcher")
      implicit val contextExecutor = system.dispatcher
      val targetUrl = new URL("http://www.google.com")
      val requests = HttpRequest(uri = Uri(targetUrl.toExternalForm))
      val r = httpFetcher ? Fetch(targetUrl, requests)
      val f = r.mapTo[FetchResult].flatMap(_.response)
      Await.result(f, 3 seconds)
      f onFailure {
        case _ => assert(false)
      }
    }
  }
}
