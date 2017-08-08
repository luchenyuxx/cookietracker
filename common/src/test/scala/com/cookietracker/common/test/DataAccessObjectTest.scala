package com.cookietracker.common.test

import com.cookietracker.common.concurrency.ThreadPoolExecutionContext
import com.cookietracker.common.data.{SchemaChecker, WebHost, WebHostDataAccess}
import com.cookietracker.common.database.H2Component
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}

import scala.concurrent.ExecutionContext.Implicits.global

class DataAccessObjectTest extends AsyncFlatSpec with BeforeAndAfterAll {

  val schemaChecker = new SchemaChecker with H2Component

  override protected def beforeAll(): Unit = {
    schemaChecker.checkAndCreateTables(global)
  }

  val webHostDao = new WebHostDataAccess with H2Component with ThreadPoolExecutionContext

  "DataAccessObject" can "insert a WebHost without id to database then return the object with id" in {
    val w = WebHost("www.test1.com")
    val f = webHostDao.insert(w)
    f.map(l => assert(l.id.isDefined))
  }

  it should "return error when inserting a WebHost with id" in {
    val w = WebHost("www.test2.com", Some(11))
    val f = webHostDao.insert(w)
    f.map(l => assert(false)).recover { case _ => assert(true) }
  }

  it can "update a WebHost with id in database and return the updated object with id" in {
    val w = WebHost("www.test3.com")
    val f = webHostDao.insert(w)
    f.flatMap { l =>
      val nw = l.copy(hostName = "www.test3updated.com")
      webHostDao.update(nw).map(nl => (nw, nl))
    }.map(l => assert(l._1.equals(l._2.get)))
  }
  it should "return error when updating a WebHost without id" in {
    val w = WebHost("www.test4.com")
    val f = webHostDao.update(w)
    f.map(l => assert(false)).recover { case _ => assert(true) }
  }
  it should "return None when updating a WebHost with non existing id" in {
    val w = WebHost("www.test6.com", Some(1000000))
    val f = webHostDao.update(w)
    f.map(l => assert(l.isEmpty))
  }
  it can "delete a WebHost with id in database and return affected line number" in {
    val w = WebHost("www.test5.com")
    val f = webHostDao.insert(w)
    f.flatMap { l =>
      val nw = WebHost("www.test5.com", Some(l.id.get))
      webHostDao.delete(nw)
    }.map(l => assert(l == 1))
  }
  it should "return error when deleting a WebHost without id" in {
    val w = WebHost("www.test8.com")
    val f = webHostDao.delete(w)
    f.map(l => assert(false)).recover { case _ => assert(true) }
  }
}
