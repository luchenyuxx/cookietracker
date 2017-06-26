package com.cookietracker.crawler

import java.net.InetAddress
import java.util
import java.util.Collections

import akka.actor.{Actor, ActorLogging, Props}
import com.google.common.cache.{Cache, CacheBuilder}

import scala.util.{Failure, Success, Try}

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
  private val cache: Cache[String, InetAddress] = CacheBuilder.newBuilder().maximumSize(10000).build[String, InetAddress]()
  private val badHostNames: util.Collection[String] = Collections.synchronizedCollection(new util.HashSet[String]())
}

class DnsResolver extends Actor with ActorLogging {
  override def receive: Receive = {
    case DnsResolve(hostName) =>
      if (DnsResolver.badHostNames.contains(hostName)) {
        log.info(s"Bad host name: $hostName")
        sender() ! DnsResolved(None)
      }
      else
        Option(DnsResolver.cache.getIfPresent(hostName)) match {
          case Some(a) =>
            log.info(s"Resolved from cache: $hostName -> ${a.getHostAddress}")
            sender() ! DnsResolved(Some(a))
          case None =>
            log.info(s"Host name $hostName not in cache, trying to resolve it.")
            Try(InetAddress.getByName(hostName)) match {
              case Success(i) =>
                DnsResolver.cache.put(hostName, i)
                sender() ! DnsResolved(Some(i))
              case Failure(e) =>
                log.error(s"Can't not resolve host name $hostName", e)
                DnsResolver.badHostNames.add(hostName)
                sender() ! DnsResolved(None)
            }
        }
  }
}
