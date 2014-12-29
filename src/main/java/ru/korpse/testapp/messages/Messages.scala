package ru.korpse.testapp.messages

import spray.json.JsValue

object Messages {
  sealed trait WebSocketClientMessage
  case object Connecting extends WebSocketClientMessage
  case class ConnectionFailed(reason: Option[Throwable] = None) extends WebSocketClientMessage
  case object Connected extends WebSocketClientMessage
  case class TextMessage(text: String) extends WebSocketClientMessage
  case class JsonMessage(text: JsValue) extends WebSocketClientMessage
  case class WriteFailed(message: String, reason: Option[Throwable]) extends WebSocketClientMessage
  case object Disconnecting extends WebSocketClientMessage
  case object Disconnected extends WebSocketClientMessage
  case class Error(th: Throwable) extends WebSocketClientMessage
}
