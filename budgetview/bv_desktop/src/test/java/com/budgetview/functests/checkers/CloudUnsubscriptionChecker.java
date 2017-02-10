package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudUnsubscriptionChecker extends ViewChecker {

  public CloudUnsubscriptionChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudUnsubscriptionPanel");
  }

  public CloudUnsubscriptionChecker unsubscribeAndConfirm() {
    ConfirmationDialogChecker.open(mainWindow.getButton("unsubscribe").triggerClick())
      .validate();
    return this;
  }

  public CloudUnsubscriptionChecker unsubscribeAndCancel() {
    ConfirmationDialogChecker.open(mainWindow.getButton("unsubscribe").triggerClick())
      .cancel();
    return this;
  }

  public CloudUnsubscriptionChecker checkUnsubscribeButtonShown() {
    checkComponentVisible(mainWindow, JButton.class, "unsubscribe", true);
    return this;
  }

  public CloudUnsubscriptionChecker checkUnsubscribeButtonHidden() {
    checkComponentVisible(mainWindow, JButton.class, "unsubscribe", false);
    return this;
  }

  public CloudUnsubscriptionChecker checkIntroMessageShown() {
    assertThat(mainWindow.getTextBox("message").htmlEquals(Lang.get("import.cloud.unsubscription.message")));
    return this;
  }

  public CloudUnsubscriptionChecker checkCompletionMessageShown() {
    assertThat(mainWindow.getTextBox("message").htmlEquals(Lang.get("import.cloud.unsubscription.done")));
    return this;
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
