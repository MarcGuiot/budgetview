package com.budgetview.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudValidationChecker extends ViewChecker {

  public CloudValidationChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudValidationPanel");
  }


  public CloudBankSelectionChecker processEmail(String code) {
    mainWindow.getInputTextBox("codeField").setText(code, false);
    mainWindow.getButton("next").click();
    checkPanelShown("importCloudBankSelectionPanel");
    return new CloudBankSelectionChecker(mainWindow);
  }
}
