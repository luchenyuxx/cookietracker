package com.cookietracker.crawler

import java.io.InputStream
import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.HttpEntity
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, StreamConverters}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

/**
  * Extract links from document
  */
object LinkExtractor {
  def props = Props(new LinkExtractor)

  case class LinkContainer(hrefLinks: Seq[URL], srcLinks: Seq[URL])

  private val HREF_LINK_NAME = "href"
  private val SRC_LINK_NAME = "src"

  private def extractFromInputStream(i: InputStream, baseUrl: URL): LinkContainer = {
    val document = Jsoup.parse(i, null, baseUrl.toExternalForm)
    new LinkContainer(getLinksByAttribute(document, HREF_LINK_NAME, baseUrl), getLinksByAttribute(document, SRC_LINK_NAME, baseUrl))
  }

  private def getLinksByAttribute(document: Document, attributeName: String, baseUrl: URL): Seq[URL] = {
    val urlValidator = UrlValidator.getInstance()
    document.getElementsByAttribute(attributeName).map(_.attr(attributeName)).distinct.map(preprocess(_, baseUrl)).filter(urlValidator.isValid).map(new URL(_))
  }

  private def preprocess(attribute: String, baseUrl: URL): String = if (attribute.startsWith("//")) {
    baseUrl.getProtocol + ":" + attribute
  } else attribute

  case class ExtractLink(baseUrl: URL, entity: HttpEntity)

  case class ExtractResult(baseUrl: URL, links: LinkContainer)

  case class ExtractFailure(bastUrl: URL, throwable: Throwable)
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
            log.info(s"Success to extract ${links.hrefLinks.size} href links, ${links.srcLinks.size} src links in $url")
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
