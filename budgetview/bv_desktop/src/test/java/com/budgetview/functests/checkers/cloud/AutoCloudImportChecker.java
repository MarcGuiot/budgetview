package com.budgetview.functests.checkers.cloud;

import com.budgetview.functests.checkers.ViewChecker;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class AutoCloudImportChecker extends ViewChecker {
  public AutoCloudImportChecker(Window mainWindow) {
    super(mainWindow);
  }

  public AutoCloudImportChecker checkDisplayed() {
    checkComponentVisible(mainWindow, JPanel.class, "autoCloudImportView", true);
    return this;
  }

  public AutoCloudImportChecker checkMessage(String message) {
    assertThat(mainWindow.getTextBox("label").textEquals(message));
    return this;
  }

  public AutoCloudImportChecker waitForEndOfProgress() {
    UISpecAssert.waitUntil(componentVisible(mainWindow, JPanel.class, "progressPanel", false), 10000);
    return this;
  }

  public AutoCloudImportChecker performAction(String expectedLabel) {
    Button button = mainWindow.getButton("action");
    assertThat(button.isVisible());
    assertThat(button.textEquals(expectedLabel));
    button.click();
    return this;
  }

  public AutoCloudImportChecker checkActionHidden() {
    checkComponentVisible(mainWindow, JButton.class, "action", false);
    return this;
  }

  public AutoCloudImportChecker cancel() {
    Button button = mainWindow.getButton("cancel");
    assertThat(button.isVisible());
    button.click();
    return this;
  }
}
