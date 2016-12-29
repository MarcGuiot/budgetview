package com.budgetview.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudEditionChecker extends ViewChecker {

  public CloudEditionChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudSignupPanel");
  }

  public CloudBankSelectionChecker addConnection() {
    Button button = mainWindow.getButton("addConnection");
    assertThat(button.isEnabled());
    button.click();
    return new CloudBankSelectionChecker(mainWindow);
  }

  public ImportDialogPreviewChecker download() {
    Button button = mainWindow.getButton("download");
    assertThat(button.isEnabled());
    button.click();
    return new ImportDialogPreviewChecker(mainWindow);
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
