package ru.korpse.actors

import akka.actor.Actor

class ClientActor extends Actor {
    def receive = {
      case "connect" => client.connect
      case "disconnect" => client.disconnect
      case "ping" => client.send("{\"id\":1,\"command\":\"ping\"}")
    }
  }
  