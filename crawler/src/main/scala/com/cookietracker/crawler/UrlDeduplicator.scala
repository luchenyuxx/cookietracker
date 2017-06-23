package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * To avoid downloading and processing a document multiple times, a URL dedup test
  * must be performed on each extracted link before adding it to the URL frontier.
  *
  * To perform the URL dedup test, we can store all the URLs seen by our crawler in
  * canonical form in a database.
  *
  * To reduce the numbe rof operations on the database store, we can keep in-memory
  * cache of popular URLs on each host shared by all threads.
  */
object UrlDeduplicator {
  def props = Props(new UrlDeduplicator)
}

class UrlDeduplicator extends Actor {
  override def receive: Receive = ???
}
