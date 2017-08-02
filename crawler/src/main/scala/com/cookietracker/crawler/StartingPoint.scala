package com.cookietracker.crawler

import java.net.URL

import akka.actor.{ActorSystem, PoisonPill}
import com.cookietracker.crawler.UrlDeduplicator.DeduplicateResult

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.language.postfixOps

object StartingPoint extends App {
  val system = ActorSystem("cookietracker")
  val webCrawler = system.actorOf(WebCrawler.props, "web-crawler")
  val startUrl = new URL("http://www.leparisien.fr/")

  webCrawler ! DeduplicateResult(startUrl, Seq(startUrl))
  webCrawler ! Start

  println("Web crawler started, press RETURN to exit.")
  StdIn.readLine()

  webCrawler ! PoisonPill
  Await.result(system.terminate(), Duration.Inf)
}