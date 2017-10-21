package com.cookietracker.common.data

import com.cookietracker.common.concurrency.ImplicitExecutionContext
import com.cookietracker.common.database.slick._
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

trait SlickHttpCookieDataAccess extends HttpCookieDataAccess with WithHttpCookieTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: HttpCookie): Future[HttpCookie] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[HttpCookie]): Future[Seq[HttpCookie]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: HttpCookie): Future[Option[HttpCookie]] = withId(v) { (i, o) =>
    db.run(findById(i).update(o).map {
      case 0 => None
      case _ => Some(o)
    })
  }

  override def delete(v: HttpCookie): Future[Int] = withId(v) { (i, _) => db.run(findById(i).delete) }

  override def getAll: Future[Seq[HttpCookie]] = db.run(httpCookieTableQuery.result)

  override def getById(id: Long): Future[Option[HttpCookie]] = db.run(findById(id).result.headOption)

  private def insertQueryReturningObject = httpCookieTableQuery.returning(httpCookieTableQuery.map(_.id)).into((o, i) => o.copy(id = Some(i)))

  private val findById = Compiled((id: ConstColumn[Long]) => httpCookieTableQuery.filter(_.id === id))
}

trait SlickHostRelationDataAccess extends DataAccess[HostRelation] with WithHostRelationTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: HostRelation): Future[HostRelation] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[HostRelation]): Future[Seq[HostRelation]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: HostRelation): Future[Option[HostRelation]] = withId(v) { (i, o) =>
    db.run(findById(i).update(o).map {
      case 0 => None
      case _ => Some(o)
    })
  }

  override def getAll: Future[Seq[HostRelation]] = db.run(hostRelationTableQuery.result)

  override def delete(v: HostRelation): Future[Int] = withId(v) { (i, _) => db.run(findById(i).delete) }

  override def getById(id: Long): Future[Option[HostRelation]] = db.run(findById(id).result.headOption)

  def allRelationsFrom(fromHost: String): Future[Seq[HostRelation]] = db.run(hostRelationTableQuery.filter(_.fromHost === fromHost).result)

  private val findById = Compiled((id: ConstColumn[Long]) => hostRelationTableQuery.filter(_.id === id))

  private def insertQueryReturningObject = hostRelationTableQuery.returning(hostRelationTableQuery.map(_.id)).into((w, i) => w.copy(id = Some(i)))
}

trait SlickUrlDataAccess extends DataAccess[Url] with WithUrlTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: Url): Future[Url] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[Url]): Future[Seq[Url]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: Url): Future[Option[Url]] = withId(v) { (i, o) =>
    db.run(findById(i).update(o).map {
      case 0 => None
      case _ => Some(o)
    })
  }

  override def delete(v: Url): Future[Int] = withId(v) { (i, _) => db.run(findById(i).delete) }

  /**
    * @return Future of delete count
    */
  def deleteAll(): Future[Int] = db.run(urlTableQuery.delete)

  override def getAll: Future[Seq[Url]] = db.run(urlTableQuery.result)

  override def getById(id: Long): Future[Option[Url]] = db.run(findById(id).result.headOption)

  private val findById = Compiled((id: ConstColumn[Long]) => urlTableQuery.filter(_.id === id))

  private def insertQueryReturningObject = urlTableQuery.returning(urlTableQuery.map(_.id)).into((w, i) => w.copy(id = Some(i)))

}

trait SlickMemoryDataAccess extends MemoryDataAccess with WithMemoryTable {
  this: DBComponent with ImplicitExecutionContext =>
  def upsert(m: Memory): Future[Int] = db.run(memoryTableQuery.insertOrUpdate(m))

  def getByName(name: String): Future[Option[Memory]] = db.run(memoryTableQuery.filter(_.name === name).result.headOption)
}