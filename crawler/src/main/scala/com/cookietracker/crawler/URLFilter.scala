package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * The URL filtering mechanism provides a customizable way to
  * control the set of URLs that are downloaded.
  */
object URLFilter {
  def props = Props(new URLFilter)
}
class URLFilter extends Actor{
  override def receive: Receive = ???
}
