package ru.korpse.testapp

import java.net.URI

import akka.actor.{ActorSystem, TypedActor, TypedProps}
import ru.korpse.testapp.reporter.ReporterComponent
import ru.korpse.testapp.trustline.{TrustlineService, TrustlineServiceComponent}
import ru.korpse.testapp.websocketclient.{SimpleWebSocketClient, SimpleWebSocketClientComponent}

object Main extends App with TrustlineServiceComponent with ReporterComponent
    with SimpleWebSocketClientComponent {

  val system = ActorSystem("ClientActor")

  val reporter = new ReporterImpl
  val trustlineService = TypedActor(system).typedActorOf(
      TypedProps(classOf[TrustlineService], new TrustlineServiceImpl),
      "TrustlineActor")

  val client: SimpleWebSocketClient = TypedActor(system).typedActorOf(
      TypedProps(classOf[SimpleWebSocketClient], new SimpleWebSocketClientProxy(new URI("ws://s1.ripple.com:443"))),
        "ClientActor")

  client.connect
}