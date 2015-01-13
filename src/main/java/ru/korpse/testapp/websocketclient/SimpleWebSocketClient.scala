package ru.korpse.testapp.websocketclient

import java.net.URI

trait SimpleWebSocketClient {
  val url: URI
  def connect
  def disconnect
  def send(msg: String)
}
