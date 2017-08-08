package com.cookietracker.common.concurrency

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

trait ImplicitExecutionContext {
  implicit val executionContext: ExecutionContext
}

trait ThreadPoolExecutionContext extends ImplicitExecutionContext {
  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
}