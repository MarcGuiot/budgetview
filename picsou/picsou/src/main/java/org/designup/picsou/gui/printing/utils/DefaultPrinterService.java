package org.designup.picsou.gui.printing.utils;

import org.designup.picsou.gui.printing.PrinterService;
import org.designup.picsou.gui.printing.PrintableReport;
import org.globsframework.utils.exceptions.OperationFailed;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class DefaultPrinterService implements PrinterService {
  public void print(String jobName, PrintableReport report) throws OperationFailed {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setJobName(jobName);
    report.initFormat(printJob.defaultPage());
    printJob.setPageable(report);
    if (printJob.printDialog()) {
      try {
        printJob.print();
      }
      catch (PrinterException exc) {
        throw new OperationFailed(exc);
      }
    }
  }
}
