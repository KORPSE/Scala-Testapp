package ru.korpse.testapp.json

import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.RootJsonFormat

object TrustlineRequestProtocol extends DefaultJsonProtocol {
  implicit val trustlineRequestFormat = jsonFormat5(TrustlineRequest)
}