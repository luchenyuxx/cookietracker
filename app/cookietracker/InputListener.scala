package cookietracker

import akka.actor.{Actor, Props}

import scala.io.StdIn

object InputListener {
  def props = Props(new InputListener)
}

class InputListener extends Actor{
  override def receive: Receive = {
    case ReadyToProcess(url) =>
      println(s"InputListener: Ready to process $url, press return to proceed")
      StdIn.readLine()
      sender() ! Process(url)
  }
}
