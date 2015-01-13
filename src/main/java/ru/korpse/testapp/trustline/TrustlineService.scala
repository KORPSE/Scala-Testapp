package ru.korpse.testapp.trustline

import spray.json.JsValue

trait TrustlineService {
  def connected
  def disconnected
  def connecting
  def disconnecting
  def processJson(obj: JsValue)
}
