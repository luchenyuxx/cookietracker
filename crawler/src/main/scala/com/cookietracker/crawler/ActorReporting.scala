package com.cookietracker.crawler

import akka.actor.{Actor, ActorLogging}

import scala.collection.mutable

trait ActorReporting {
  self: Actor with ActorLogging =>
  val messageCount: mutable.Map[String, Long] = mutable.Map()
  val startTime: Long = System.currentTimeMillis()

  override def receive: Receive = {
    case x =>
      val k = x.getClass.getName
      if (messageCount.contains(k)) {
        messageCount.update(k, messageCount(k) + 1)
      } else messageCount.put(k, 1)
      monitoredReceive(x)
  }


  override def postStop(): Unit = {
    val e = System.currentTimeMillis()
    val s = (e - startTime) / 1000
    messageCount.map(x => s"${x._1}: total ${x._2}, rate ${x._2 / s} per second").foreach(log.info(_))
  }

  def monitoredReceive: Receive
}
