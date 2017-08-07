package com.cookietracker.common.database

import slick.driver.JdbcProfile

trait DBComponent {
  protected val driver: JdbcProfile
  import driver.api._

  protected val db: Database
}
