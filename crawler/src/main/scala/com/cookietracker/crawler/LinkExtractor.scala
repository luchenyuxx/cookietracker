package com.cookietracker.crawler

import java.io.{InputStream, StringReader}
import java.net.URL
import java.util.Scanner

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream._
import akka.stream.scaladsl.{Sink, StreamConverters}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.cookietracker.crawler.LinkExtractor._
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
  * Extract links from document
  */
object LinkExtractor {
  case class LinkContainer(hrefLinks: Seq[URL], srcLinks: Seq[URL])

  private val HREF_LINK_NAME = "href"
  private val SRC_LINK_NAME = "src"

  private def extractFromInputStream(i: InputStream, baseUrl: URL): LinkContainer = {
    val document = Jsoup.parse(i, null, baseUrl.toExternalForm)
    new LinkContainer(getLinksByAttribute(document, HREF_LINK_NAME, baseUrl), getLinksByAttribute(document, SRC_LINK_NAME, baseUrl))
  }

  private def get(i: InputStream) = {
  }

  private def getLinksByAttribute(document: Document, attributeName: String, baseUrl: URL): Seq[URL] = {
    val urlValidator = UrlValidator.getInstance()
    document.getElementsByAttribute(attributeName).map(_.attr(attributeName)).distinct.map(preprocess(_, baseUrl)).filter(urlValidator.isValid).map(new URL(_))
  }

  private def preprocess(attribute: String, baseUrl: URL): String = if (attribute.startsWith("//")) {
    baseUrl.getProtocol + ":" + attribute
  } else attribute

}

class LinkExtractor(implicit m: Materializer) extends GraphStage[FlowShape[(URL, HttpResponse), (URL, LinkContainer)]] {
  val in = Inlet[(URL, HttpResponse)]("in")
  val out = Outlet[(URL, LinkContainer)]("out")
  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val message = grab(in)
        val response = message._2
        if(response.status.equals(StatusCodes.OK) && response.entity.getContentType().mediaType.isText) {
          Try {
            val inputStream = response.entity.dataBytes.runWith(StreamConverters.asInputStream())
            extractFromInputStream(inputStream, message._1)
          } match {
            case Success(s) =>
              println(s"Successfully extract ${s.hrefLinks.size} href links and ${s.srcLinks.size} source links from ${message._1}")
              push(out, (message._1, s))
            case Failure(t) =>
              println(s"Fail to extract link from ${message._1}")
              t.printStackTrace()
              pull(in)
          }
        } else {
          println(s"Ignore status code ${response.status} from ${message._1}")
          response.entity.dataBytes.runWith(Sink.ignore)
          pull(in)
        }
      }
    })
    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })
  }
}
