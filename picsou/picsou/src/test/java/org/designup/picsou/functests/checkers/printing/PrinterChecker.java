package org.designup.picsou.functests.checkers.printing;

import org.designup.picsou.functests.checkers.utils.DummyPrinterService;

public class PrinterChecker {
  private DummyPrinterService printService;

  public PrinterChecker(DummyPrinterService printService) {
    this.printService = printService;
  }

  public BudgetReportChecker getBudgetReport() {
    return new BudgetReportChecker(printService.getLastReport());
  }
}
