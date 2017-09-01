package com.cookietracker.common.data

import com.cookietracker.common.concurrency.ImplicitExecutionContext
import com.cookietracker.common.database.DBComponent
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

trait DataAccess[T <: WithId] {
  /**
    * Insert an object without id to database
    * @param v the object to insert, it should not have an id
    * @return A future containing the object with its id
    */
  def insert(v: T): Future[T]

  /**
    * Insert a sequence of objects without id to database
    * @param vs the objects to insert, they should not have ids
    * @return A future containing the objects with their ids in database
    */
  def insert(vs: Seq[T]): Future[Seq[T]]

  /**
    * Update an object in database. The object should indicate its id.
    * @param v the object to update, it should have its id in database.
    * @return A future containing: 1) Some(v) if update succeed. 2) None if there is no object with such id in database.
    */
  def update(v: T): Future[Option[T]]

  /**
    * Delete an object in database. The object should indicate its id.
    * @param v the object to delete, it should have its id in database.
    * @return
    */
  def delete(v: T): Future[Int]

  def getAll: Future[Seq[T]]

  def getById(id: Long): Future[Option[T]]

  protected def withId[S](v: T)(f: (Long, T) => Future[S]): Future[S] = v.id match {
    case Some(i) => f(i, v)
    case None => Future.failed(new FindEmptyIdException)
  }

}

trait HttpCookieDataAccess extends DataAccess[HttpCookie] with WithHttpCookieTable {
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

trait HostRelationDataAccess extends DataAccess[HostRelation] with WithHostRelationTable {
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

trait UrlDataAccess extends DataAccess[Url] with WithUrlTable {
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

trait MemoryDataAccess extends WithMemoryTable {
  this: DBComponent with ImplicitExecutionContext =>
  def upsert(m: Memory): Future[Int] = db.run(memoryTableQuery.insertOrUpdate(m))

  def getByName(name: String): Future[Option[Memory]] = db.run(memoryTableQuery.filter(_.name === name).result.headOption)
}