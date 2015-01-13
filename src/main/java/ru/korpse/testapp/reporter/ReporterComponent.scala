package ru.korpse.testapp.reporter

import org.slf4j.LoggerFactory
import ru.korpse.testapp.json.Trustline

trait ReporterComponent {
  val reporter: Reporter

  class ReporterImpl extends Reporter {
    val log = LoggerFactory.getLogger("testapp." + getClass.getName)
    def report(trustline: Trustline) = {
      log.info("==Account line==\n" +
        "CUR: " + trustline.currency + "\n" +
        "VAL: " + trustline.balance)
    }
  }
}
