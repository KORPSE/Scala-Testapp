package ru.korpse.testapp

import java.io.InputStream
import java.net.URI
import java.util.Properties

import akka.actor.{ActorSystem, TypedActor, TypedProps}
import ru.korpse.testapp.reporter.ReporterComponent
import ru.korpse.testapp.trustline.{TrustlineService, TrustlineServiceComponent}
import ru.korpse.testapp.util.PropertiesAwared
import ru.korpse.testapp.websocketclient.{SimpleWebSocketClient, SimpleWebSocketClientComponent}

object Main extends App with TrustlineServiceComponent with ReporterComponent with PropertiesAwared
    with SimpleWebSocketClientComponent {

  val system = ActorSystem("ClientActor")

  val reporter = new ReporterImpl
  val trustlineService = TypedActor(system).typedActorOf(
      TypedProps(classOf[TrustlineService], new TrustlineServiceImpl),
      "TrustlineActor")

  val props: Properties = new Properties
  val in: InputStream = getClass().getResourceAsStream("/settings.properties")
  props.load(in)
  in.close()

  val client: SimpleWebSocketClient = TypedActor(system).typedActorOf(
      TypedProps(classOf[SimpleWebSocketClient], new SimpleWebSocketClientImpl(new URI("ws://s1.ripple.com:443"))),
        "ClientActor")

  client.connect
}