package com.cookietracker.common.data

import java.sql.Date

import com.cookietracker.common.database.DBComponent
import slick.jdbc.meta.MTable
import slick.lifted.{ForeignKeyQuery, PrimaryKey, ProvenShape}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

private[data] trait WithWebHostTable {
  self: DBComponent =>
  import driver.api._

  protected[WithWebHostTable] class WebHostTable(tag: Tag) extends Table[WebHost](tag: Tag, "WebHosts") {
    def hostname: Rep[String] = column[String]("HostName")

    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    override def * : ProvenShape[WebHost] = (hostname, id.?) <> (WebHost.tupled, WebHost.unapply)
  }

  protected val webHostTableQuery: TableQuery[WebHostTable] = TableQuery[WebHostTable]

  protected def webHostTableAutoInc: driver.ReturningInsertActionComposer[WebHost, Long] = webHostTableQuery returning webHostTableQuery.map(_.id)
}

private[data] trait WithHostRelationTable extends WithWebHostTable {
  self: DBComponent =>
  import driver.api._

  protected[WithHostRelationTable] class HostRelationTable(tag: Tag) extends Table[HostRelation](tag, "HostRelations") {
    def fromID: Rep[Long] = column[Long]("FromID")

    def fromK: ForeignKeyQuery[WebHostTable, WebHost] = foreignKey("HOST_FROM_FK", fromID, webHostTableQuery)(_.id)

    def toID: Rep[Long] = column[Long]("ToID")

    def toK: ForeignKeyQuery[WebHostTable, WebHost] = foreignKey("HOST_TO_FK", toID, webHostTableQuery)(_.id)

    def url: Rep[String] = column[String]("RequestURL")

    def pk: PrimaryKey = primaryKey("HOST_PK", (fromID, toID))

    override def * : ProvenShape[HostRelation] = (fromID, toID, url) <> (HostRelation.tupled, HostRelation.unapply)
  }

  protected val hostRelationTableQuery: TableQuery[HostRelationTable] = TableQuery[HostRelationTable]
}

private[data] trait WithHttpCookieTable extends WithWebHostTable {
  self: DBComponent =>
  import driver.api._

  protected[WithHttpCookieTable] class HttpCookieTable(tag: Tag) extends Table[HttpCookie](tag, "HttpCookies") {
    def name: Rep[String] = column[String]("Name")

    def value: Rep[String] = column[String]("Value")

    def expire: Rep[Date] = column[Date]("ExpiredDate")

    def maxAge: Rep[Long] = column[Long]("MaxAge")

    def domain: Rep[String] = column[String]("Domain")

    def path: Rep[String] = column[String]("Path")

    def secure: Rep[Boolean] = column[Boolean]("Secure")

    def httpOnly: Rep[Boolean] = column[Boolean]("HttpOnly")

    def extension: Rep[String] = column[String]("Extension")

    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    override def * : ProvenShape[HttpCookie] = (name, value, expire.?, maxAge.?, domain.?, path.?, secure, httpOnly, extension.?, id.?) <> (HttpCookie.tupled, HttpCookie.unapply)
  }

  protected val httpCookieTableQuery: TableQuery[HttpCookieTable] = TableQuery[HttpCookieTable]

  protected def httpCookieTableAutoInc: driver.ReturningInsertActionComposer[HttpCookie, Long] = httpCookieTableQuery returning httpCookieTableQuery.map(_.id)
}

private[data] trait WithUrlTable extends WithWebHostTable {
  self: DBComponent =>
  import driver.api._

  protected[WithUrlTable] class UrlTable(tag: Tag) extends Table[Url](tag, "URLs") {
    def protocol: Rep[String] = column[String]("Protocol")

    def hostId: Rep[Long] = column[Long]("HostID")

    def port: Rep[Int] = column[Int]("Port")

    def file: Rep[String] = column[String]("File")

    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def host: ForeignKeyQuery[UrlTable, Url] = foreignKey("URL_FK", id, urlTableQuery)(_.id)
    override def * : ProvenShape[Url] = (protocol, hostId, port, file, id.?) <> (Url.tupled, Url.unapply)
  }

  protected val urlTableQuery: TableQuery[UrlTable] = TableQuery[UrlTable]

  protected def urlTableAutoInc: driver.ReturningInsertActionComposer[Url, Long] = urlTableQuery returning urlTableQuery.map(_.id)
}

trait SchemaChecker extends WithHostRelationTable with WithWebHostTable with WithHttpCookieTable with WithUrlTable {
  self: DBComponent =>

  import driver.api._

  def checkAndCreateTables(implicit ec: ExecutionContext): Unit = {
    val tables = Seq(webHostTableQuery, hostRelationTableQuery, httpCookieTableQuery, urlTableQuery)
    val existing = db.run(MTable.getTables)
    val f = existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables.filter(t => !names.contains(t.baseTableRow.tableName)).map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })
    Await.result(f, Duration.Inf)
  }
}