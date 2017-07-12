package com.cookietracker.crawler

import java.util
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.JavaConversions._

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

  val seenUrl: util.Set[String] = Collections.newSetFromMap[String](new ConcurrentHashMap())
}

class UrlDeduplicator extends Actor with ActorLogging {

  import UrlDeduplicator._

  override def receive: Receive = {
    case Deduplicate(baseUrl, urls) =>
      val deduplicated = urls.filter(u => !seenUrl.contains(u.toExternalForm))
      log.info(s"Deduplicated urls from ${urls.size} to ${deduplicated.size}")
      seenUrl.addAll(deduplicated.map(_.toExternalForm))
      sender() ! DeduplicateResult(baseUrl, deduplicated)
  }
}
