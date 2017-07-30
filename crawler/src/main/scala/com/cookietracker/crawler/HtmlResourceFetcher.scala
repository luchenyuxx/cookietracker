package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, Props}

/**
  * This actor is responsible for getting the resources required by a
  * HTML document.
  *
  * In the industry of web advertising, they use tricks like getting a script
  * or an image from the server of retargeting company to track user preference
  * by returning a cookie.
  * So, this actor should monitor the set-cookie header of the response.
  *
  * It should set Referer header in the request to resource.
  */

object HtmlResourceFetcher {
  def props = Props(new HtmlResourceFetcher)

  case class FetchResource(baseUrl: URL, resources: Seq[URL])

}

class HtmlResourceFetcher extends Actor {

  import HtmlResourceFetcher._

  override def receive: Receive = {
    case FetchResource(baseUrl, resources) =>
  }
}
