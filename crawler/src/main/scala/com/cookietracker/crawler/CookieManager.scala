package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.headers

object CookieManager {
  def props = Props(new CookieManager)

  case class RecordCookie(baseUrl: URL, cookies: Seq[headers.HttpCookie])

  case class GetCookie(url: URL)

  case class GetCookieResult(url: URL, cookies: Seq[headers.HttpCookie])
}

class CookieManager extends Actor with ActorLogging {

  import CookieManager._
  override def receive: Receive = {
    case RecordCookie(url, cookies) =>
    //      cookies.map(c => new HttpCookie(c.name, c.value, ))
    case GetCookie(url) => GetCookieResult(url, Seq())
  }
}
