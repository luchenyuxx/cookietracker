package com.cookietracker.crawler

import java.io._
import java.net.URL

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.cookietracker.common.{DAO, Memory}
import com.google.common.hash.{BloomFilter, Funnels}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * To avoid downloading and processing a document multiple times, a URL dedup test
  * must be performed on each extracted link before adding it to the URL frontier.
  *
  * To perform the URL dedup test, we can store all the URLs seen by our crawler in
  * canonical form in a database.
  *
  * To reduce the number of operations on the database store, we can keep in-memory
  * cache of popular URLs on each host shared by all threads.
  */

object UrlDeduplicator extends GraphStage[FlowShape[URL, URL]] {
  val MEMORY_NAME = "url_bloom_filter"
  val in = Inlet[URL]("url_in")
  val out = Outlet[URL]("url_out")
  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {

    lazy val bloomFilter: BloomFilter[CharSequence] = {
      Await.result(
        DAO().getByName(MEMORY_NAME).map {
          case Some(m) =>
            println("Read memory")
            BloomFilter.readFrom[CharSequence](new ByteArrayInputStream(m.data), Funnels.unencodedCharsFunnel())
          case None =>
            println("Create memory")
            BloomFilter.create[CharSequence](Funnels.unencodedCharsFunnel(), Int.MaxValue, 0.95)
        }, Duration.Inf
      )
    }

    override def postStop(): Unit = {
      super.postStop()
      val outputStream = new ByteArrayOutputStream()
      bloomFilter.writeTo(outputStream)
      Await.ready(
        DAO().upsert(Memory(MEMORY_NAME, outputStream.toByteArray)), Duration.Inf
      )
      println("Write memory")
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val u = grab(in)
        if (bloomFilter.mightContain(u.toExternalForm)) {
          pull(in)
        } else {
          bloomFilter.put(u.toExternalForm)
          push(out, u)
        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })
  }
}

