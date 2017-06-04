package cookietracker

import java.net.URL

import akka.actor.{Actor, Props, _}

import scala.language.postfixOps

object Supervisor {
  def props(socketPublisher: ActorRef) = Props(new Supervisor(Some(socketPublisher)))
  def props = Props(new Supervisor(None))
}

class Supervisor(socketPublisher: Option[ActorRef]) extends Actor {
  val indexer: ActorRef = context actorOf Indexer.props(self)

  val maxPages = 100
  val maxRetries = 2

  var numVisited = 0
  var toScrap = Set.empty[URL]
  var scrapCounts = Map.empty[URL, Int]
  var host2Actor = Map.empty[String, ActorRef]

  def receive: Receive = {
    case Start(url) =>
      println(s"starting $url")
      scrap(url)
    case ScrapFinished(url) =>
      println(s"scraping finished $url")
    case IndexFinished(url, urls) =>
      if (numVisited < maxPages)
        urls.toSet.filter(l => !scrapCounts.contains(l)).foreach(scrap)
      checkAndShutdown(url)
    case ScrapFailure(url, reason) =>
      val retries: Int = scrapCounts(url)
      println(s"scraping failed $url, $retries, reason = $reason")
      if (retries < maxRetries) {
        countVisits(url)
        host2Actor(url.getHost) ! Scrap(url)
      } else
        checkAndShutdown(url)
  }

  // TODO: calculate graph when recieve IndexFinished
  def calculateGraph(url: URL, urls: Seq[URL]) = {

  }

  def checkAndShutdown(url: URL): Unit = {
    toScrap -= url
    // if nothing to visit
    if (toScrap.isEmpty) {
      self ! PoisonPill
    }
  }

  def scrap(url: URL): Unit = {
    val host = url.getHost
    println(s"Supervisor: going to scrap $host")
    if (!host.isEmpty) {
      val actor = host2Actor.getOrElse(host, {
        val buff = context actorOf SiteCrawler.props(self, indexer)
        host2Actor += (host -> buff)
        buff
      })

      numVisited += 1
      toScrap += url
      countVisits(url)
      actor ! Scrap(url)
    }
  }

  def countVisits(url: URL): Unit = scrapCounts += (url -> (scrapCounts.getOrElse(url, 0) + 1))
}