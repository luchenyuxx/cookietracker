package cookietracker

import akka.actor.{ActorSystem, PoisonPill}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object StartingPoint extends App {
  val system = ActorSystem()
  val supervisor = system.actorOf(Supervisor.props)

  supervisor ! Start("https://foat.me")

  Await.result(system.whenTerminated, 10 minutes)

  supervisor ! PoisonPill
  system.terminate
}
