package com.cookietracker.common.data

import java.net.URL
import java.util.Date

import org.mongodb.scala.bson.ObjectId

object WebHost {
  def apply(hostName: String, domain: Domain): WebHost = WebHost(new ObjectId, hostName, domain)
}

final case class WebHost(_id: ObjectId, hostName: String, domain: Domain)

object Domain {
  def apply(name: String): Domain = new Domain(new ObjectId, name)
}

final case class Domain(_id: ObjectId, name: String)


final case class HostRelation(from: WebHost, to: WebHost, requestUrl: String)

object Url {
  def apply(protocol: String, host: WebHost, port: Int, file: String): Url = new Url(new ObjectId, protocol, host, port, file)
}
final case class Url(_id: ObjectId, protocol: String, host: WebHost, port: Int, file: String)

final case class HttpCookie(_id: ObjectId,
                            name: String,
                            value: String,
                            expires: Option[Date] = None,
                            maxAge: Option[Long] = None,
                            domain: Domain,
                            path: Option[String] = None,
                            secure: Boolean = false,
                            httpOnly: Boolean = false,
                            extension: Option[String] = None,
                            fromUrl: Url)
