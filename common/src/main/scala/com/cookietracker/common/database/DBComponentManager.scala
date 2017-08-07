package com.cookietracker.common.database

import slick.driver.{H2Driver, PostgresDriver}

trait PostgreSqlComponent extends DBComponent {
  val driver = PostgresDriver
  import driver.api._
  val db: Database = PostgreSql.connectionPool
}

private[database] object PostgreSql {
  import slick.driver.PostgresDriver.api._

  val connectionPool: Database = Database.forConfig("postgresDB")
}

trait H2Component extends DBComponent {
  val driver = H2Driver

  import driver.api._

  val db: Database = H2.connectionPool
}

private[database] object H2 {

  import slick.driver.H2Driver.api._

  val connectionPool: Database = Database.forConfig("h2mem")
}

object DBComponentManager {
}
