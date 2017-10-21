package com.cookietracker.common.data

import com.cookietracker.common.concurrency.ThreadPoolExecutionContext
import com.cookietracker.common.database.slick.PostgreSqlComponent

object DaoFactory {
  lazy val hostRelationDao = new SlickHostRelationDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val urlDao = new SlickUrlDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val httpCookieDao = new SlickHttpCookieDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
  lazy val memoryDao = new SlickMemoryDataAccess with PostgreSqlComponent with ThreadPoolExecutionContext
}
