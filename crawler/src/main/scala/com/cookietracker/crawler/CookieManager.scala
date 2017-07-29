package com.cookietracker.crawler

import akka.actor.{Actor, ActorLogging, Props}
import com.cookietracker.common.data.HttpCookie

object CookieManager {
  def props = Props(new CookieManager)
}

class CookieManager extends Actor with ActorLogging {
  override def receive: Receive = {
    case RecordCookie(url, cookies) =>
    //      cookies.map(c => new HttpCookie(c.name, c.value, ))
    case GetCookie(url) => GetCookieResult(url, Seq())
  }
}
