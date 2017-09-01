package com.cookietracker.crawler

import java.io._
import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import com.cookietracker.common.data.{DaoFactory, Memory}
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
object UrlDeduplicator {
  def props = Props(new UrlDeduplicator)

  val MEMORY_NAME = "url_bloom_filter"
  lazy val bloomFilter: BloomFilter[CharSequence] = {
    Await.result(
      DaoFactory.memoryDao.getByName(MEMORY_NAME).map {
        case Some(m) =>
          BloomFilter.readFrom[CharSequence](new ByteArrayInputStream(m.data), Funnels.unencodedCharsFunnel())
        case None =>
          BloomFilter.create[CharSequence](Funnels.unencodedCharsFunnel(), Int.MaxValue, 0.95)
      }, Duration.Inf
    )
  }

  case class Deduplicate(baseUrl: URL, urls: Seq[URL])

  case class DeduplicateResult(baseUrl: URL, urls: Seq[URL])

}

class UrlDeduplicator extends Actor with ActorLogging {

  import UrlDeduplicator._

  override def receive: Receive = {
    case Deduplicate(baseUrl, urls) =>
      val deduplicated = urls.filter(u => !bloomFilter.mightContain(u.toExternalForm))
      log.info(s"Deduplicated urls from ${urls.size} to ${deduplicated.size}")
      deduplicated.map(_.toExternalForm).foreach(bloomFilter.put)
      sender() ! DeduplicateResult(baseUrl, deduplicated)
  }

  override def postStop(): Unit = {
    super.postStop()
    val outputStream = new ByteArrayOutputStream()
    bloomFilter.writeTo(outputStream)
    Await.ready(
      DaoFactory.memoryDao.upsert(Memory(MEMORY_NAME, outputStream.toByteArray)), Duration.Inf
    )
    log.info("Wrote url bloom filter to database")
  }
}
