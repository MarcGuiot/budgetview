package org.designup.picsou.functests.checkers;

import org.uispec4j.*;

public class LoginChecker {
  private Panel panel;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;

  public LoginChecker(Panel panel) {
    this.panel = panel;
  }

  public void logNewUser(String user, String password) {
    userField = panel.getInputTextBox("name");
    passwordField = panel.getPasswordField("password");
    createUserCheckbox = panel.getCheckBox();
    createUserCheckbox.select();
    loginButton = panel.getButton("Enter");
    userField.setText(user);
    passwordField.setPassword(password);
    panel.getPasswordField("confirmPassword").setPassword(password);
    loginButton.click();
  }

  public void skip() {
    panel.getButton("Close").click();
  }
}
