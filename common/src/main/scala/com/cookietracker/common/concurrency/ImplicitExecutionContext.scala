package com.cookietracker.common.concurrency

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

trait ImplicitExecutionContext {
  implicit val executionContext: ExecutionContext
}

/**
  * All subclass will share a same execution context based on a Java thread pool
  * executor
  */
trait ThreadPoolExecutionContext extends ImplicitExecutionContext {
  implicit val executionContext: ExecutionContext = ThreadPoolExecutionContext.ec
}

object ThreadPoolExecutionContext {
  private val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
}