package com.cookietracker.common.data

import java.sql.Date

import com.cookietracker.common.database.DBComponent
import slick.jdbc.meta.MTable
import slick.lifted._
import slick.profile.SqlProfile.ColumnOption.Nullable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

private[data] trait WithHostRelationTable {
  self: DBComponent =>
  import driver.api._

  protected[WithHostRelationTable] class HostRelationTable(tag: Tag) extends Table[HostRelation](tag, "host_relations") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def fromHost: Rep[String] = column[String]("from_host")

    def toHost: Rep[String] = column[String]("to_host")

    def url: Rep[String] = column[String]("request_url")

    override def * : ProvenShape[HostRelation] = (fromHost, toHost, url, id ?) <> (HostRelation.tupled, HostRelation.unapply)
  }

  protected val hostRelationTableQuery: TableQuery[HostRelationTable] = TableQuery[HostRelationTable]
}

private[data] trait WithHttpCookieTable {
  self: DBComponent =>

  import driver.api._

  protected[WithHttpCookieTable] class HttpCookieTable(tag: Tag) extends Table[HttpCookie](tag, "cookies") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def value: Rep[String] = column[String]("value")

    def expire: Rep[Date] = column[Date]("expired_date", Nullable)

    def maxAge: Rep[Long] = column[Long]("max_age", Nullable)

    def domain: Rep[String] = column[String]("domain")

    def path: Rep[String] = column[String]("path", Nullable)

    def secure: Rep[Boolean] = column[Boolean]("secure")

    def httpOnly: Rep[Boolean] = column[Boolean]("http_only")

    def extension: Rep[String] = column[String]("extension", Nullable)

    override def * : ProvenShape[HttpCookie] = (name, value, expire.?, maxAge.?, domain, path.?, secure, httpOnly, extension.?, id.?) <> (HttpCookie.tupled, HttpCookie.unapply)
  }

  protected val httpCookieTableQuery: TableQuery[HttpCookieTable] = TableQuery[HttpCookieTable]
}

private[data] trait WithUrlTable {
  self: DBComponent =>
  import driver.api._

  protected[WithUrlTable] class UrlTable(tag: Tag) extends Table[Url](tag, "urls_to_process") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def url: Rep[String] = column[String]("url")

    override def * : ProvenShape[Url] = (url, id.?) <> (Url.tupled, Url.unapply)
  }

  protected val urlTableQuery: TableQuery[UrlTable] = TableQuery[UrlTable]

  protected def urlTableAutoInc: driver.ReturningInsertActionComposer[Url, Long] = urlTableQuery returning urlTableQuery.map(_.id)
}

private[data] trait WithMemoryTable {
  self: DBComponent =>

  import driver.api._

  protected[WithMemoryTable] class MemoryTable(tag: Tag) extends Table[Memory](tag, "memory") {
    def name: Rep[String] = column[String]("name", O.PrimaryKey)

    def data: Rep[Array[Byte]] = column[Array[Byte]]("data")

    override def * : ProvenShape[Memory] = (name, data) <> (Memory.tupled, Memory.unapply)
  }

  protected val memoryTableQuery = TableQuery[MemoryTable]
}

trait SchemaChecker extends WithHostRelationTable with WithHttpCookieTable with WithUrlTable with WithMemoryTable {
  self: DBComponent =>

  import driver.api._

  def checkAndCreateTables(implicit ec: ExecutionContext): Unit = {
    val tables = Seq(hostRelationTableQuery, httpCookieTableQuery, urlTableQuery, memoryTableQuery)
    val existing = db.run(MTable.getTables)
    val f = existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables.filter(t => !names.contains(t.baseTableRow.tableName)).map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })
    Await.result(f, Duration.Inf)
  }
}