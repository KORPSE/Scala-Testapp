package ru.korpse.testapp.trustline

import akka.actor.TypedActor
import akka.event.Logging
import ru.korpse.testapp.json.protocol.TrustlineProtocol._
import ru.korpse.testapp.json.protocol.TrustlineRequestProtocol._
import ru.korpse.testapp.json.{AccountLines, TrustlineRequest}
import ru.korpse.testapp.reporter.ReporterComponent
import ru.korpse.testapp.util.PropertiesAwared
import ru.korpse.testapp.websocketclient.SimpleWebSocketClientComponent
import spray.json.{DefaultJsonProtocol, JsValue, pimpAny}

trait TrustlineServiceComponent {
  this: PropertiesAwared with SimpleWebSocketClientComponent with ReporterComponent =>
  def trustlineService: TrustlineService

  class TrustlineServiceImpl extends TrustlineService with DefaultJsonProtocol {
    val log = Logging(TypedActor.context.system, TypedActor.context.self)
    val account = props.getProperty("account")
    private def getTrustlinesMsg(account: String, marker: String = null) = {
      val req = TrustlineRequest(account = account, marker = Option(marker)).toJson.compactPrint
      client.send(req)
    }

    def connecting = log.debug("Connecting")

    def disconnecting = log.debug("Disconnecting")

    def connected = {
      log.debug("Connection has been established")
      val thread = new Thread(new Runnable {
        override def run(): Unit = {
          while (true) {
            client.send(TrustlineRequest(account = account, marker = None).toJson.compactPrint)
            Thread.sleep(10000)
          }
        }
      })
      thread.setDaemon(true)
      thread.start
    }

    def disconnected = {
      log.debug("The websocket disconnected.")
    }

    def processJson(obj: JsValue) = {
      if (obj.asJsObject.fields.contains("result")
        && obj.asJsObject.fields("result").asJsObject.fields.contains("lines")) {
        val accountLines = obj.convertTo[AccountLines];
        accountLines.result.lines.foreach(trustline => {
          reporter.report(trustline)
        })
        accountLines.result.marker.map(marker => getTrustlinesMsg(account, marker))
      }
    }
  }
}
