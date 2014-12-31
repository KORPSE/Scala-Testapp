package ru.korpse.testapp.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import ru.korpse.testapp.json.LimitAmount
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.util.ReceiveLogger
import spray.json.{DefaultJsonProtocol, JsArray, JsValue, pimpAny}
import ru.korpse.testapp.json.AccountTxRequest

class AccountTxActor(account: String) extends Actor with ActorLogging with ReceiveLogger {
  private def getSubscriptionMsg(account: String, marker: String = null) = {
    import ru.korpse.testapp.json.AccountTxRequestProtocol._
    val req = AccountTxRequest(account = account, marker = Option(marker)).toJson.compactPrint
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
          val transactions = obj.asJsObject.fields("result").asJsObject.fields("transactions")
          import ru.korpse.testapp.json.LimitAmountProtocol._
          transactions match {
            case JsArray(list: Vector[JsValue]) =>
              list.foreach (transactionJs => {
                if (transactionJs.asJsObject.fields("tx").asJsObject
                    .fields("TransactionType").convertTo[String] == "TrustSet") {
                  val limitAmount = transactionJs.asJsObject.fields("tx").asJsObject
                    .fields("LimitAmount").convertTo[LimitAmount]
                  log.info("\n===Limit amount===\n" +
                      "CUR: " + limitAmount.currency + "\n" +
                      "ISR: " + limitAmount.issuer + "\n" +
                      "VAL: " + limitAmount.value)
                }
              })
            case _ => throw new RuntimeException("bad results")
          }
          if (obj.asJsObject.fields("result").asJsObject.fields.contains("marker")) {
            import DefaultJsonProtocol._
            val marker = obj.asJsObject.fields("result").asJsObject.fields("marker").convertTo[String]
            val account = obj.asJsObject.fields("result").asJsObject.fields("account").convertTo[String]
            getSubscriptionMsg(account, marker)
          }

        }
      } catch {
        case e: Exception => e.printStackTrace() 
      }
    }
    case _ =>
  }
}
  