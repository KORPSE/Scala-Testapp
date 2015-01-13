package ru.korpse.testapp.actors

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Actor, ActorLogging, actorRef2Scala}
import ru.korpse.testapp.json.protocol.{TrustlineRequestProtocol, TrustlineProtocol}
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.messages.ReplyMessages.DoSendMessage
import ru.korpse.testapp.messages.ReplyMessages.DoShutdown
import ru.korpse.testapp.reporter.ReporterComponent
import ru.korpse.testapp.util.{PropertiesAwared, ReceiveLogger}
import spray.json.pimpAny
import ru.korpse.testapp.json.{AccountLines, TrustlineRequest, Trustline}
import spray.json.DefaultJsonProtocol
import TrustlineProtocol._
import TrustlineRequestProtocol._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration

trait TrustlineActorComponent {
  this: ReporterComponent with PropertiesAwared =>

  val trustlineActorRef: ActorRef

  class TrustlineActor extends Actor with ActorLogging with ReceiveLogger with DefaultJsonProtocol {
    val account = props.getProperty("account")
    private def getTrustlinesMsg(account: String, marker: String = null) = {
      val req = TrustlineRequest(account = account, marker = Option(marker)).toJson.compactPrint
      sender ! DoSendMessage(req)
    }

    def receive: Receive = logMessage orElse {
      case Messages.Connected => {
        log.debug("Connection has been established")
        context.system.scheduler.schedule(Duration.Zero, Duration.create(10, TimeUnit.SECONDS), sender,
          DoSendMessage(TrustlineRequest(account = account, marker = None).toJson.compactPrint))
      }
      case Messages.Disconnected => {
        log.debug("The websocket disconnected.")
        sender ! DoShutdown
      }
      case Messages.JsonMessage(obj) => {
        if (obj.asJsObject.fields.contains("result")
          && obj.asJsObject.fields("result").asJsObject.fields.contains("lines")) {
          val accountLines = obj.convertTo[AccountLines];
          accountLines.result.lines.foreach(trustline => {
            reporter.report(trustline)
          })
          accountLines.result.marker.map(marker => getTrustlinesMsg(account, marker))
        }
      }
      case _ =>
    }
  }
}