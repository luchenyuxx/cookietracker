package com.cookietracker.crawler

import akka.actor.{Actor, ActorLogging, Props}

object CookieManager {
  def props = Props(new CookieManager)
}

class CookieManager extends Actor with ActorLogging {
  override def receive: Receive = {
    case RecordCookie(url, cookies) =>
    case GetCookie(url) => GetCookieResult(url, Seq())
  }
}
