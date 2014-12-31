package ru.korpse.testapp.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.util.ReceiveLogger
import spray.json.JsArray
import spray.json.JsValue
import spray.json.pimpAny
import ru.korpse.testapp.json.TrustlineRequest
import ru.korpse.testapp.json.Trustline
import spray.json.DefaultJsonProtocol

class TrustlineActor(account: String) extends Actor with ActorLogging with ReceiveLogger {
  private def getTrustlinesMsg(account: String, marker: String = null) = {
    import ru.korpse.testapp.json.TrustlineRequestProtocol._
    val req = TrustlineRequest(account = account, marker = Option(marker)).toJson.compactPrint
    sender ! DoSendMessage(req)
  }
  def receive: Receive = logMessage orElse {
    case Messages.Connected => {
      log.info("Connection has been established")
      getTrustlinesMsg(account)
    }
    case Messages.Disconnected => {
      log.info("The websocket disconnected.")
      sender ! DoShutdown
    }
    case Messages.JsonMessage(obj) => {
      try {
        if (obj.asJsObject.fields.contains("result")
            && obj.asJsObject.fields("result").asJsObject.fields.contains("lines")) {
          val lines = obj.asJsObject.fields("result").asJsObject.fields("lines");
          import ru.korpse.testapp.json.TrustlineProtocol._
          lines match {
            case JsArray(list: Vector[JsValue]) =>
              list.foreach (trustlineJs => {
                val trustline = trustlineJs.convertTo[Trustline]
                log.info("==Account line==\n" +
                    "CUR: " + trustline.currency + "\n" +
                    "VAL: " + trustline.balance)
              })
            case _ => throw new RuntimeException("bad results")
          }
          if (obj.asJsObject.fields("result").asJsObject.fields.contains("marker")) {
            import DefaultJsonProtocol._
            val marker = obj.asJsObject.fields("result").asJsObject.fields("marker").convertTo[String];
            val account = obj.asJsObject.fields("result").asJsObject.fields("account").convertTo[String];
            getTrustlinesMsg(account, marker)
          }
        }
      } catch {
        case e: Exception => e.printStackTrace() 
      }
    }
    case _ =>
  }
}
  