package ru.korpse.testapp.json

case class AccountTxResult(transactions: Array[AccountTxTransaction], account: String, marker: Option[String])
