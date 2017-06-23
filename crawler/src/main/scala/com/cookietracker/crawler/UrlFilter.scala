package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * The URL filtering mechanism provides a customizable way to
  * control the set of URLs that are downloaded.
  */
object UrlFilter {
  def props = Props(new UrlFilter)
}
class UrlFilter extends Actor{
  override def receive: Receive = ???
}
