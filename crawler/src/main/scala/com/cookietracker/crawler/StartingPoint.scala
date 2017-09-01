package com.cookietracker.crawler

import java.net.URL

import akka.actor.{ActorSystem, PoisonPill}
import com.cookietracker.common.data.SchemaChecker
import com.cookietracker.common.database.PostgreSqlComponent

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn
import scala.language.postfixOps

object StartingPoint extends App {
  def initialize(): Unit = {
    checkDataBaseSchema()
  }
  def checkDataBaseSchema(): Unit = {
    val checker = new SchemaChecker with PostgreSqlComponent
    checker.checkAndCreateTables(ExecutionContext.global)
  }

  initialize()

  val system = ActorSystem("cookietracker")
  val webCrawler = system.actorOf(WebCrawler.props, "web-crawler")
  val startUrl = new URL("http://www.leparisien.fr/")

  //  webCrawler ! DeduplicateResult(startUrl, Seq(startUrl))
  webCrawler ! Start

  println("Web crawler started, press RETURN to exit.")
  StdIn.readLine()

  webCrawler ! PoisonPill
  Await.result(system.terminate(), Duration.Inf)
}