package ru.korpse.testapp.util

import akka.actor.ActorLogging
import akka.actor.Actor

trait ReceiveLogger {
  this: Actor with ActorLogging =>

  def logMessage: Receive = new Receive {
    def isDefinedAt(x: Any) = {
      log.info(s"Got a $x")
      false
    }
    def apply(x: Any) = throw new UnsupportedOperationException
  }
} 