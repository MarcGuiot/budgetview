package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.time.TimeViewPanel;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;

public class LoginChecker {
  private Window window;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;

  public LoginChecker(Window window) {
    this.window = window;
  }

  public void logNewUser(String user, String password) {
    userField = window.getInputTextBox("name");
    passwordField = window.getPasswordField("password");
    createUserCheckbox = window.getCheckBox();
    createUserCheckbox.select();
    loginButton = window.getButton("Enter");
    userField.setText(user);
    passwordField.setPassword(password);
    window.getPasswordField("confirmPassword").setPassword(password);
    loginButton.click();
    UISpecAssert.waitUntil(window.containsSwingComponent(TimeViewPanel.class), 10000);
  }

  public void logUser(String user, String password) {
    userField = window.getInputTextBox("name");
    passwordField = window.getPasswordField("password");
    loginButton = window.getButton("Enter");
    userField.setText(user);
    passwordField.setPassword(password);
    loginButton.click();
    UISpecAssert.waitUntil(window.containsSwingComponent(TimeViewPanel.class), 10000);
  }
}
