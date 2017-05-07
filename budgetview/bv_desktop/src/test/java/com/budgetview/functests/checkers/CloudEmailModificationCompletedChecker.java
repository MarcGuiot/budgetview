package com.budgetview.functests.checkers;

import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudEmailModificationCompletedChecker extends ViewChecker {

  public CloudEmailModificationCompletedChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudEmailModificationCompletedPanel");
  }

  public CloudEmailModificationCompletedChecker checkNewEmail(String email) {
    assertThat(mainWindow.getTextBox("emailLabel").textEquals(email));
    return this;
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
