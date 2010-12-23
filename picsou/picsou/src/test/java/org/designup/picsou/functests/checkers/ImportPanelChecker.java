package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ImportPanelChecker extends ViewChecker {
  private Panel panel;

  public ImportPanelChecker(Window window) {
    super(window);
  }

  public ImportDialogChecker openImport() {
    return ImportDialogChecker.open(getPanel().getButton("Import").triggerClick());
  }

  public ImportPanelChecker checkImportMessage(String message) {
    assertThat(getPanel().getButton().textEquals(message));
    return this;
  }

  public ImportPanelChecker checkImportSignpostDisplayed(String message) {
    views.selectData();
    checkSignpostVisible(getPanel(), getPanel().getButton(), message);
    return this;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectData();
      panel = mainWindow.getPanel("importPanel");
    }
    return panel;
  }
}