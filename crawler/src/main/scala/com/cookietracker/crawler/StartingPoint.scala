package com.cookietracker.crawler

import java.net.URL
import java.util.concurrent.ConcurrentHashMap

import akka.NotUsed
import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.cookietracker.crawler.LinkExtractor.LinkContainer
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object StartingPoint extends App {

  import akka.http.scaladsl.Http
  import akka.stream.scaladsl._
  import akka.stream._

  val config = ConfigFactory.defaultApplication(ClassLoader.getSystemClassLoader)
  implicit val system = ActorSystem("cookie-tracker", config)
  val decider: Supervision.Decider = {
    case _ => Supervision.Resume
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )
  implicit val ec = system.dispatchers.lookup("akka.stream.default-blocking-io-dispatcher")

  val urlToRequest: Flow[URL, (URL, HttpRequest), NotUsed] = Flow[URL].map(u => {
    val requestHeaders = {
      val userAgentHeader: HttpHeader = headers.`User-Agent`("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
      List(userAgentHeader)
    }
    (u, HttpRequest(uri = Uri(u.toExternalForm), headers = requestHeaders))
  })

  val http = Http()
  val requestToResponse: Flow[(URL, HttpRequest), (URL, HttpResponse), NotUsed] = Flow[(URL, HttpRequest)].log("Fetching URL")
    .mapAsyncUnordered(1)(x => http.singleRequest(x._2).map((x._1, _)))
  val extractLink = Flow.fromGraph(new LinkExtractor())

  val crawlUrl = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    val workerCount = 8
    import GraphDSL.Implicits._
    val balance = builder.add(Balance[URL](workerCount))
    val mergeExtractResult = builder.add(Merge[(URL, LinkContainer)](workerCount))
    for (_ <- 1 to workerCount)
      balance ~> urlToRequest.async ~> requestToResponse.async ~> extractLink.async ~> mergeExtractResult
    FlowShape.of(balance.in, mergeExtractResult.out)
  })

  val urlFilter = Flow[URL].filter(u => !u.toExternalForm.endsWith(".jpg"))
  val killSwitch = KillSwitches.single[URL]

  val runnableGraph = RunnableGraph.fromGraph(GraphDSL.create(killSwitch) { implicit builder => sw =>
    import GraphDSL.Implicits._
    val dedup: FlowShape[URL, URL] = builder.add(UrlDeduplicator)
    val broadcast: UniformFanOutShape[(URL, LinkContainer), (URL, LinkContainer)] = builder.add(Broadcast[(URL, LinkContainer)](2))
    val relationSink: SinkShape[(URL, Seq[URL])] = builder.add(RelationManager.sink)
    val hostThrottler = builder.add(new Throttler(system.scheduler, system.dispatcher))
    val throttler = Flow[Option[URL]].throttle(1, 1 second, 1, ThrottleMode.Shaping).collect { case Some(u) => u }
    val merge = builder.add(MergePreferred[URL](1))

    UrlFrontier.source ~> urlFilter ~> merge ~> hostThrottler.in
    merge.preferred <~ throttler <~ hostThrottler.out1
    hostThrottler.out0 ~> sw ~> crawlUrl ~> broadcast
    broadcast.out(1) ~> Flow[(URL, LinkContainer)].mapConcat[URL](_._2.hrefLinks.to[collection.immutable.Iterable]) ~> urlFilter ~> dedup ~> UrlFrontier.sink
    broadcast.out(0) ~> Flow[(URL, LinkContainer)].map(x => (x._1, x._2.srcLinks)) ~> relationSink
    ClosedShape
  })

  val ks = runnableGraph.run()

  StdIn.readLine()
  ks.shutdown()
  system.terminate()
}

class Throttler(scheduler: Scheduler, implicit val ec: ExecutionContext) extends GraphStage[FanOutShape2[URL, URL, Option[URL]]] {
  val in = Inlet[URL]("url_in")
  val out = Outlet[URL]("url_out")
  val out2 = Outlet[Option[URL]]("throttle_url")
  val shape = new FanOutShape2[URL, URL, Option[URL]](in, out, out2)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val hostReady: ConcurrentHashMap[String, Boolean] = new ConcurrentHashMap()
    val urlByHost: mutable.Map[String, mutable.Queue[URL]] = mutable.Map()
    val urlQueue = mutable.Queue[URL]()
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        import scala.concurrent.duration._
        val url = grab(in)
        val host = url.getHost
        if (hostReady.getOrDefault(host, true)) {
          hostReady.put(host, false)
          scheduler.scheduleOnce(1 second)(hostReady.put(host, true))
          push(out, url)
        } else {
          urlQueue.enqueue(url)
          pull(in)
        }
      }
    })
    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })
    setHandler(out2, new OutHandler {
      override def onPull(): Unit = push(out2, urlQueue.dequeueFirst(_ => true))
    })
  }
}

class Accounting[T] extends GraphStage[FlowShape[T, T]] {
  val in = Inlet[T]("flow_in")
  val out = Outlet[T]("flow_out")
  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var count = 0
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        push(out, grab(in))
        count += 1
      }
    })
    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })
  }
}