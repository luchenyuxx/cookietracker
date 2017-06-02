package cookietracker

import akka.actor.Actor

import scala.io.StdIn

class InputListener extends Actor{
  override def receive: Receive = {
    case ReadyToProcess(url) =>
      println(s"InputListener: Ready to process $url, press return to proceed")
      StdIn.readLine()
      sender() ! Process(url)
  }
}
