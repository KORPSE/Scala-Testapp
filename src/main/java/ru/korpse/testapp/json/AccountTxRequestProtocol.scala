package ru.korpse.testapp.json

import spray.json.DefaultJsonProtocol

object AccountTxRequestProtocol extends DefaultJsonProtocol {
  implicit val subscribeRequestFormat = jsonFormat4(AccountTxRequest)
}