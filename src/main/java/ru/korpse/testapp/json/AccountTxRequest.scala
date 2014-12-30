package ru.korpse.testapp.json

case class AccountTxRequest(
    accounts: Array[String],
    id: String = "1",
    command: String = "subscribe",
    streams: Array[String] = Array("transactions"))