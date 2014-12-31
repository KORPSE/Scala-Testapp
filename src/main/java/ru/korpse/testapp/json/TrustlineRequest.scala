package ru.korpse.testapp.json

case class TrustlineRequest(
  account: String,
  id: String = "1",
  command: String = "account_lines",
  ledger: String = "current",
  marker: Option[String])
