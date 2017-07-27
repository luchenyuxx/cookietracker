package com.cookietracker.common.data

import com.cookietracker.common.database.mongodb.MongodbManager
import org.mongodb.scala.model.Filters._
import org.mongodb.scala._
import org.mongodb.scala.model.Updates._

import scala.reflect.ClassTag

object DataAccessObject {
  import DataAccesses._

  def insert[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.insert(v)

  def insert[T](vs: Seq[T])(implicit dataAccess: DataAccess[T]) = dataAccess.insert(vs)

  def update[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.update(v)

  def delete[T](v: T)(implicit dataAccess: DataAccess[T]) = dataAccess.delete(v)

  def getAll[T](implicit dataAccess: DataAccess[T]): Seq[T] = dataAccess.getAll
}

trait DataAccess[T] {
  def insert(v: T)

  def insert(vs: Seq[T])

  def update(v: T)

  def delete(v: T)

  def getAll: Seq[T]
}

object DataAccesses {
  val connection: MongoClient = MongodbManager.getConnection(MongodbManager.myhost, 27017)
  val dataBase: MongoDatabase = MongodbManager.getDataBase(connection, MongodbManager.dbname)

  implicit object WebHostDataAccess extends DataAccess[WebHost] {
    val webHostCollection: MongoCollection[WebHost] = MongodbManager.getDBCollection[WebHost](dataBase, "WebHosts")

    override def insert(v: WebHost): Unit = webHostCollection.insertOne(v)

    override def insert(vs: Seq[WebHost]): Unit = webHostCollection.insertMany(vs)

    override def update(v: WebHost): Unit = webHostCollection.updateOne(equal("_id", v._id), set("hostName", v.hostName))

    override def delete(v: WebHost): Unit = webHostCollection.deleteOne(equal("_id", v._id))

    override def getAll: Seq[WebHost] = {
      val webhosts = Seq()
      for (webhost <- webHostCollection.find())
        webhost +: webhosts
      webhosts
    }
  }

  implicit object HttpCookieDataAccess extends DataAccess[HttpCookie] {
    val httpCookieCollection: MongoCollection[HttpCookie] = MongodbManager.getDBCollection[HttpCookie](dataBase, "HttpCookies")

    override def insert(v: HttpCookie): Unit = {
      httpCookieCollection.find(equal("_id", v._id)).first()
      httpCookieCollection.insertOne(v)
    }

    override def insert(vs: Seq[HttpCookie]): Unit = httpCookieCollection.insertMany(vs)

    override def update(v: HttpCookie): Unit = httpCookieCollection.updateOne(equal("_id", v._id),
      combine(set("name", v.name), set("value", v.value), set("expireDate", v.expires), set("maxAge", v.maxAge),
        set("domain", v.domain), set("path", v.path), set("secure", v.secure), set("httpOnly", v.httpOnly),
        set("extension", v.extension), set("_fromHostId", v.fromHost._id)))

    override def delete(v: HttpCookie): Unit = httpCookieCollection.deleteOne(equal("_id", v._id))

    override def getAll: Seq[HttpCookie] = {
      val httpcookies = Seq()
      for (cookie <- httpCookieCollection.find())
        cookie +: httpcookies
      httpcookies
    }
  }

  implicit object HostRelationDataAccess extends DataAccess[HostRelation] {
    val hostRelationCollection: MongoCollection[HostRelation] = MongodbManager.getDBCollection[HostRelation](dataBase, "HostRelations")

    override def insert(v: HostRelation): Unit = hostRelationCollection.insertOne(v)

    override def insert(vs: Seq[HostRelation]): Unit = hostRelationCollection.insertMany(vs)

    override def update(v: HostRelation): Unit = hostRelationCollection.updateOne(and(equal("_fromHostId", v.from), equal("_toHostId", v.to)),
      set("requestUrl", v.requestUrl))

    override def getAll: Seq[HostRelation] = {
      val hostrelations = Seq()
      for (hostrelation <- hostRelationCollection.find())
        hostrelation +: hostrelations
      hostrelations
    }

    override def delete(v: HostRelation): Unit = hostRelationCollection.deleteOne(and(equal("_fromHostId", v.from), equal("_toHostId", v.to)))
  }
}

