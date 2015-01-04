package ru.korpse.testapp.json

case class AccountLinesResult(lines: Array[Trustline], account: String, marker: Option[String])
