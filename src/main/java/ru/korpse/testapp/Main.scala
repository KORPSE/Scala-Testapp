package ru.korpse.testapp

import java.io.InputStream
import java.net.URI
import java.util.Properties
import akka.actor.ActorSystem
import akka.actor.Props
import org.slf4j.LoggerFactory
import ru.korpse.testapp.actors.{TrustlineActorComponent, SimpleWebSocketClientActor}
import ru.korpse.testapp.messages.ReplyMessages._
import ru.korpse.testapp.reporter.{ReporterImpl, ReporterComponent}
import ru.korpse.testapp.util.PropertiesAwared

object Main extends App with TrustlineActorComponent with ReporterComponent with PropertiesAwared {

  val system = ActorSystem("ClientActor")

  val reporter = new ReporterImpl
  val trustlineActorRef = system.actorOf(Props(new TrustlineActor), name = "TrustlineActor")

  val props: Properties = new Properties
  val in: InputStream = getClass().getResourceAsStream("/settings.properties")
  props.load(in)
  in.close()

  var client = system.actorOf(
      Props(new SimpleWebSocketClientActor(new URI("ws://s1.ripple.com:443"), Array(trustlineActorRef))),
        "ClientActor");

  client ! DoConnect
}