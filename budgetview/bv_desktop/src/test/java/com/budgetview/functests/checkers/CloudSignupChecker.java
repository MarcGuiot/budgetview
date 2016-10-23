package com.budgetview.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudSignupChecker extends ViewChecker {

  public CloudSignupChecker(Window mainWindow) {
    super(mainWindow);
    assertThat(new Assertion() {
      public void check() {
        if (!mainWindow.getPanel("importCloudSignupPanel").isVisible().isTrue()) {
          UISpecAssert.fail();
        }
      }
    }, 5000);
  }

  public CloudValidationChecker register(String email) {
    mainWindow.getTextBox("emailField").setText(email, false);
    mainWindow.getButton("next").click();
    checkPanelShown("importCloudValidationPanel");
    return new CloudValidationChecker(mainWindow);
  }
}
