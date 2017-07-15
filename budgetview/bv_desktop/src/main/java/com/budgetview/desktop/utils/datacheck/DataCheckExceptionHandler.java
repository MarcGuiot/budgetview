package com.budgetview.desktop.utils.datacheck;

import org.globsframework.utils.exceptions.ExceptionHandler;

public class DataCheckExceptionHandler implements ExceptionHandler {

  private DataCheckReport report;

  public DataCheckExceptionHandler(DataCheckReport report) {
    this.report = report;
  }

  public void onException(Throwable ex) {
    report.addError(ex);
  }

  public void setFirstReset(boolean firstReset) {

  }
}
