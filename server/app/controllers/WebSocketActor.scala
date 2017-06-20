package controllers

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.JsValue

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor{
  override def receive: Receive = {
    case msg: JsValue =>
      out ! msg
  }

//  lazy val cookieTrackerSupervisor: ActorRef = context.actorOf(Supervisor.props(self))
  // Do something when stop
  override def postStop(): Unit = super.postStop()
}
