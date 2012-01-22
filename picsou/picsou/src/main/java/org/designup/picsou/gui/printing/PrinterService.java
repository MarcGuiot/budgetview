package org.designup.picsou.gui.printing;

import org.globsframework.utils.exceptions.OperationFailed;

public interface PrinterService {
  void print(String jobName, PrintableReport report) throws OperationFailed;
}
