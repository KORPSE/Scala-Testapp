package ru.korpse.testapp

import java.net.URI
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import akka.actor.Props
import ch.qos.logback.core.util.StatusPrinter
import ru.korpse.testapp.actors.SimpleWebSocketClientActor
import ru.korpse.testapp.actors.TrustlineActor
import ru.korpse.testapp.messages.ReplyMessages._
import ch.qos.logback.classic.LoggerContext
import akka.event.Logging

object Main extends App {

  val system = ActorSystem("ClientActor")
  val trustlineActor = system.actorOf(Props(new TrustlineActor("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q")), name = "TrustlineActor")
  
  var client = system.actorOf(
      Props(new SimpleWebSocketClientActor(new URI("ws://s1.ripple.com:443"), trustlineActor)), "ClientActor");

  client ! DoConnect
}