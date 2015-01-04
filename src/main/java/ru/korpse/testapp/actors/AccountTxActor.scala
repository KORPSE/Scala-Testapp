package ru.korpse.testapp.actors

import akka.actor.{Actor, ActorLogging, actorRef2Scala}
import ru.korpse.testapp.json.{AccountTx, AccountTxRequest}
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.{DoSendMessage, DoShutdown}
import ru.korpse.testapp.util.ReceiveLogger
import ru.korpse.testapp.json.protocol.AccountTxProtocol._
import ru.korpse.testapp.json.protocol.AccountTxRequestProtocol._
import spray.json.{DefaultJsonProtocol, pimpAny}

class AccountTxActor(account: String) extends Actor with ActorLogging with ReceiveLogger with DefaultJsonProtocol {
  private def getSubscriptionMsg(account: String, marker: Option[String] = None) = {
    val req = AccountTxRequest(account = account, marker = marker).toJson.compactPrint
    sender ! DoSendMessage(req)
  }
  def receive: Receive = logMessage orElse {
    case Messages.Connected => {
      log.info("Connection has been established")
      getSubscriptionMsg(account)
    }
    case Messages.Disconnected => {
      log.info("The websocket disconnected.")
      sender ! DoShutdown
    }
    case Messages.JsonMessage(obj) => {
      try {
        if (obj.asJsObject.fields.contains("result")
            && obj.asJsObject.fields("result").asJsObject.fields.contains("transactions")) {
          val accountTx = obj.convertTo[AccountTx]
          accountTx.result.transactions.foreach (transaction => {
            transaction.tx.transactionType match {
              case Some("TrustSet") => {
                val limitAmount = transaction.tx.limitAmount
                log.info("\n===Limit amount===\n" +
                  "CUR: " + limitAmount.get.currency + "\n" +
                  "ISR: " + limitAmount.get.issuer + "\n" +
                  "VAL: " + limitAmount.get.value)
              }
              case _ =>
            }
          })
          accountTx.result.marker.map(marker => getSubscriptionMsg(account, Option(marker)))
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    case _ =>
  }
}
