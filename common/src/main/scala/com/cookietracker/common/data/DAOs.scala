package com.cookietracker.common.data

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

trait HttpCookieDataAccess extends DataAccess[HttpCookie]

trait HostRelationDataAccess extends DataAccess[HostRelation]

trait MemoryDataAccess {
  def upsert(m: Memory): Future[Int]

  def getByName(name: String): Future[Option[Memory]]
}



