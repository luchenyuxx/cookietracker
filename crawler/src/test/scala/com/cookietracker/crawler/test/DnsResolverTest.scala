package com.cookietracker.crawler.test

import com.cookietracker.crawler.{DnsResolve, DnsResolved, DnsResolver}

class DnsResolverTest extends AkkaTest{
  "A DnsResolver" must {
    "return None when bad host name" in {
      val dnsResolver = system.actorOf(DnsResolver.props)
      dnsResolver ! DnsResolve("-----")
      expectMsg(DnsResolved(None))
    }
  }
}
