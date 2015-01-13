package ru.korpse.testapp.json.protocol

import ru.korpse.testapp.json.{AccountTx, AccountTxResult, AccountTxTransaction, AccountTxTransactionTx, LimitAmount}
import spray.json.DefaultJsonProtocol

object AccountTxProtocol extends DefaultJsonProtocol {
  implicit val limitAmountFormat = jsonFormat3(LimitAmount)
  implicit val accountTxTransactionTxFormat = jsonFormat(AccountTxTransactionTx, "TransactionType", "LimitAmount")
  implicit val accountTxTransactionFormat = jsonFormat1(AccountTxTransaction)
  implicit val accountTxResultFormat = jsonFormat3(AccountTxResult)
  implicit val accountTxFormat = jsonFormat1(AccountTx)
}
