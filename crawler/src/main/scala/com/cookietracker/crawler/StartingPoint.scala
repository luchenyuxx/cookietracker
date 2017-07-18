package com.cookietracker.crawler

import java.net.URL

import akka.actor.{ActorSystem, PoisonPill}

import scala.io.StdIn
import scala.language.postfixOps

object StartingPoint extends App {
  val system = ActorSystem("cookietracker")
  val webCrawler = system.actorOf(WebCrawler.props, "web-crawler")
  val startUrl = new URL("https://en.wikipedia.org/wiki/Main_Page")

  webCrawler ! DeduplicateResult(startUrl, Seq(startUrl))
  webCrawler ! Start

  println("Web crawler started, press RETURN to exit.")
  StdIn.readLine()

  webCrawler ! PoisonPill
  system.terminate
}
