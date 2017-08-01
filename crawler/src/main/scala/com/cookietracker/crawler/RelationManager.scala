package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, Props}

object RelationManager {
  def props = Props(new RelationManager)

  case class RecordRelation(baseUrl: URL, resourceLinks: Seq[URL])

}

class RelationManager extends Actor {

  import RelationManager._

  override def receive: Receive = {
    case RecordRelation(baseUrl, resourceLinks) =>

  }
}
