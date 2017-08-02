package com.cookietracker.common.database

import slick.driver.PostgresDriver

trait PostgreSqlComponent extends DBComponent {
  val driver = PostgresDriver
  import driver.api._
  val db: Database = PostgreSql.connectionPool
}

private[database] object PostgreSql {
  import slick.driver.PostgresDriver.api._

  val connectionPool = Database.forConfig("postgresDB")
}

object DBComponentManager {
}
