package com.cookietracker.crawler.test

import java.net.URL

import akka.actor.ActorRef
import com.cookietracker.crawler.{DnsResolve, DnsResolved, DnsResolver}

class DnsResolverTest extends AkkaTest{
  val dnsResolver: ActorRef = system.actorOf(DnsResolver.props, "dns-resolver")
  "A DnsResolver" must {
    "return None when bad host name" in {
      val url = new URL("http://nonexisthost")
      dnsResolver ! DnsResolve(url)
      expectMsg(DnsResolved(url, None))
    }
    "resolve domain name (check the log)" in {
      dnsResolver ! DnsResolve(new URL("http://www.google.com"))
      expectMsgClass(classOf[DnsResolved])
    }
  }
}