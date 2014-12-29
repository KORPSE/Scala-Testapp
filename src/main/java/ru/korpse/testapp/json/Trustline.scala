package ru.korpse.testapp.json

case class Trustline(
  account: String,
  balance: String,
  currency: String,
  limit: String,
  limit_peer: String,
  quality_in: Int,
  quality_out: Int)
