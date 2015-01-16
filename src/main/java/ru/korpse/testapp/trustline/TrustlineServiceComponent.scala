package ru.korpse.testapp.trustline

import java.util.concurrent.TimeUnit

import akka.actor.TypedActor
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import ru.korpse.testapp.json.protocol.TrustlineProtocol._
import ru.korpse.testapp.json.protocol.TrustlineRequestProtocol._
import ru.korpse.testapp.json.{AccountLines, TrustlineRequest}
import ru.korpse.testapp.reporter.ReporterComponent
import ru.korpse.testapp.websocketclient.SimpleWebSocketClientComponent
import spray.json.{DefaultJsonProtocol, JsValue, pimpAny}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait TrustlineServiceComponent {
  this: SimpleWebSocketClientComponent with ReporterComponent =>
  val trustlineService: TrustlineService

  class TrustlineServiceImpl extends TrustlineService with DefaultJsonProtocol {
    val log = Logging(TypedActor.context.system, TypedActor.context.self)
    val config = ConfigFactory.load().getConfig("testapp")
    val account = config.getString("account")
    val issuer = config.getString("issuer")
    private def getTrustlinesMsg(account: String, marker: String = null) = {
      val req = TrustlineRequest(account = account, marker = Option(marker)).toJson.compactPrint
      client.send(req)
    }

    def connecting = log.debug("Connecting")

    def disconnecting = log.debug("Disconnecting")

    def connected = {
      log.debug("Connection has been established")
      val system = TypedActor.context.system
      system.scheduler.schedule(Duration.Zero, 10 seconds, new Runnable() {
        override def run(): Unit = {
            client.send(TrustlineRequest(account = account, marker = None).toJson.compactPrint)
        }
      })
    }

    def disconnected = {
      log.debug("The websocket disconnected.")
    }

    def processJson(obj: JsValue) = {
      if (obj.asJsObject.fields.contains("result")
        && obj.asJsObject.fields("result").asJsObject.fields.contains("lines")) {
        val accountLines = obj.convertTo[AccountLines];
        accountLines.result.lines.foreach(line =>
          if (line.account == issuer) reporter.report(line))
        accountLines.result.marker.map(marker => getTrustlinesMsg(account, marker))
      }
    }
  }
}
