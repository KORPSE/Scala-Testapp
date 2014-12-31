package ru.korpse.testapp.json

import ru.korpse.testapp.json.TrustlineRequestProtocol._
import spray.json._

object AccountTxRequestProtocol extends DefaultJsonProtocol {
  implicit val accountTxRequestFormat = jsonFormat9(AccountTxRequest)
}