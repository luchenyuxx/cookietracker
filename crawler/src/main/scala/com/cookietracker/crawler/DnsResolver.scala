package com.cookietracker.crawler

import java.net.URL

import akka.actor.{Actor, Props}
import akka.io.Dns

/**
  * Caching DNS results to avoid querying DNS repeatedly
  *
  * {{{
  *   DnsResolve(url) ~> DnsResolved(newUrl)
  * }}}
  */
object DnsResolver {
  def props = Props(new DnsResolver)
}
class DnsResolver extends Actor{
  override def receive: Receive = {
    case DnsResolve(url) =>
      val resolved = Dns.resolve(url.getHost)(context.system, self)
      val newUrl = resolved.flatMap(_.addrOption).map(_.getHostAddress).map(ip =>new URL(url.getProtocol, ip, url.getPort, url.getFile))
      sender() ! DnsResolved(newUrl)
  }
}
