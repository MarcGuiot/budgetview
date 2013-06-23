package org.designup.picsou.functests.checkers.utils;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.PrinterService;
import org.globsframework.utils.exceptions.OperationFailed;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

public class DummyPrinterService implements PrinterService {
  private String jobName;
  private PrintableReport report;
  private String exceptionMessage;

  public void print(String jobName, PrintableReport report) throws OperationFailed {
    this.jobName = jobName;
    this.report = report;
    if (exceptionMessage != null) {
      throw new OperationFailed(exceptionMessage);
    }
  }

  public void setException(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public void checkLastReportName(String expectedName) {
    Assert.assertEquals(expectedName, jobName);
  }

  public PrintableReport getLastReport() {
    UISpecAssert.assertTrue(new Assertion() {
      public void check() {
        if (report == null) {
          throw new AssertionFailedError("No report was printed");
        }
      }
    });
    return report;
  }
}
