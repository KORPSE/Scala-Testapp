package ru.korpse.testapp.json.protocol

import ru.korpse.testapp.json.{AccountLinesResult, AccountLines, Trustline}
import spray.json.DefaultJsonProtocol

object TrustlineProtocol extends DefaultJsonProtocol {
  implicit val trustlineFormat = jsonFormat7(Trustline)
  implicit val accountLinesResultFormat = jsonFormat3(AccountLinesResult)
  implicit val accountLinesFormat = jsonFormat1(AccountLines)
}