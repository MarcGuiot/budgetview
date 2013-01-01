package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class SynchroChecker extends GuiChecker {
  protected ImportDialogChecker importDialogChecker;
  protected final Window window;
  private final String importButtonName;

  public SynchroChecker(ImportDialogChecker importDialogChecker, Window window, String importButtonName) {
    this.importDialogChecker = importDialogChecker;
    this.window = window;
    this.importButtonName = importButtonName;
  }

  public ImportDialogChecker doImportAndWaitForCompletion() {
    doImport();
    importDialogChecker.checkLastStep();
    return importDialogChecker;
  }

  public ImportDialogChecker doImportAndWaitForPreview() {
    doImport();
    importDialogChecker.waitForPreview();
    return importDialogChecker;
  }

  private void doImport() {
    window.getButton(importButtonName).click();
    assertTrue(window.isVisible());
  }
}
