package com.cookietracker.crawler

import java.net.{URL, URLConnection}

import akka.actor.{Actor, ActorLogging, Props}

/**
  * The URL filtering mechanism provides a customizable way to
  * control the set of URLs that are downloaded.
  */
object UrlFilter {
  def props = Props(new UrlFilter)

  case class FilterUrl(baseUrl: URL, urls: Seq[URL])

  case class FilterResult(baseUrl: URL, urls: Seq[URL])
}

class UrlFilter extends Actor with ActorLogging {

  import UrlFilter._
  override def receive: Receive = {
    case FilterUrl(baseUrl, urls) =>
      val filtered = urls.filter { u =>
        Option(URLConnection.guessContentTypeFromName(u.getPath)).forall(_.contains("text"))
      }
      log.info(s"Filter ${urls.size} urls to ${filtered.size}")
      sender() ! FilterResult(baseUrl, filtered)
  }
}
