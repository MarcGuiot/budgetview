package org.designup.picsou.functests.checkers;

import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.Trigger;
import org.uispec4j.Window;

public class BackupChecker {
  private OperationChecker operationsChecker;

  public BackupChecker(OperationChecker operationsChecker) {
    this.operationsChecker = operationsChecker;
  }

}
