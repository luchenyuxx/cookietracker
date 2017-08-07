package com.cookietracker.common.data

import com.cookietracker.common.database.{DBComponent, PostgreSqlComponent}
import slick.dbio.DBIOAction
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DataAccessObject {
  def insert[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Long] = dataAccess.insert(v)

  def insert[T](vs: Seq[T])(implicit dataAccess: DataAccess[T]): Future[Seq[Long]] = dataAccess.insert(vs)

  def update[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Int] = dataAccess.update(v)

  def delete[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Int] = dataAccess.delete(v)

  def getAll[T](implicit dataAccess: DataAccess[T]): Future[Seq[T]] = dataAccess.getAll

  def getData[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Option[T]] = dataAccess.getData(v)
}

trait DataAccess[T] {
  def insert(v: T): Future[Long]

  def insert(vs: Seq[T]): Future[Seq[Long]]

  def update(v: T): Future[Int]

  def delete(v: T): Future[Int]

  def getAll: Future[Seq[T]]

  def getData(v: T): Future[Option[T]]
}

object DataAccesses {
  implicit object WebHostDataAccess extends WebHostDataAccess with PostgreSqlComponent {
    //Check if exist table
    db.run(webHostTableQuery.schema.create)
    /*val a = db.run(DBIOAction.seq(
      MTable.getTables.map(tables => {
        if (!tables.exists(_.name.name == webHostTableQuery.baseTableRow.tableName))
          webHostTableQuery.schema.create
      })
    ))*/
  }

  implicit object HostRelationDataAccess extends HostRelationDataAccess with PostgreSqlComponent {
    db.run(hostRelationTableQuery.schema.create)
    /*db.run(DBIOAction.seq(
      MTable.getTables.map(tables => {
        if (!tables.exists(_.name.name == hostRelationTableQuery.baseTableRow.tableName))
          hostRelationTableQuery.schema.create
      })
    ))*/
  }

  implicit object HttpCookieDataAccess extends HttpCookieDataAccess with PostgreSqlComponent {
    db.run(DBIOAction.seq(
      MTable.getTables.map(tables => {
        if (!tables.exists(_.name.name == httpCookieTableQuery.baseTableRow.tableName))
          httpCookieTableQuery.schema.create
      })
    ))
  }

  implicit object UrlDataAccess extends UrlDataAccess with PostgreSqlComponent {
    db.run(DBIOAction.seq(
      MTable.getTables.map(tables => {
        if (!tables.exists(_.name.name == urlTableQuery.baseTableRow.tableName))
          urlTableQuery.schema.create
      })
    ))
  }

  trait WebHostDataAccess extends DataAccess[WebHost] with WithWebHostTable {
    this: DBComponent =>

    override def insert(v: WebHost): Future[Long] = db.run((webHostTableAutoInc += v).transactionally)

    override def insert(vs: Seq[WebHost]): Future[Seq[Long]] = db.run((webHostTableAutoInc ++= vs).transactionally)

    override def update(v: WebHost): Future[Int] = db.run(webHostTableQuery.filter(_.id === v.id.get).update(v).transactionally)

    override def delete(v: WebHost): Future[Int] = db.run(webHostTableQuery.filter(_.id === v.id.get).delete.transactionally)

    override def getAll: Future[Seq[WebHost]] = db.run(webHostTableQuery.to[Seq].result.transactionally)

    override def getData(v: WebHost): Future[Option[WebHost]] = v.id match {
      case Some(_id) => getWebHostBy(_id)
      case _ => getWebHostBy(v.hostName)
    }

    def getWebHostBy(hostName: String): Future[Option[WebHost]] = db.run(webHostTableQuery.filter(_.hostname === hostName).result.headOption.transactionally)

    def getWebHostBy(id: Long): Future[Option[WebHost]] = db.run(webHostTableQuery.filter(_.id === id).result.headOption.transactionally)
  }

  trait HttpCookieDataAccess extends DataAccess[HttpCookie] with WithHttpCookieTable {
    this: DBComponent =>

    override def insert(v: HttpCookie): Future[Long] = db.run((httpCookieTableAutoInc += v).transactionally)

    override def insert(vs: Seq[HttpCookie]): Future[Seq[Long]] = db.run((httpCookieTableAutoInc ++= vs).transactionally)

    override def update(v: HttpCookie): Future[Int] = db.run(httpCookieTableQuery.filter(_.id === v.id.get).update(v).transactionally)

    override def delete(v: HttpCookie): Future[Int] = db.run(httpCookieTableQuery.filter(_.id === v.id.get).delete.transactionally)

    override def getAll: Future[Seq[HttpCookie]] = db.run(httpCookieTableQuery.to[Seq].result.transactionally)

    override def getData(v: HttpCookie): Future[Option[HttpCookie]] = v.id match {
      case Some(_id) => getHttpCookieBy(_id)
      case _ => Future(None)
    }

    def getHttpCookieBy(id: Long): Future[Option[HttpCookie]] = db.run(httpCookieTableQuery.filter(_.id === id).result.headOption.transactionally)
  }

  trait HostRelationDataAccess extends DataAccess[HostRelation] with WithHostRelationTable {
    this: DBComponent =>

    override def insert(v: HostRelation): Future[Long] ={
      db.run((hostRelationTableQuery += v).transactionally)
      // Just return get from host id
      Future{v.fromHostId}
    }

    override def insert(vs: Seq[HostRelation]): Future[Seq[Long]] = {
      db.run((hostRelationTableQuery ++= vs).transactionally)
      Future{vs.map(_.fromHostId)}
    }

    override def update(v: HostRelation): Future[Int] = db.run(hostRelationTableQuery.filter(r => r.fromID === v.fromHostId && r.toID === v.toHostId).update(v).transactionally)

    override def getAll: Future[Seq[HostRelation]] = db.run(hostRelationTableQuery.to[Seq].result.transactionally)

    override def delete(v: HostRelation): Future[Int] = db.run(hostRelationTableQuery.filter(r => r.fromID === v.fromHostId && r.toID === v.toHostId).delete.transactionally)

    override def getData(v: HostRelation): Future[Option[HostRelation]] = getHostRelationBy(v.fromHostId, v.toHostId)

    def getHostRelationBy(fromid: Long, toid: Long): Future[Option[HostRelation]] = db.run(hostRelationTableQuery.filter(r => r.fromID === fromid && r.toID === toid).result.headOption.transactionally)
  }

  trait UrlDataAccess extends DataAccess[Url] with WithUrlTable {
    this: DBComponent =>

    override def insert(v: Url): Future[Long] = db.run((urlTableAutoInc += v).transactionally)

    override def insert(vs: Seq[Url]): Future[Seq[Long]] = db.run((urlTableAutoInc ++= vs).transactionally)

    override def update(v: Url): Future[Int] = db.run(urlTableQuery.filter(_.id === v.id.get).update(v).transactionally)

    override def delete(v: Url): Future[Int] = db.run(urlTableQuery.filter(_.id === v.id.get).delete.transactionally)

    override def getAll: Future[Seq[Url]] = db.run(urlTableQuery.to[Seq].result.transactionally)

    override def getData(v: Url): Future[Option[Url]] = v.id match {
      case Some(_id) => getUrlBy(_id)
      case _ => Future(None)
    }

    def getUrlBy(id: Long): Future[Option[Url]] = db.run(urlTableQuery.filter(_.id === id).result.headOption.transactionally)
  }
}

