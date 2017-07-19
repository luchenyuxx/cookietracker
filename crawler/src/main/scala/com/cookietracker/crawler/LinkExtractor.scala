package com.cookietracker.crawler

import java.io.InputStream
import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, StreamConverters}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

/**
  * Extract links from document
  */
object LinkExtractor {
  def props = Props(new LinkExtractor)

  private def extractFromInputStream(i: InputStream, baseUrl: URL): Seq[URL] = {
    Jsoup.parse(i, null, baseUrl.toExternalForm).getElementsByTag("a").toIndexedSeq.map(_.attr("href")).distinct.filter(s => UrlValidator.getInstance().isValid(s)).map(new URL(_))
  }
}

class LinkExtractor extends Actor with ActorLogging {

  import LinkExtractor._

  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case ExtractLink(url, e) =>
      if (e.getContentType().mediaType.isText) {
        log.info(s"Extracting links in $url")
        val futureReceiver = sender()
        Try {
          val inputStream = e.dataBytes.runWith(StreamConverters.asInputStream())
          extractFromInputStream(inputStream, url)
        } match {
          case Success(links) =>
            log.info(s"Success to extract ${links.size} links in $url")
            futureReceiver ! ExtractResult(url, links)
          case Failure(t) =>
            log.error(t, s"Fail to extract links in $url")
            futureReceiver ! ExtractFailure(url, t)
        }
      } else {
        log.warning(s"Won't extract $url with content type ${e.getContentType()}")
      }
      /** Consuming (or discarding) the Entity of a request is mandatory!
        * If accidentally left neither consumed or discarded Akka HTTP will assume the incoming data should remain back-pressured,
        * and will stall the incoming data via TCP back-pressure mechanisms.
        * A client should consume the Entity regardless of the status of the HttpResponse.
        */
      e.dataBytes.runWith(Sink.ignore)
  }
}
