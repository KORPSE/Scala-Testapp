package ru.korpse.testapp.json.protocol

import ru.korpse.testapp.json.AccountTxRequest
import spray.json._

object AccountTxRequestProtocol extends DefaultJsonProtocol {
  implicit val accountTxRequestFormat = jsonFormat9(AccountTxRequest)
}