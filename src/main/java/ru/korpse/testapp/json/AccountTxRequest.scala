package ru.korpse.testapp.json

case class AccountTxRequest(
  account: String,
  id: String = "1",
  command: String = "account_tx",
  ledgerIndexMin: Int = -1,
  ledgerIndexMax: Int = -1,
  binary: Boolean = false,
  count: Boolean = false,
  forward: Boolean = false,
  marker: Option[String])