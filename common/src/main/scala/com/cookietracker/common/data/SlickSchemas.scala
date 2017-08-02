package com.cookietracker.common.data

import java.sql.Date

import com.cookietracker.common.database.DBComponent
import slick.lifted.{ProvenShape, Tag}

private[data] trait WebHostTable { this: DBComponent =>
  import driver.api._

  protected[WebHostTable] class WebHostTable(tag: Tag) extends Table[WebHost](tag: Tag, "WebHosts") {
    def hostname = column[String]("HostName")
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    override def * : ProvenShape[WebHost] = (hostname, id.?) <> (WebHost.tupled, WebHost.unapply)
  }

  protected val webHostTableQuery = TableQuery[WebHostTable]
  protected def webHostTableAutoInc = webHostTableQuery returning webHostTableQuery.map(_.id)
}

private[data] trait HostRelationTable extends WebHostTable { this: DBComponent =>
  import driver.api._

  protected[HostRelationTable] class HostRelationTable(tag: Tag) extends Table[HostRelation](tag, "HostRelations") {
    def fromID = column[Long]("FromID")
    def fromK = foreignKey("HOST_FROM_FK", fromID, webHostTableQuery)(_.id)
    def toID = column[Long]("ToID")
    def toK = foreignKey("HOST_TO_FK", toID, webHostTableQuery)(_.id)
    def url = column[String]("RequestURL")
    def pk = primaryKey("HOST_PK", (fromID, toID))

    override def * : ProvenShape[HostRelation] = (fromID, toID, url) <> (HostRelation.tupled, HostRelation.unapply)
  }
  protected val hostRelationTableQuery = TableQuery[HostRelationTable]
}

private[data] trait HttpCookieTable extends WebHostTable { this: DBComponent =>
  import driver.api._

  protected[HttpCookieTable] class HttpCookieTable(tag: Tag) extends Table[HttpCookie](tag, "HttpCookies") {
    def name = column[String]("Name")
    def value = column[String]("Value")
    def expire = column[Date]("ExpiredDate")
    def maxAge = column[Long]("MaxAge")
    def domain = column[String]("Domain")
    def path = column[String]("Path")
    def secure = column[Boolean]("Secure")
    def httpOnly = column[Boolean]("HttpOnly")
    def extension = column[String]("Extension")
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    override def * : ProvenShape[HttpCookie] = (name, value, expire.?, maxAge.?, domain.?, path.?, secure, httpOnly, extension.?, id.?) <> (HttpCookie.tupled, HttpCookie.unapply)
  }

  protected val httpCookieTableQuery = TableQuery[HttpCookieTable]
  protected def httpCookieTableAutoInc = httpCookieTableQuery returning httpCookieTableQuery.map(_.id)
}

private[data] trait UrlTable extends WebHostTable { this: DBComponent =>
  import driver.api._

  protected[UrlTable] class UrlTable(tag: Tag) extends Table[Url](tag, "URLs") {
    def protocol = column[String]("Protocol")
    def hostId = column[Long]("HostID")
    def port = column[Int]("Port")
    def file = column[String]("File")
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def host = foreignKey("URL_FK", id, urlTableQuery)(_.id)
    override def * : ProvenShape[Url] = (protocol, hostId, port, file, id.?) <> (Url.tupled, Url.unapply)
  }

  protected val urlTableQuery = TableQuery[UrlTable]
  protected def urlTableAutoInc = urlTableQuery returning urlTableQuery.map(_.id)
}