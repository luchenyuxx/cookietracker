package com.cookietracker.crawler

import java.net.URL

import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream._
import com.cookietracker.common.DAO

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

/**
  * The URL frontier is the data structure that contains all the URLs that
  * remain to be downloaded.
  *
  * To perform a breadth-first traversal, use a FIFO queue.
  *
  * To avoid overload a server by downloading too frequently, we make each worker
  * thread have its separate sub-queue determined by the host name.
  */

object UrlFrontier {
  def sink(implicit ec: ExecutionContext) = Sink.foreach[URL]{DAO().addUrl(_)}
  def source(implicit ec: ExecutionContext) = Source.fromGraph(UrlFrontierSource)
}
object UrlFrontierSource extends GraphStage[SourceShape[URL]] {
  val out = Outlet[URL]("pop_url")
  val shape = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
    implicit val ex: ExecutionContext = ExecutionContext.global

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        Await.result(DAO().popUrl, Duration.Inf) match {
          case Some(u) => push(out, u)
          case None =>
            println("get a none url")
        }
      }
    })
  }
}

