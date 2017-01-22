package com.budgetview.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudSignupChecker extends ViewChecker {

  public CloudSignupChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudSignupPanel");
  }

  public CloudValidationChecker register(String email) {
    mainWindow.getTextBox("emailField").setText(email, false);
    next();
    return new CloudValidationChecker(mainWindow);
  }

  public CloudSignupChecker registerAndCheckError(String email, String errorMessage) {
    mainWindow.getTextBox("emailField").setText(email, false);
    mainWindow.getButton("next").click();
    checkComponentVisible(mainWindow, JLabel.class, "errorLabel", true);
    assertThat(mainWindow.getTextBox("errorLabel").textContains(errorMessage));
    return this;
  }

  public CloudSignupChecker checkEmail(String email) {
    assertThat(mainWindow.getTextBox("emailField").textEquals(email));
    return this;
  }

  public CloudValidationChecker next() {
    mainWindow.getButton("next").click();
    return new CloudValidationChecker(mainWindow);
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
