package ru.korpse.testapp

import java.net.URI

object Main extends App {

  var client = new SimpleWebSocketClient(new URI("ws://s1.ripple.com:443"))({
    case Messages.Connected(client) => {
      println("Connection has been established")
      client.send("{\"id\":1,\"command\":\"ping\"}")
    }

    case Messages.Disconnected(client, _) => println("The websocket disconnected.")
    case Messages.TextMessage(client, message) => {
      println("RECV: " + message)
      //client send ("ECHO: " + message)
      client.disconnect
    }
    case _ =>
  })
  client.connect
}