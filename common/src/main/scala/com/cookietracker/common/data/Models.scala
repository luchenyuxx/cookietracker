package com.cookietracker.common.data

import java.sql.Date

trait WithId {
  def id: Option[Long]
}

final case class HostRelation(fromHost: String, toHost: String, requestUrl: String, id: Option[Long] = None) extends WithId

final case class Url(url: String, id: Option[Long] = None) extends WithId

final case class HttpCookie(name: String,
                            value: String,
                            expires: Option[Date] = None,
                            maxAge: Option[Long] = None,
                            domain: String,
                            path: Option[String] = None,
                            secure: Boolean = false,
                            httpOnly: Boolean = false,
                            extension: Option[String] = None,
                            id: Option[Long] = None) extends WithId

final case class Memory(name: String, data: Array[Byte])