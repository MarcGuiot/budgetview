package com.budgetview.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudSignupChecker extends ViewChecker {

  public CloudSignupChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudSignupPanel");
  }

  public CloudValidationChecker register(String email) {
    mainWindow.getTextBox("emailField").setText(email, false);
    mainWindow.getButton("next").click();
    return new CloudValidationChecker(mainWindow);
  }
}
