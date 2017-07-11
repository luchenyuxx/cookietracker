package com.cookietracker.crawler

import java.net.URL
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import akka.actor.{Actor, ActorRef, Props}
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
  * The URL frontier is the data structure that contains all the URLs that
  * remain to be downloaded.
  *
  * To perform a breadth-first traversal, use to FIFO queue.
  *
  * To avoid overload a server by downloading too frequently, we make each worker
  * thread have its separate sub-queue determined by the host name.
  */
object UrlFrontier {
  def props = Props(new UrlFrontier)

  // A host contains a queue and a state
  val subQueueByHost: ConcurrentHashMap[String, ConcurrentLinkedQueue[URL]] = new ConcurrentHashMap()
  val hostByReady: ConcurrentHashMap[String, Boolean] = new ConcurrentHashMap()
}

class UrlFrontier extends Actor {
  val resolver: ActorRef = context.actorOf(DnsResolver.props)
  val hostTimer: ActorRef = context.actorOf(HostTimer.props)

  override def receive: Receive = {
    case Enqueue(urls) =>
      // When receive a bag of URLs, call a work thread to work on it
      urls.foreach(url => {
        val hostName = url.getHost
        Option(UrlFrontier.subQueueByHost.get(hostName)) match {
          case Some(aSubQueue) => aSubQueue.add(url)
          case None => {
            val subQueue: ConcurrentLinkedQueue[URL] = new ConcurrentLinkedQueue()
            subQueue.add(url)
            UrlFrontier.subQueueByHost.put(hostName, subQueue)
            UrlFrontier.hostByReady.put(hostName, true)
          }
        }
        sender() ! EnqueueResult()
      })
    case Dequeue() =>
      hostTimer ! ReleaseHost()
      for ((hostname, isready) <- UrlFrontier.hostByReady) {
        if (isready) {
          UrlFrontier.hostByReady.update(hostname, false)
          resolver ! DequeueResult(UrlFrontier.subQueueByHost.get(hostname).poll())
        }
      }
  }
}

//TODO not finished, need to change for the timer
case class ReleaseHost()

object HostTimer {
  def props = Props(new HostTimer)
}

class HostTimer extends Actor {
  context.system.scheduler.schedule(0 seconds, 5 seconds)

  override def receive: Receive = {
    case ReleaseHost() => {
      for ((hostname, isready) <- UrlFrontier.hostByReady)
        if (!isready) UrlFrontier.hostByReady.update(hostname, true)
    }
  }
}


/*
class FrontierTask(url: URL) extends Actor {
  override def receive: Receive = {
    case StoreUrlTask() =>
      val hostName = url.getHost
      Option(UrlFrontier.getSubQueueByHost.get(hostName)) match {
        case Some(aSubQueue) => aSubQueue.add(url)
        case None => {
          val subQueue: ConcurrentLinkedQueue[URL] = new ConcurrentLinkedQueue()
          subQueue.add(url)
          UrlFrontier.getSubQueueByHost.put(hostName, subQueue)
          UrlFrontier.getHostByReady.put(hostName, true)
        }
      }
      sender() ! EnqueueResult()
    case LoadUrlTask() => {
      if (url != null) {
        UrlFrontier.getHostByReady.update(url.getHost, true)
      }
      for((hostname, isready) <- UrlFrontier.getHostByReady) {
        if (isready) {
          UrlFrontier.getHostByReady.update(hostname, false)
          sender() ! DequeueResult(UrlFrontier.getSubQueueByHost.get(hostname).poll())
        }
      }
    }
  }
}
*/