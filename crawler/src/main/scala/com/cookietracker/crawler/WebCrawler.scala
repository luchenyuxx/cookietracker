package com.cookietracker.crawler

import akka.actor.{Actor, Props}

object WebCrawler {
  def props = Props(new WebCrawler)
}
class WebCrawler extends Actor{
  override def receive: Receive = ???
}
