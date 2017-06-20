package com.cookietracker.crawler

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

trait HaveLogger { self: Actor =>
  lazy val logger: LoggingAdapter = Logging.getLogger(context.system, this)
}
