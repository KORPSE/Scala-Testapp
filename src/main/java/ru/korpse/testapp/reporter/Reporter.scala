package ru.korpse.testapp.reporter

import ru.korpse.testapp.json.Trustline

trait Reporter {
  def report(trustline: Trustline)
}
