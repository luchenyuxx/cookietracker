package cookietracker

import java.net.URL

import akka.actor.{Actor, Props, _}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class SiteCrawler(supervisor: ActorRef, indexer: ActorRef) extends Actor {
  val process = "Process next url"
  val readyToProcess = "Ready to process next url"

  val scraper = context actorOf Props(new Scraper(indexer))
  implicit val timeout = Timeout(3 seconds)
  val inputListener = context actorOf Props(new InputListener)
//  val tick =
//    context.system.scheduler.schedule(0 millis, 1000 millis, self, process)
  var toProcess = List.empty[URL]

  def receive: Receive = {
    case Scrap(url) =>
      // wait some time, so we will not spam a website
      println(s"SiteCraler: received request to process ... $url")
      toProcess = url :: toProcess
      self ! readyToProcess
    case `readyToProcess` =>
      toProcess match {
        case Nil =>
        case url :: list =>
          toProcess = list
          inputListener ! ReadyToProcess(url)
      }
    case Process(url) =>
      println(s"SiteCrawler: processing $url")
      (scraper ? Scrap(url)).mapTo[ScrapFinished]
        .recoverWith { case e => Future {
          ScrapFailure(url, e)
        }
        }
        .pipeTo(supervisor)
      self ! readyToProcess
//    case `process` =>
//      toProcess match {
//        case Nil =>
//        case url :: list =>
//          println(s"site scraping... $url")
//          toProcess = list
//          (scraper ? Scrap(url)).mapTo[ScrapFinished]
//            .recoverWith { case e => Future {
//              ScrapFailure(url, e)
//            }
//            }
//            .pipeTo(supervisor)
//      }
  }
}