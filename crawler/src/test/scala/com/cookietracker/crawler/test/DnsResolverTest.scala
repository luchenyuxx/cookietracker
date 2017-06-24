package com.cookietracker.crawler.test

import java.net.URL

import com.cookietracker.crawler.{DnsResolve, DnsResolved, DnsResolver}

class DnsResolverTest extends AkkaTest{
  "A DnsResolver" must {
    "correctly resolve localhost" in {
      val dnsResolver = system.actorOf(DnsResolver.props)
      val url = new URL("http://www.google.com")
      println(url.getHost)
      dnsResolver ! DnsResolve(url)
      expectMsg(DnsResolved(Some(new URL("http://216.58.211.100"))))
    }
  }
}
