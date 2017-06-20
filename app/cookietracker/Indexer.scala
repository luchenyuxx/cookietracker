package cookietracker

import java.net.URL

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

object Indexer {
  def props(supervisor: ActorRef) = Props(new Indexer(supervisor))
}

class Indexer(supervisor: ActorRef) extends Actor {
  var store = Map.empty[URL, Content]
  val logger = Logger(this.getClass)

  def receive: Receive = {
    case Index(url, content) =>
      logger.info(s"saving page $url with $content")
      store += (url -> content)
      supervisor ! IndexFinished(url, content.urls)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    store.foreach(println)
    println(store.size)
  }
}