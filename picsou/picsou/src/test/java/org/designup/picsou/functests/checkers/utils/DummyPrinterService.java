package org.designup.picsou.functests.checkers.utils;

import junit.framework.Assert;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.PrinterService;
import org.globsframework.utils.exceptions.OperationFailed;

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
    if (report == null) {
      Assert.fail("No report was printed");
    }
    return report;
  }
}
