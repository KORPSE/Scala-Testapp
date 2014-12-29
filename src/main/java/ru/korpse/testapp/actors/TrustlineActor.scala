package ru.korpse.testapp.actors

import akka.actor.Actor
import ru.korpse.testapp.messages.Messages
import spray.json._
import spray.json.DefaultJsonProtocol._
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.messages.ReplyMessages.DoDisconnect
import ru.korpse.testapp.util.ReceiveLogger
import akka.actor.ActorLogging

class TrustlineActor(account: String) extends Actor with ActorLogging with ReceiveLogger {
  def receive: Receive = logMessage orElse {
    case Messages.Connected => {
      log.info("Connection has been established")
      object TrustlineRequestJsonProtocol extends DefaultJsonProtocol {
        implicit val trustlineRequestFormat = jsonFormat4(TrustlineRequest)
      }
      import TrustlineRequestJsonProtocol._
      val req = TrustlineRequest(account).toJson.compactPrint
      sender ! DoSendMessage(req)
    }
    case Messages.Disconnected => {
      log.info("The websocket disconnected.")
      sender ! DoShutdown
    }
    case Messages.JsonMessage(obj) => {
      try {
        val lines = obj.asJsObject.fields("result").asJsObject.fields("lines");
        lines match {
          case JsArray(list: Vector[JsValue]) =>
            list.foreach (trustline => {
              trustline.asJsObject.fields.foreach {
                case (fld: String, value: JsNumber) => println(fld + " -> " + value.convertTo[Int])
                case (fld: String, value: JsBoolean) => println(fld + " -> " + value.convertTo[Boolean])
                case (fld: String, value: Any) => println(fld + " -> " + value.convertTo[String])
              }
              println("==========\n")
            })
          case _ => throw new RuntimeException("bad results")
        }
      } catch {
        case e: Exception => e.printStackTrace() 
      }
      
      //client send ("ECHO: " + message)
      //sender ! DoDisconnect
    }
    case _ =>
  }
  
  case class TrustlineRequest(account: String, id: String = "1", command: String = "account_lines", ledger: String = "current")

}
  