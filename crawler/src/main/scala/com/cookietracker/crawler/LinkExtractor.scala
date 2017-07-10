package com.cookietracker.crawler

import java.io.InputStream
import java.net.URL

import akka.actor.{Actor, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
/**
  * Extract links from document
  */
object LinkExtractor {
  def props = Props(new LinkExtractor)

  private def extractFromInputStream(i: InputStream, baseUrl: URL): Seq[URL] = {
    Jsoup.parse(i, null, baseUrl.getPath).getElementsByTag("a").toIndexedSeq.map(_.attr("href")).filter(s => UrlValidator.getInstance().isValid(s)).map(new URL(_))
  }
}
class LinkExtractor extends Actor{

  import LinkExtractor._

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case ExtractLink(url, e) =>
      val linksFuture = e.map(entity => entity.dataBytes).map(_.runWith(StreamConverters.asInputStream())).map(i => extractFromInputStream(i, url))
      linksFuture onSuccess {
        case links => sender() ! ExtractResult(url, links)
      }
      linksFuture onFailure {
        case t => sender() ! ExtractFailure(url, t)
      }
  }
}
