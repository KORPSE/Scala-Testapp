package ru.korpse.testapp.json.protocol

import ru.korpse.testapp.json.AccountTxTransaction
import spray.json.DefaultJsonProtocol
import ru.korpse.testapp.json.AccountTxResult
import ru.korpse.testapp.json.AccountTx
import ru.korpse.testapp.json.AccountTxTransactionTx
import ru.korpse.testapp.json.LimitAmount

object AccountTxProtocol extends DefaultJsonProtocol {
  implicit val limitAmountFormat = jsonFormat3(LimitAmount)
  implicit val accountTxTransactionTxFormat = jsonFormat(AccountTxTransactionTx, "TransactionType", "LimitAmount")
  implicit val accountTxTransactionFormat = jsonFormat1(AccountTxTransaction)
  implicit val accountTxResultFormat = jsonFormat3(AccountTxResult)
  implicit val accountTxFormat = jsonFormat1(AccountTx)
}
