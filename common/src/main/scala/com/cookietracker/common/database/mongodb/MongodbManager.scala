package com.cookietracker.common.database.mongodb

import com.cookietracker.common.data._
import org.mongodb.scala._
import org.mongodb.scala.connection._
import org.mongodb.scala.bson.codecs._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

object MongodbManager {
  val myadress = "mongodb://localhost"
  val myhost = "localhost"
  val dbname = "CookiesDB"

  //TODO Need to change not hard code
  val providers = List(Macros.createCodecProvider[WebHost](), Macros.createCodecProvider[HttpCookie](), Macros.createCodecProvider[HostRelation]())

  def getConnection(host: String, port: Int): MongoClient = {
    try {
      val connectionClusterSettings = ClusterSettings.builder().hosts(List(new ServerAddress(host)).asJava).build()
      val connectionSettings = MongoClientSettings.builder().clusterSettings(connectionClusterSettings).build()
      MongoClient(connectionSettings)
    } catch {
      case connectionError: MongoClientException => println("Connection Error"); null
      case unknown: Throwable => println(unknown); null
    }
  }

  def getDataBase(connectiondb: MongoClient, dbName: String): MongoDatabase = {
    try {
      val codecRegistry = fromRegistries(fromProviders(providers.asJava), DEFAULT_CODEC_REGISTRY)
      connectiondb.getDatabase(dbName).withCodecRegistry(codecRegistry)
    } catch {
      case getDatabaseError: MongoException => println("Get Database Error"); null
      case unknown: Throwable => println(unknown); null
    }
  }

  def getDBCollection[T:ClassTag](db: MongoDatabase, collectionName: String): MongoCollection[T] = {
    try {
      val dbNames: Seq[String] = List()
      db.listCollectionNames().filter(name => name.equals(collectionName)).foreach(name => dbNames.+:(name))
      if (dbNames.isEmpty)
        db.createCollection(collectionName)
      db.getCollection[T](collectionName)
    } catch {
      case getDBCollectionError: MongoException => println("Get DBCollection Error"); null
      case unknown: Throwable => println(unknown); null
    }
  }
}

class MongodbManager {

}
