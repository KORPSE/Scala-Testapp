package ru.korpse.testapp

class WebSocketException(s: String, th: Throwable) extends java.io.IOException(s, th) {
  def this(s: String) = this(s, null)
}