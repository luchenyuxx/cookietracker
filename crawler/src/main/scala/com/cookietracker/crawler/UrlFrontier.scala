package com.cookietracker.crawler

import java.net.URL
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The URL frontier is the data structure that contains all the URLs that
  * remain to be downloaded.
  *
  * To perform a breadth-first traversal, use a FIFO queue.
  *
  * To avoid overload a server by downloading too frequently, we make each worker
  * thread have its separate sub-queue determined by the host name.
  */
object UrlFrontier {
  def props = Props(new UrlFrontier)

  // A host contains a queue and a state
  val subQueueByHost: ConcurrentHashMap[String, ConcurrentLinkedQueue[URL]] = new ConcurrentHashMap()
  val hostByReady: ConcurrentHashMap[String, Boolean] = new ConcurrentHashMap()

  case class Enqueue(urls: Seq[URL])

  case class StoreUrlTask()

  case class EnqueueResult()

  case object Dequeue

  case class LoadUrlTask()

  case class DequeueResult(urlLoaded: URL)

  case class EmptyOrBusyQueue()
}

class UrlFrontier extends Actor with ActorLogging {

  import UrlFrontier._

  implicit val contextExecutor: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case Enqueue(urls) =>
      urls.foreach(url => {
        val hostName = url.getHost
        Option(subQueueByHost.get(hostName)) match {
          case Some(aSubQueue) => aSubQueue.add(url)
          case None =>
            val subQueue: ConcurrentLinkedQueue[URL] = new ConcurrentLinkedQueue()
            subQueue.add(url)
            subQueueByHost.put(hostName, subQueue)
            hostByReady.put(hostName, true)
        }
      })
      log.info(s"Enqueue ${urls.size} urls")
    case Dequeue =>
      // We should find a queue which is ready and not empty
      hostByReady.find(x => x._2 && !subQueueByHost.get(x._1).isEmpty).map(_._1) match {
        case Some(hostname) =>
          log.info(s"Dequeue $hostname")
          hostByReady.update(hostname, false)
          context.system.scheduler.scheduleOnce(3 seconds) {
            hostByReady.update(hostname, true)
          }(contextExecutor)
          sender() ! DequeueResult(subQueueByHost.get(hostname).poll())
        case None => sender() ! EmptyOrBusyQueue
      }
  }
}

