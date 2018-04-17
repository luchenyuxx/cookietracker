package com.cookietracker.common

import java.net.URL

import com.redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DAO {
  def popUrl(implicit ex: ExecutionContext): Future[Option[URL]]

  def addUrl(s: URL)(implicit ex: ExecutionContext): Future[Long]

  def upsert(m: Memory)(implicit ex: ExecutionContext): Future[Boolean]

  def getByName(name: String)(implicit ex: ExecutionContext): Future[Option[Memory]]

  def addRelation(fromHost: String, toHost: String)(implicit ex: ExecutionContext): Future[Boolean]
}

object DAO {
  def apply(): DAO = new RedisDAO {}
}

trait RedisDAO extends DAO {
  val URL_KEY = "url_to_process"
  val MEMORY_PREFIX = "memory_"
  val FROM_HOST_PREFIX = "from_host_"
  val TO_HOST_PREFIX = "to_host_"
  val HOST_KEY = "hosts"
  lazy val redisClient = new RedisClient("localhost", 6379)

  import com.redis.serialization.Parse.Implicits.parseByteArray

  override def popUrl(implicit ex: ExecutionContext): Future[Option[URL]] = Future(redisClient.spop[String](URL_KEY).map(new URL(_)))

  override def addUrl(s: URL)(implicit ex: ExecutionContext): Future[Long] = Future(redisClient.sadd(URL_KEY, s.toExternalForm).get)

  override def upsert(m: Memory)(implicit ex: ExecutionContext): Future[Boolean] = Future(redisClient.set(MEMORY_PREFIX + m.name, m.data))

  override def getByName(name: String)(implicit ex: ExecutionContext): Future[Option[Memory]] = Future(redisClient.get[Array[Byte]](MEMORY_PREFIX + name).map(Memory(name, _)))

  override def addRelation(fromHost: String, toHost: String)(implicit ex: ExecutionContext): Future[Boolean] = Future {
    Try {
      redisClient.sadd(HOST_KEY, fromHost, toHost)
      redisClient.sadd(FROM_HOST_PREFIX + fromHost, toHost)
      redisClient.sadd(TO_HOST_PREFIX + toHost, fromHost)
      true
    }.getOrElse(false)
  }
}