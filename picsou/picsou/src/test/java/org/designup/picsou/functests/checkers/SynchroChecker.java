package org.designup.picsou.functests.checkers;

import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.Window;

public class SynchroChecker extends GuiChecker{
  protected ImportDialogChecker importDialogChecker;
  protected Window window;
  private String importName;

  public SynchroChecker(ImportDialogChecker importDialogChecker, Window window, String importName) {
    this.importDialogChecker = importDialogChecker;
    this.window = window;
    this.importName = importName;
  }

  public ImportDialogChecker doImport() {
    if (importDialogChecker != null) {
      window.getButton(importName).click();
      UISpecAssert.assertFalse(window.isVisible());
      importDialogChecker.waitAcceptFiles();
    }
    else {
      importDialogChecker =
        ImportDialogChecker.openInStep2(window.getButton("update").triggerClick());
      UISpecAssert.assertFalse(window.isVisible());
    }
    return importDialogChecker;
  }
}
