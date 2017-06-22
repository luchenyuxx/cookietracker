package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * Extract links from document
  */
object LinkExtractor {
  def props = Props(new LinkExtractor)
}
class LinkExtractor extends Actor{
  override def receive: Receive = ???
}
