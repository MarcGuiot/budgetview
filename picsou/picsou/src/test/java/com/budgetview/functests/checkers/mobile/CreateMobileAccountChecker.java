package com.budgetview.functests.checkers.mobile;

import com.budgetview.functests.checkers.GuiChecker;
import com.budgetview.utils.Lang;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CreateMobileAccountChecker extends GuiChecker {
  private Window dialog;

  public static CreateMobileAccountChecker open(Trigger trigger) {
    return new CreateMobileAccountChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private CreateMobileAccountChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CreateMobileAccountChecker checkTitle() {
    assertThat(dialog.getTextBox("title").textEquals(Lang.get("mobile.dialog.title")));
    return this;
  }

  public CreateMobileAccountChecker setEmailWithoutValidating(String mail) {
    TextBox field = dialog.getInputTextBox("emailField");
    field.setText(mail, false);
    FocusListener[] listeners = field.getAwtComponent().getFocusListeners();
    for (FocusListener listener : listeners) {
      listener.focusLost(new FocusEvent(field.getAwtComponent(), 0));
    }
    assertThat(dialog.isVisible());
    return this;
  }

  public CreateMobileAccountChecker setEmailAndValidate(String mail) {
    dialog.getInputTextBox("emailField").setText(mail);
    return this;
  }

  public String getPassword() {
    return dialog.getTextBox("passwordLabel").getText();
  }

  public  CreateMobileAccountChecker checkEditablePassword() {
    checkComponentVisible(dialog, JLabel.class, "passwordLabel", false);
    checkComponentVisible(dialog, JButton.class, "changePassword", false);
    assertThat(dialog.getTextBox("passwordField").isVisible());
    assertThat(dialog.getButton("applyPasswordEdit").isVisible());
    assertThat(dialog.getButton("cancelPasswordEdit").isVisible());
    return this;
  }

  public CreateMobileAccountChecker setNewPassword(String requestedPassword) {
    dialog.getButton("changePassword").click();
    checkEditablePassword();
    dialog.getTextBox("passwordField").setText(requestedPassword);
    dialog.getButton("applyPasswordEdit").click();
    checkReadOnlyPassword(requestedPassword);
    checkNoTipVisible(dialog);
    return this;
  }

  public CreateMobileAccountChecker editPasswordAndCancel(String dummyPassword) {
    dialog.getButton("changePassword").click();
    checkEditablePassword();
    dialog.getTextBox("passwordField").setText(dummyPassword);
    dialog.getButton("cancelPasswordEdit").click();
    return this;
  }

  public CreateMobileAccountChecker setEmptyPasswordAndCheckErrorOnApply(String message) {
    dialog.getButton("changePassword").click();
    checkEditablePassword();
    TextBox passwordField = dialog.getTextBox("passwordField");
    passwordField.setText("");
    dialog.getButton("applyPasswordEdit").click();
    assertThat(passwordField.isVisible());
    checkTipVisible(dialog, passwordField, message);
    dialog.getButton("cancelPasswordEdit").click();
    return this;
  }

  public CreateMobileAccountChecker setEmptyPasswordAndCheckErrorOnActivate(String message) {
    dialog.getButton("changePassword").click();
    checkEditablePassword();
    TextBox passwordField = dialog.getTextBox("passwordField");
    passwordField.setText("");
    dialog.getButton("activateMobileAccount").click();
    assertThat(passwordField.isVisible());
    checkTipVisible(dialog, passwordField, message);
    dialog.getButton("cancelPasswordEdit").click();
    return this;
  }

  public void checkConfirmationAndClose() {
    checkMessageHidden(dialog, "message");
    checkComponentVisible(dialog, JEditorPane.class, "completionMessage", true);
    checkComponentVisible(dialog, JButton.class, "activateMobileAccount", false);
    assertThat(dialog.isVisible());
    close();
  }

  public void validateAndClose() {
    activate();
    checkNoTipVisible(dialog);
    checkConfirmationAndClose();
  }

  public CreateMobileAccountChecker validateAndCheckEmailTip(String errorMessage) {
    return checkErrorTip(errorMessage, "emailField");
  }

  private CreateMobileAccountChecker checkErrorTip(String errorMessage, String fieldName) {
    activate();
    UISpecAssert.assertTrue(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox(fieldName), errorMessage);
    return this;
  }

  public CreateMobileAccountChecker validateAndCheckAlreadyCreated() {
    activate();
    assertThat(dialog.getTextBox("message").textContains("A mobile account was already created for this email."));
    return this;
  }

  public CreateMobileAccountChecker checkNoErrorsShown() {
    checkNoTipVisible(dialog);
    checkMessageHidden(dialog, "message");
    return this;
  }

  private void activate() {
    dialog.getButton("activateMobileAccount").click();
  }

  public void close() {
    dialog.getButton(Lang.get("close")).click();
    assertFalse(dialog.isVisible());
  }

  public CreateMobileAccountChecker checkReadOnlyPassword(String password) {
    assertThat(dialog.getTextBox("passwordLabel").isVisible());
    assertThat(dialog.getTextBox("passwordLabel").textEquals(password));
    assertThat(dialog.getButton("changePassword").isVisible());
    checkComponentVisible(dialog, JTextField.class, "passwordField", false);
    checkComponentVisible(dialog, JButton.class, "applyPasswordEdit", false);
    checkComponentVisible(dialog, JButton.class, "cancelPasswordEdit", false);
    return this;
  }
}
