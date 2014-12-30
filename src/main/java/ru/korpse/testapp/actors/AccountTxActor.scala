package ru.korpse.testapp.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import ru.korpse.testapp.json.LimitAmount
import ru.korpse.testapp.json.LimitAmountProtocol.StringJsonFormat
import ru.korpse.testapp.json.AccountTxRequest
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.util.ReceiveLogger
import spray.json.JsArray
import spray.json.JsValue
import spray.json.pimpAny
import ru.korpse.testapp.json.AccountTxRequest

class AccountTxActor(account: String) extends Actor with ActorLogging with ReceiveLogger {
  private def getSubscriptionMsg(account: String) = {
    import ru.korpse.testapp.json.AccountTxRequestProtocol._
    val req = AccountTxRequest(Array(account)).toJson.compactPrint
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
          val transactions = obj.asJsObject.fields("result").asJsObject.fields("transactions");
          import ru.korpse.testapp.json.LimitAmountProtocol._
          transactions match {
            case JsArray(list: Vector[JsValue]) =>
              list.foreach (transactionJs => {
                if (transactionJs.asJsObject.fields("tx").asJsObject
                    .fields("TransactionType").convertTo[String] == "TrustSet") {
                  val limitAmount = transactionJs.asJsObject.fields("tx").asJsObject
                    .fields("LimitAmount").convertTo[LimitAmount]
                  println("===Limit amount===\n")
                  println("CUR: " + limitAmount.currency)
                  println("ISR: " + limitAmount.issuer)
                  println("VAL: " + limitAmount.value)
                }
              })
            case _ => throw new RuntimeException("bad results")
          }
        }
      } catch {
        case e: Exception => e.printStackTrace() 
      }
    }
    case _ =>
  }
}
  