package com.cookietracker.common.data

import java.util.Date

import org.mongodb.scala.bson.ObjectId

object WebHost {
  def apply(hostName: String): WebHost = WebHost(new ObjectId, hostName)
}

final case class WebHost(_id: ObjectId, hostName: String)

final case class HostRelation(from: WebHost, to: WebHost, requestUrl: String)

final case class HttpCookie(_id: ObjectId,
                            name: String,
                            value: String,
                            expires: Option[Date] = None,
                            maxAge: Option[Long] = None,
                            domain: Option[String] = None,
                            path: Option[String] = None,
                            secure: Boolean = false,
                            httpOnly: Boolean = false,
                            extension: Option[String] = None,
                            fromHost: WebHost)