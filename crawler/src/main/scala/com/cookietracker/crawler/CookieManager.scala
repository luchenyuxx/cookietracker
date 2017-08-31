package com.cookietracker.crawler

import java.net.URL
import java.sql.Date

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.headers
import com.cookietracker.common.data.{DaoFactory, HttpCookie}
import com.cookietracker.crawler.CookieManager.{GetCookie, GetCookieResult}

import scala.concurrent.ExecutionContextExecutor

/**
  * CookieManager is responsible for recording and getting cookies from database.
  */
object CookieManager {
  def props = Props(new CookieManager)

  case class RecordCookie(baseUrl: URL, cookies: Seq[headers.HttpCookie])

  case class GetCookie(url: URL)

  case class GetCookieResult(url: URL, cookies: Seq[headers.HttpCookie])
}

class NoCookieManager extends Actor {
  override def receive = {
    case GetCookie(url) => sender() ! GetCookieResult(url, Seq())
    case _ =>
  }
}

class CookieManager extends Actor with ActorLogging {

  import CookieManager._

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case RecordCookie(url, cookies) =>
      val cookiesToInsert = cookies.map(c => HttpCookie(c.name, c.value, c.expires.map(x => new Date(x.clicks)), c.maxAge, c.domain.getOrElse(url.getHost), c.path, c.secure, c.httpOnly, c.extension, None))
      val f = DaoFactory.httpCookieDao.insert(cookiesToInsert)
      f onSuccess {
        case ls => log info s"Success to insert ${ls.size} cookies."
      }
      f onFailure {
        case t => log.error(t, "Fail to insert cookies.")
      }
    //      cookies.map(c => new HttpCookie(c.name, c.value, ))
    case GetCookie(url) =>
      log.info(s"Get cookies for ${url.toExternalForm}")
      sender() ! GetCookieResult(url, Seq())
  }
}
