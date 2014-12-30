package ru.korpse.testapp.json

import spray.json._

object AccountTxRequestProtocol extends DefaultJsonProtocol {
  implicit object AccountTxRequestJsonFormat extends RootJsonFormat[AccountTxRequest] {

    def write(req: AccountTxRequest) = {
      if (req.marker == null)
        JsObject(
          "account" -> JsString(req.account),
          "id" -> JsString(req.id),
          "command" -> JsString(req.command),
          "ledger_index_min" -> JsNumber(req.ledgerIndexMin),
          "ledger_index_max" -> JsNumber(req.ledgerIndexMax),
          "binary" -> JsBoolean(req.binary),
          "count" -> JsBoolean(req.count),
          "forward" -> JsBoolean(req.forward))
      else
        JsObject(
          "account" -> JsString(req.account),
          "id" -> JsString(req.id),
          "command" -> JsString(req.command),
          "ledger_index_min" -> JsNumber(req.ledgerIndexMin),
          "ledger_index_max" -> JsNumber(req.ledgerIndexMax),
          "binary" -> JsBoolean(req.binary),
          "count" -> JsBoolean(req.count),
          "forward" -> JsBoolean(req.forward),
          "marker" -> JsString(req.marker))
    }

    def read(value: JsValue) = {
      value.asJsObject.getFields("account", "id", "command", "ledger_index_min", "ledger_index_max", "binary",
          "count", "forward", "marker") match {
        case Seq(JsString(account), JsString(id), JsString(command), JsNumber(ledgerIndexMin), JsNumber(ledgerIndexMax),
            JsBoolean(binary), JsBoolean(count), JsBoolean(forward),
            JsString(marker)) =>
          AccountTxRequest(account, id, command, ledgerIndexMin.intValue(), ledgerIndexMax.intValue(),
              binary, count, forward, marker)
        case _ => throw new DeserializationException("TrustlineRequest expected")
      }
    }
  }
}