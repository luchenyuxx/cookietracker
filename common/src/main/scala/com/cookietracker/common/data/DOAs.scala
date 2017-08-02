package com.cookietracker.common.data

import com.cookietracker.common.database.{DBComponent, PostgreSqlComponent}
import slick.dbio.DBIOAction
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object DataAccessObject {
  import DataAccesses._

  def insert[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Long] = dataAccess.insert(v)

  def insert[T](vs: Seq[T])(implicit dataAccess: DataAccess[T]): Future[Seq[Long]] = dataAccess.insert(vs)

  def update[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Int] = dataAccess.update(v)

  def delete[T](v: T)(implicit dataAccess: DataAccess[T]): Future[Int] = dataAccess.delete(v)

  def getAll[T](implicit dataAccess: DataAccess[T]): Future[Seq[_]] = dataAccess.getAll
}

trait DataAccess[T] {
  def insert(v: T): Future[Long]

  def insert(vs: Seq[T]): Future[Seq[Long]]

  def update(v: T): Future[Int]

  def delete(v: T): Future[Int]

  def getAll: Future[Seq[_]]
}

object DataAccesses {
  implicit object WebHostDataAccess extends WebHostDataAccess with PostgreSqlComponent {
    val webHostSchema = db.run(DBIOAction.seq(webHostTableQuery.schema.create))
  }

  implicit object HostRelationDataAccess extends HostRelationDataAccess with PostgreSqlComponent {
    val hostRelationSchema = db.run(DBIOAction.seq(hostRelationTableQuery.schema.create))
  }

  implicit object HttpCookieDataAccess extends HttpCookieDataAccess with PostgreSqlComponent {
    val httpCookieSchema = db.run(DBIOAction.seq(httpCookieTableQuery.schema.create))
  }

  implicit object UrlDataAccess extends UrlDataAccess with PostgreSqlComponent {
    val urlSchema = db.run(DBIOAction.seq(urlTableQuery.schema.create))
  }

  trait WebHostDataAccess extends DataAccess[WebHost] with WebHostTable { this: DBComponent =>

    override def insert(v: WebHost): Future[Long] = db.run{webHostTableAutoInc += v}

    override def insert(vs: Seq[WebHost]): Future[Seq[Long]] = {
      var hostids = Seq[Long]()
      vs.foreach(v => {
        db.run(webHostTableAutoInc += v).onComplete{
          case Success(id) => hostids = hostids :+ id
          case Failure(e) => println(s"Failed to create webhost : ${v.hostName}")
        }
      })
      Future(hostids)
    }

    override def update(v: WebHost): Future[Int] = db.run(webHostTableQuery.filter(_.id === v.id.get).update(v))

    override def delete(v: WebHost): Future[Int] = db.run(webHostTableQuery.filter(_.id === v.id.get).delete)

    override def getAll: Future[Seq[WebHost]] = db.run(webHostTableQuery.to[Seq].result)
  }

  trait HttpCookieDataAccess extends DataAccess[HttpCookie] with HttpCookieTable { this: DBComponent =>

    override def insert(v: HttpCookie): Future[Long] = db.run(httpCookieTableAutoInc += v)

    override def insert(vs: Seq[HttpCookie]): Future[Seq[Long]] = {
      var cookieids = Seq[Long]()
      vs.foreach(v => {
        db.run(httpCookieTableAutoInc += v).onComplete {
          case Success(id) => cookieids = cookieids :+ id
          case Failure(e) => println(s"Failed to create httpcookie : ${v.name}")
        }
      })
      Future(cookieids)
    }

    override def update(v: HttpCookie): Future[Int] = db.run(httpCookieTableQuery.filter(_.id === v.id.get).update(v))

    override def delete(v: HttpCookie): Future[Int] = db.run(httpCookieTableQuery.filter(_.id === v.id.get).delete)

    override def getAll: Future[Seq[HttpCookie]] = db.run(httpCookieTableQuery.to[Seq].result)
  }

  trait HostRelationDataAccess extends DataAccess[HostRelation] with HostRelationTable { this: DBComponent =>

    //TODO Need To think for HostRelation return (id_from, id_to), same to insert(seq)
    override def insert(v: HostRelation): Future[Long] ={
      db.run(DBIOAction.seq(hostRelationTableQuery += v))
      Future(1)
    }

    override def insert(vs: Seq[HostRelation]): Future[Seq[Long]] = {
      vs.foreach(v => db.run(hostRelationTableQuery += v))
      Future(Seq[Long]())
    }

    override def update(v: HostRelation): Future[Int] = db.run(hostRelationTableQuery.filter(r => r.fromID === v.fromHostId && r.toID === v.toHostId).update(v))

    override def getAll: Future[Seq[HostRelation]] = db.run(hostRelationTableQuery.to[Seq].result)

    override def delete(v: HostRelation): Future[Int] = db.run(hostRelationTableQuery.filter(r => r.fromID === v.fromHostId && r.toID === v.toHostId).delete)
  }

  trait UrlDataAccess extends DataAccess[Url] with UrlTable { this: DBComponent =>

    override def insert(v: Url): Future[Long] = db.run(urlTableAutoInc += v)

    override def insert(vs: Seq[Url]): Future[Seq[Long]] = {
      var urls = Seq[Long]()
      vs.foreach(v => {
        db.run(urlTableAutoInc += v).onComplete {
          case Success(id) => urls = urls :+ id
          case Failure(e) => println(s"Failed to create url from host : ${v.hostId}")
        }
      })
      Future(urls)
    }

    override def update(v: Url): Future[Int] = db.run(urlTableQuery.filter(_.id === v.id.get).update(v))

    override def delete(v: Url): Future[Int] = db.run(urlTableQuery.filter(_.id === v.id.get).delete)

    override def getAll: Future[Seq[Url]] = db.run(urlTableQuery.to[Seq].result)
  }
}

