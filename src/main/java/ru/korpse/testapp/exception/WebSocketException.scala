package ru.korpse.testapp.exception

class WebSocketException(s: String, th: Throwable) extends java.io.IOException(s, th) {
  def this(s: String) = this(s, null)
}