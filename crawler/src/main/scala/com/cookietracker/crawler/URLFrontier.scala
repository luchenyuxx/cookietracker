package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * The URL frontier is the data structure that contains all the URLs that
  * remain to be downloaded.
  *
  * To perform a breadth-first traversal, use to FIFO queue.
  *
  * To avoid overload a server by downloading too frequently, we make each worker
  * thread have its separate sub-queue determined by the host name.
  */
object URLFrontier {
  def props = Props(new URLFrontier)
}
class URLFrontier extends Actor{
  override def receive: Receive = ???
}
