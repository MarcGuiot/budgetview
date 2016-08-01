package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class LoginChecker extends GuiChecker {
  private Window window;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;
  private PasswordField passwordConfirmationField;

  public static LoginChecker init(Window window) {
    return new LoginChecker(window);
  }

  public LoginChecker(Window window) {
    this.window = window;
    userField = window.getInputTextBox("name");
    passwordField = window.getPasswordField("password");
    createUserCheckbox = window.getCheckBox();
    passwordConfirmationField = window.getPasswordField("confirmPassword");
    loginButton = window.getButton(Lang.get("login.enter"));
  }

  public LoginChecker checkUserName(String name) {
    assertThat(userField.textEquals(name));
    return this;
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

  public void logExistingUser(String user, String password) throws Exception {
    enterUserName(user);
    enterPassword(password);
    Trigger trigger = new Trigger() {
      public void run() throws Exception {
        loginButton.click();
        waitForApplicationToLoad();
      }
    };
    trigger.run();
  }

  public void clickDemoLink() {
    window.getButton("demoMode").click();
    waitForApplicationToLoad();
  }

  public void waitForApplicationToLoad() {
    UISpecAssert.waitUntil(window.containsSwingComponent(JPanel.class, "mainPanel"), 10000);
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

  public LoginChecker checkUserSelectionAvailable() {
    checkComponentVisible(window, JButton.class, "selectUser", true);
    return this;
  }

  public LoginChecker checkUserSelectionHidden() {
    Button selectUser = window.getButton("selectUser");
    assertFalse(selectUser.isEnabled());
    assertFalse(selectUser.isVisible());
    return this;
  }

  public UserSelectionDialogChecker openUserSelection() {
    return UserSelectionDialogChecker.open(window.getButton("selectUser"));
  }

  public void checkComponentsVisible() {
    assertThat(userField.isVisible());
    assertThat(passwordField.isVisible());
    assertThat(loginButton.isVisible());
  }

  public void checkLoggedIn() {
    UISpecAssert.waitUntil(window.containsMenuBar(), 2000);
  }

  public LoginChecker checkNotLoggedIn() {
    assertFalse(window.containsMenuBar());
    return this;
  }

  public void checkFirstAutoLogin() {
    Button button = window.getButton("autoLogin");
    assertThat(button.textEquals("Create auto login user"));
  }

  public void clickFirstAutoLogin() {
    doLogin("Create auto login user", true);
  }

  public void clickAutoLogin() {
    doLogin("Enter without password", false);
  }

  private void doLogin(String message, boolean slaValidation) {
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
