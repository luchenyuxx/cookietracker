package com.cookietracker.common.data

import com.cookietracker.common.concurrency.ImplicitExecutionContext
import com.cookietracker.common.database.DBComponent
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

class FindEmptyIdException extends Exception("The object should have an id")

class InsertWithIdException extends Exception("The object to insert should not have an id")

trait DataAccess[T] {
  def insert(v: T): Future[T]

  def insert(vs: Seq[T]): Future[Seq[T]]

  def update(v: T): Future[Option[T]]

  def delete(v: T): Future[Int]

  def getAll: Future[Seq[T]]

  def getById(id: Long): Future[Option[T]]

  protected def withId[S](idOption: Option[Long])(f: Long => Future[S]): Future[S] = idOption match {
    case Some(i) => f(i)
    case None => Future.failed(new FindEmptyIdException)
  }

}

trait WebHostDataAccess extends DataAccess[WebHost] with WithWebHostTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: WebHost): Future[WebHost] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[WebHost]): Future[Seq[WebHost]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: WebHost): Future[Option[WebHost]] = withId(v.id) { i =>
    db.run(findById(i).update(v)).map {
      case 0 => None
      case _ => Some(v)
    }
  }

  override def delete(v: WebHost): Future[Int] = withId(v.id) { i => db.run(findById(i).delete) }

  override def getAll: Future[Seq[WebHost]] = db.run(webHostTableQuery.result)

  override def getById(id: Long): Future[Option[WebHost]] = db.run(findById(id).result.headOption)

  private val findById = Compiled((id: ConstColumn[Long]) => webHostTableQuery.filter(_.id === id))

  private def insertQueryReturningObject = webHostTableQuery.returning(webHostTableQuery.map(_.id)).into((w, i) => w.copy(id = Some(i)))

  def getByName(hostName: String): Future[Option[WebHost]] = db.run(webHostTableQuery.filter(_.hostname === hostName).result.headOption)

}

trait HttpCookieDataAccess extends DataAccess[HttpCookie] with WithHttpCookieTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: HttpCookie): Future[HttpCookie] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[HttpCookie]): Future[Seq[HttpCookie]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: HttpCookie): Future[Option[HttpCookie]] = withId(v.id) { i =>
    db.run(findById(i).update(v).map {
      case 0 => None
      case _ => Some(v)
    })
  }

  override def delete(v: HttpCookie): Future[Int] = withId(v.id) { i => db.run(findById(i).delete) }

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

  override def update(v: HostRelation): Future[Option[HostRelation]] = withId(v.id) { i =>
    db.run(findById(i).update(v).map {
      case 0 => None
      case _ => Some(v)
    })
  }

  override def getAll: Future[Seq[HostRelation]] = db.run(hostRelationTableQuery.result)

  override def delete(v: HostRelation): Future[Int] = withId(v.id) { i => db.run(findById(i).delete) }

  override def getById(id: Long): Future[Option[HostRelation]] = db.run(findById(id).result.headOption)

  private val findById = Compiled((id: ConstColumn[Long]) => hostRelationTableQuery.filter(_.id === id))

  private def insertQueryReturningObject = hostRelationTableQuery.returning(hostRelationTableQuery.map(_.id)).into((w, i) => w.copy(id = Some(i)))
}

trait UrlDataAccess extends DataAccess[Url] with WithUrlTable {
  this: DBComponent with ImplicitExecutionContext =>

  override def insert(v: Url): Future[Url] = if (v.id.isEmpty) db.run(insertQueryReturningObject += v) else Future.failed(new InsertWithIdException)

  override def insert(vs: Seq[Url]): Future[Seq[Url]] =
    if (vs.exists(_.id.isDefined)) Future.failed(new InsertWithIdException)
    else db.run((insertQueryReturningObject ++= vs).transactionally)

  override def update(v: Url): Future[Option[Url]] = withId(v.id) { i =>
    db.run(findById(i).update(v).map {
      case 0 => None
      case _ => Some(v)
    })
  }

  override def delete(v: Url): Future[Int] = withId(v.id) { i => db.run(findById(i).delete) }

  override def getAll: Future[Seq[Url]] = db.run(urlTableQuery.result)

  override def getById(id: Long): Future[Option[Url]] = db.run(findById(id).result.headOption)

  private val findById = Compiled((id: ConstColumn[Long]) => urlTableQuery.filter(_.id === id))

  private def insertQueryReturningObject = urlTableQuery.returning(urlTableQuery.map(_.id)).into((w, i) => w.copy(id = Some(i)))

}

