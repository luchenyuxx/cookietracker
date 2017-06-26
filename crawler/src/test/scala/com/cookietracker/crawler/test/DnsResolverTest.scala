package com.cookietracker.crawler.test

import com.cookietracker.crawler.{DnsResolve, DnsResolved, DnsResolver}

class DnsResolverTest extends AkkaTest{
  val dnsResolver = system.actorOf(DnsResolver.props)
  "A DnsResolver" must {
    "return None when bad host name" in {
      dnsResolver ! DnsResolve("-----")
      expectMsg(DnsResolved(None))
    }
    "resolve domain name (check the log)" in {
      dnsResolver ! DnsResolve("www.google.com")
      expectMsgClass(classOf[DnsResolved])
    }
  }
}