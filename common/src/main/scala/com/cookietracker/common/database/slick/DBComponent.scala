package com.cookietracker.common.database.slick

import slick.driver.JdbcProfile

trait DBComponent {
  protected val driver: JdbcProfile
  import driver.api._

  protected val db: Database
}
