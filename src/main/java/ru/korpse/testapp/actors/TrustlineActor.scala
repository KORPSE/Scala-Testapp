package ru.korpse.testapp.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import ru.korpse.testapp.json.protocol.{TrustlineRequestProtocol, TrustlineProtocol}
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.util.ReceiveLogger
import spray.json.pimpAny
import ru.korpse.testapp.json.{AccountLines, TrustlineRequest, Trustline}
import spray.json.DefaultJsonProtocol
import TrustlineProtocol._
import TrustlineRequestProtocol._

class TrustlineActor(account: String) extends Actor with ActorLogging with ReceiveLogger with DefaultJsonProtocol {
  private def getTrustlinesMsg(account: String, marker: String = null) = {
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
      if (obj.asJsObject.fields.contains("result")
          && obj.asJsObject.fields("result").asJsObject.fields.contains("lines")) {
        val accountLines = obj.convertTo[AccountLines];
        accountLines.result.lines.foreach (trustline => {
          log.info("==Account line==\n" +
              "CUR: " + trustline.currency + "\n" +
              "VAL: " + trustline.balance)
        })
        accountLines.result.marker.map(marker => getTrustlinesMsg(account, marker))
      }
    }
    case _ =>
  }
}
  