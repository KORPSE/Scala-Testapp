package ru.korpse.testapp

object Messages {
  sealed trait WebSocketClientMessage
  case object Connecting extends WebSocketClientMessage
  case class ConnectionFailed(client: SimpleWebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage
  case class Connected(client: SimpleWebSocketClient) extends WebSocketClientMessage
  case class TextMessage(client: SimpleWebSocketClient, text: String) extends WebSocketClientMessage
  case class WriteFailed(client: SimpleWebSocketClient, message: String, reason: Option[Throwable]) extends WebSocketClientMessage
  case object Disconnecting extends WebSocketClientMessage
  case class Disconnected(client: SimpleWebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage
  case class Error(client: SimpleWebSocketClient, th: Throwable) extends WebSocketClientMessage
}
