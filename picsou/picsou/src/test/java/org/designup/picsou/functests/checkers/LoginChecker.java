package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class LoginChecker extends GuiChecker {
  private Window window;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;
  private PasswordField passwordConfirmationField;

  public LoginChecker(Window window) {
    this.window = window;
    userField = window.getInputTextBox("name");
    passwordField = window.getPasswordField("password");
    createUserCheckbox = window.getCheckBox();
    passwordConfirmationField = window.getPasswordField("confirmPassword");
    loginButton = window.getButton("Enter");
  }

  public LoginChecker enterUserName(String name) {
    userField.setText(name);
    return this;
  }

  public LoginChecker enterPassword(String password) {
    passwordField.setPassword(password);
    return this;
  }

  public LoginChecker enterUserAndPassword(String user, String password) {
    enterUserName(user);
    enterPassword(password);
    return this;
  }

  public LoginChecker confirmPassword(String password) {
    passwordConfirmationField.setPassword(password);
    return this;
  }

  public void logNewUser(String user, String password) {
    enterUserName(user);
    enterPassword(password);
    setCreation();
    confirmPassword(password);
    loginAndSkipSla();
    waitForApplicationToLoad();
  }

  public LoginChecker loginAndSkipSla() {
    SlaValidationDialogChecker.init(loginButton.triggerClick()).acceptTerms().validate();
    return this;
  }

  public LoginChecker setCreation() {
    createUserCheckbox.select();
    assertThat(passwordConfirmationField.isVisible());
    return this;
  }

  public void logExistingUser(String user, String password) {
    enterUserName(user);
    enterPassword(password);
    loginButton.click();
    checkNoErrorDisplayed();
    waitForApplicationToLoad();
  }

  public void clickDemoLink() {
    window.getButton("demoMode").click();
    waitForApplicationToLoad();
  }

  public void waitForApplicationToLoad() {
    UISpecAssert.waitUntil(window.containsSwingComponent(TimeViewPanel.class), 10000);
  }

  public void checkConfirmPasswordVisible(boolean visible) {
    checkComponentVisible(window, JTextField.class, "confirmPassword", visible);
  }

  public LoginChecker checkNoErrorDisplayed() {
    assertThat(window.getTextBox("message").textIsEmpty());
    return this;
  }

  public LoginChecker checkErrorMessage(String key) {
    assertThat(window.getTextBox("message").textContains(Lang.get(key, "")));
    return this;
  }

  public LoginChecker clickEnter() {
    loginButton.click();
    return this;
  }

  public SlaValidationDialogChecker clickEnterAndGetSlaDialog() {
    return SlaValidationDialogChecker.init(loginButton.triggerClick());
  }

  public void checkComponentsVisible() {
    assertThat(userField.isVisible());
    assertThat(passwordField.isVisible());
    assertThat(loginButton.isVisible());
  }

  public void checkLoggedIn() {
    UISpecAssert.waitUntil(window.containsMenuBar(), 2000);
  }

  public void checkNotLoggedIn() {
    assertFalse(window.containsMenuBar());
  }

  public void checkFirstAutoLogin() {
    Button button = window.getButton("autoLogin");
    assertThat(button.textEquals("Create auto login user"));
  }

  public void clickFirstAutoLogin() {
    doLoggin("Create auto login user", true);
  }

  public void clickAutoLogin() {
    doLoggin("Enter without password", false);
  }

  private void doLoggin(String message, boolean slaValidation) {
    Button button = window.getButton("autoLogin");
    assertThat(button.textEquals(message));
    if (slaValidation) {
      SlaValidationDialogChecker.init(button.triggerClick()).acceptTerms().validate();
    }
    else {
      button.click();
    }
    waitForApplicationToLoad();
  }
}
