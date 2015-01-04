package ru.korpse.testapp.json.protocol

import ru.korpse.testapp.json.TrustlineRequest
import spray.json.DefaultJsonProtocol

object TrustlineRequestProtocol extends DefaultJsonProtocol {
  implicit val trustlineRequestFormat = jsonFormat5(TrustlineRequest)
}