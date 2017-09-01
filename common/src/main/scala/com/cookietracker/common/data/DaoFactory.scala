package com.cookietracker.common.data

import com.cookietracker.common.concurrency.ThreadPoolExecutionContext
import com.cookietracker.common.database.PostgreSqlComponent

object DaoFactory {
  lazy val hostRelationDao = new HostRelationDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val urlDao = new UrlDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val httpCookieDao = new HttpCookieDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val memoryDao = new MemoryDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
}
