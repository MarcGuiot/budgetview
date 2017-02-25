package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudValidationChecker extends ViewChecker {

  public CloudValidationChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudValidationPanel");
  }

  public CloudBankSelectionChecker processEmailAndNextToBankSelection(String code) {
    enterCode(code);
    return new CloudBankSelectionChecker(mainWindow);
  }

  public CloudSubscriptionErrorChecker processEmailAndNextToSubscriptionError(String code) {
    enterCode(code);
    return new CloudSubscriptionErrorChecker(mainWindow);
  }

  public ImportDialogPreviewChecker processEmailAndNextToDownload(String code) {
    enterCode(code);
    return new ImportDialogPreviewChecker(mainWindow);
  }

  public CloudFirstDownloadChecker processEmailAndNextToFirstDownload(String code) {
    enterCode(code);
    return new CloudFirstDownloadChecker(mainWindow);
  }

  public CloudValidationChecker processEmailAndCheckError(String code) {
    enterCode(code);
    checkPanelShown("importCloudValidationPanel");
    checkComponentVisible(mainWindow, JLabel.class, "error", true);
    checkComponentVisible(mainWindow, JButton.class, "back", true);
    assertFalse(mainWindow.getButton("next").isEnabled());
    return this;
  }

  public CloudValidationChecker checkInvalidTokenError() {
    checkErrorMessage(Lang.get("import.cloud.validation.invalid.code"));
    return this;
  }

  public CloudValidationChecker checkTempTokenExpiredError() {
    checkErrorMessage(Lang.get("import.cloud.validation.tempcode.expired"));
    return this;
  }

  public void checkErrorMessage(String text) {
    TextBox errorLabel = mainWindow.getTextBox("error");
    assertThat(errorLabel.textEquals(text));
    assertThat(errorLabel.isVisible());
  }

  public CloudSignupChecker back() {
    mainWindow.getButton("back").click();
    return new CloudSignupChecker(mainWindow);
  }

  public CloudSubscriptionErrorChecker processEmailAndCheckSubscriptionError(String code) {
    enterCode(code);
    return new CloudSubscriptionErrorChecker(mainWindow);
  }

  public void enterCode(String code) {
    mainWindow.getInputTextBox("codeField").setText(code, false);
    mainWindow.getButton("next").click();
  }
}
