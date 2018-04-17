package com.cookietracker.crawler

import java.net.URL

import akka.Done
import akka.stream.scaladsl.Sink
import com.cookietracker.common.DAO

import scala.concurrent.{ExecutionContext, Future}

object RelationManager {

  def sink(implicit ec: ExecutionContext): Sink[(URL, Seq[URL]), Future[Done]] = Sink.foreach[(URL, Seq[URL])]{ case (baseUrl, resLinks) =>
    val resourceLinks = resLinks.filter(u => !u.getHost.equals(baseUrl.getHost))
    resourceLinks.map(_.getHost).foreach(DAO().addRelation(baseUrl.getHost, _))
  }
}

