package ru.korpse.testapp.json

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import spray.json.JsValue

object LimitAmountProtocol extends DefaultJsonProtocol {
  implicit val subscribeRequestFormat = jsonFormat3(LimitAmount)
}