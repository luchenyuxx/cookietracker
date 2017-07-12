package com.cookietracker.crawler

import java.io.InputStream
import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
/**
  * Extract links from document
  */
object LinkExtractor {
  def props = Props(new LinkExtractor)

  private def extractFromInputStream(i: InputStream, baseUrl: URL): Seq[URL] = {
    Jsoup.parse(i, null, baseUrl.toExternalForm).getElementsByTag("a").toIndexedSeq.map(_.attr("href")).distinct.filter(s => UrlValidator.getInstance().isValid(s)).map(new URL(_))
  }
}
class LinkExtractor extends Actor with ActorLogging{

  import LinkExtractor._
  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case ExtractLink(url, e) =>
      log.info(s"Extracting links in $url")
      val futureReceiver = sender()
      val linksFuture = e.map(entity => entity.dataBytes).map(_.runWith(StreamConverters.asInputStream())).map(i => extractFromInputStream(i, url))
      linksFuture onSuccess {
        case links =>
          log.info(s"Success to extract ${links.size} links in $url")
          futureReceiver ! ExtractResult(url, links)
      }
      linksFuture onFailure {
        case t =>
          log.error(t, s"Fail to extract links in $url")
          futureReceiver ! ExtractFailure(url, t)
      }
  }
}
