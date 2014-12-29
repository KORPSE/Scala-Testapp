package ru.korpse.testapp.json

import spray.json.DefaultJsonProtocol

object TrustlineProtocol extends DefaultJsonProtocol {
  implicit val trustlineFormat = jsonFormat7(Trustline)
}