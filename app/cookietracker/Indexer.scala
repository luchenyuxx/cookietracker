package cookietracker

import java.net.URL

import akka.actor.{Actor, ActorRef, Props}

object Indexer {
  def props(supervisor: ActorRef) = Props(new Indexer(supervisor))
}

class Indexer(supervisor: ActorRef) extends Actor {
  var store = Map.empty[URL, Content]

  def receive: Receive = {
    case Index(url, content) =>
      println(s"saving page $url with $content")
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