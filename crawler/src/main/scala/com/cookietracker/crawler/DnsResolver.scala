package com.cookietracker.crawler

import java.net.{InetAddress, URL}

import akka.actor.{Actor, Props}
import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

import scala.util.Try

/**
  * Caching DNS results to avoid querying DNS repeatedly
  *
  * {{{
  *   DnsResolve(url) ~> DnsResolved(newUrl)
  * }}}
  */
object DnsResolver {
  def props = Props(new DnsResolver)

  // Host name to IP address representation
  private val cache: LoadingCache[String, Option[String]] = CacheBuilder.newBuilder().maximumSize(10000).build(new CacheLoader[String, Option[String]] {
    override def load(key: String): Option[String] = Try(InetAddress.getByName(key)).map(_.getHostAddress).toOption
  })
}
class DnsResolver extends Actor{
  override def receive: Receive = {
    case DnsResolve(url) =>
      val address = DnsResolver.cache.get(url.getHost)
      val newUrl = address.map(a => (new URL(url.getProtocol, a, url.getPort, url.getFile)))
      sender() ! DnsResolved(newUrl)
  }
}
