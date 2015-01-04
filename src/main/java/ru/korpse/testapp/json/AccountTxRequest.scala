package ru.korpse.testapp.json

case class AccountTxRequest(
  account: String,
  id: String = "1",
  command: String = "account_tx",
  ledger_index_min: Int = -1,
  ledger_index_max: Int = -1,
  binary: Boolean = false,
  count: Boolean = false,
  forward: Boolean = false,
  marker: Option[String])