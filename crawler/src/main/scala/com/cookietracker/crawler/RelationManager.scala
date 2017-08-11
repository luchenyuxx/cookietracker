package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, Props}
import com.cookietracker.common.data.{DaoFactory, HostRelation}

object RelationManager {
  def props = Props(new RelationManager)

  case class RecordRelation(baseUrl: URL, resourceLinks: Seq[URL])

}

class RelationManager extends Actor {

  import RelationManager._

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case RecordRelation(baseUrl, resLinks) =>
      val resourceLinks = resLinks.filter(u => !u.getHost.equals(baseUrl.getHost))
      val hostRelationDao = DaoFactory.hostRelationDao
      for (
        existingDst <- hostRelationDao.allRelationsFrom(baseUrl.getHost).map(_.map(_.toHost))
      ) {
        hostRelationDao.insert(resourceLinks.filter(r => !existingDst.contains(r.getHost)).map(u => HostRelation(baseUrl.getHost, u.getHost, u.toExternalForm)))
      }
  }
}
