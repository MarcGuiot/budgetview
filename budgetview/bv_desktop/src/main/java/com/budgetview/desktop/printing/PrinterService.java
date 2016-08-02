package com.budgetview.desktop.printing;

import org.globsframework.utils.exceptions.OperationFailed;

public interface PrinterService {
  void print(String jobName, PrintableReport report) throws OperationFailed;
}
