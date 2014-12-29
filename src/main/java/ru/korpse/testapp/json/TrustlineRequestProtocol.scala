package ru.korpse.testapp.json

import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.RootJsonFormat

object TrustlineRequestProtocol extends DefaultJsonProtocol {
  implicit object TrustlineRequestJsonFormat extends RootJsonFormat[TrustlineRequest] {

    def write(req: TrustlineRequest) = {
      if (req.marker == null)
        JsObject(
          "account" -> JsString(req.account),
          "id" -> JsString(req.id),
          "command" -> JsString(req.command),
          "ledger" -> JsString(req.ledger))
      else
        JsObject(
          "account" -> JsString(req.account),
          "id" -> JsString(req.id),
          "command" -> JsString(req.command),
          "ledger" -> JsString(req.ledger),
          "marker" -> JsString(req.marker))
    }

    def read(value: JsValue) = {
      value.asJsObject.getFields("account", "id", "command", "ledger") match {
        case Seq(JsString(account), JsString(id), JsString(command), JsString(ledger)) =>
          TrustlineRequest(account, id, command, ledger)
        case _ => throw new DeserializationException("TrustlineRequest expected")
      }
    }

  }
}