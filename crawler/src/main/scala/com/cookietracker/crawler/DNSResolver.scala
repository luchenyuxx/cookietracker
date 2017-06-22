package com.cookietracker.crawler

import akka.actor.{Actor, Props}

/**
  * Caching DNS results to avoid querying DNS repeatedly
  */
object DNSResolver {
  def props = Props(new DNSResolver)
}
class DNSResolver extends Actor{
  override def receive: Receive = ???
}
