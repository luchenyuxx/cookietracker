package com.cookietracker.common.data

import java.util.Date

import org.mongodb.scala.bson.ObjectId

object WebHost {
  def apply(hostName: String, domain: Domain): WebHost = WebHost(new ObjectId, hostName, domain)
}

final case class Domain(_id: ObjectId, name: String)

final case class WebHost(_id: ObjectId, hostName: String, domain: Domain)

final case class HostRelation(from: WebHost, to: WebHost, requestUrl: String)

final case class Url(protocol: String, host: WebHost, port: Int, file: String)

final case class HttpCookie(name: String,
                            value: String,
                            expires: Option[Date] = None,
                            maxAge: Option[Long] = None,
                            domain: Option[Domain] = None,
                            path: Option[String] = None,
                            secure: Boolean = false,
                            httpOnly: Boolean = false,
                            extension: Option[String] = None,
                            fromUrl: Url)
