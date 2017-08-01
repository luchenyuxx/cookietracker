package com.cookietracker.common.data

object DataAccessObject {

  def insert[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.insert(v)

  def insert[T](vs: Seq[T])(implicit dataAccess: DataAccess[T]) = dataAccess.insert(vs)

  def update[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.update(v)

  def delete[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.delete(v)

  def getAll[T](implicit dataAccess: DataAccess[T]): Seq[T] = dataAccess.getAll
}

sealed trait DataAccess[T] {
  def insert(v: T)

  def insert(vs: Seq[T])

  def update(v: T)

  def delete(v: T)

  def getAll: Seq[T]
}

object DataAccesses {

  implicit object WebHostDataAccess extends DataAccess[WebHost] {
    override def insert(v: WebHost): Unit = ???

    override def insert(vs: Seq[WebHost]): Unit = ???

    override def update(v: WebHost): Unit = ???

    override def delete(v: WebHost): Unit = ???

    override def getAll: Seq[WebHost] = ???
  }

  implicit object HttpCookieDataAccess extends DataAccess[HttpCookie] {
    override def insert(v: HttpCookie): Unit = ???

    override def insert(vs: Seq[HttpCookie]): Unit = ???

    override def update(v: HttpCookie): Unit = ???

    override def delete(v: HttpCookie): Unit = ???

    override def getAll: Seq[HttpCookie] = ???
  }

  implicit object HostRelationDataAccess extends DataAccess[HostRelation] {
    override def insert(v: HostRelation): Unit = ???

    override def insert(vs: Seq[HostRelation]): Unit = ???

    override def update(v: HostRelation): Unit = ???

    override def getAll: Seq[HostRelation] = ???

    override def delete(v: HostRelation): Unit = ???
  }

}

