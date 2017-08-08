package com.cookietracker.common.data

import java.sql.Date

final case class WebHost(hostName: String, id: Option[Long] = None)

final case class HostRelation(fromHostId: Long, toHostId: Long, requestUrl: String, id: Option[Long] = None)

final case class Url(protocol: String, hostId: Long, port: Int, file: String, id: Option[Long] = None)

final case class HttpCookie(name: String,
                            value: String,
                            expires: Option[Date] = None,
                            maxAge: Option[Long] = None,
                            domain: Option[String] = None,
                            path: Option[String] = None,
                            secure: Boolean = false,
                            httpOnly: Boolean = false,
                            extension: Option[String] = None,
                            id: Option[Long] = None)
