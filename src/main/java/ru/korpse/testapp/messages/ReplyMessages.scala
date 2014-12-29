package ru.korpse.testapp.messages

object ReplyMessages {
  sealed trait ReplyMessage
  case object DoConnect extends ReplyMessage
  case class DoSendMessage(msg: String) extends ReplyMessage
  case object DoDisconnect extends ReplyMessage
  case object DoShutdown extends ReplyMessage
}