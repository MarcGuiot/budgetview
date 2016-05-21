package com.budgetview.functests.checkers.printing;

import com.budgetview.functests.checkers.utils.DummyPrinterService;

public class PrinterChecker {
  private DummyPrinterService printService;

  public PrinterChecker(DummyPrinterService printService) {
    this.printService = printService;
  }

  public BudgetReportChecker getBudgetReport() {
    return new BudgetReportChecker(printService.getLastReport());
  }

  public TransactionPrintChecker getTransactions() {
    return new TransactionPrintChecker(printService.getLastReport());
  }

  public void setException(String exceptionMessage) {
    printService.setException(exceptionMessage);
  }
}
