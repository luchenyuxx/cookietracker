package com.cookietracker.crawler.test

import java.net.URL

import akka.testkit.TestActorRef
import com.cookietracker.crawler.{DnsResolve, DnsResolved, DnsResolver}

class DnsResolverTest extends AkkaTest{
  "A DnsResolver" must {
    "correctly resolve localhost" in {
      val dnsResolver = TestActorRef(DnsResolver.props)
      val url = new URL("http://localhost")
      dnsResolver ! DnsResolve(url)
      expectMsg(DnsResolved(Some(new URL("http://127.0.0.1"))))
    }
  }
}
