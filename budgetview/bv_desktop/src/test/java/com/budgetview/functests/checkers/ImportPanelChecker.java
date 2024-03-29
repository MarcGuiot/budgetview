package com.budgetview.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class ImportPanelChecker extends ViewChecker {
  private Panel panel;

  public ImportPanelChecker(Window window) {
    super(window);
  }

  public ImportDialogChecker openImport() {
    return ImportDialogChecker.open(getPanel().getButton("importFile").triggerClick());
  }

  public ImportPanelChecker checkImportSignpostDisplayed(String message) {
    views.selectData();
    checkSignpostVisible(mainWindow, getPanel().getButton("importFile"), message);
    return this;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectData();
      panel = mainWindow.getPanel("timeviewHeader");
    }
    return panel;
  }
}