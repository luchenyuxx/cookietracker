package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * Fetch document using HTTP protocol
  */
object HTTPFetcher {
  def props = Props(new HTTPFetcher)
}
class HTTPFetcher extends Actor{
  override def receive: Receive = ???
}
