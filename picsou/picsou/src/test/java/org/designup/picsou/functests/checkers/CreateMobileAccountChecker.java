package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CreateMobileAccountChecker extends GuiChecker{
  private Window dialog;

  public static CreateMobileAccountChecker open(Trigger trigger) {
    return new CreateMobileAccountChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private CreateMobileAccountChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CreateMobileAccountChecker setEmail(String mail) {
    dialog.getInputTextBox("email").setText(mail);
    return this;
  }

  public CreateMobileAccountChecker setPassword(String password) {
    dialog.getInputTextBox("password").setText(password);
    return this;
  }

  public CreateMobileAccountChecker validateAndClose() {
    clickCreateButton();
    checkComponentVisible(dialog, JButton.class, "create", false);
    checkComponentVisible(dialog, JEditorPane.class, "completionMessage", true);
    assertThat(dialog.isVisible());
    close();
    return this;
  }

  public CreateMobileAccountChecker validateAndCheckEmailTip(String errorMessage) {
    return checkErrorTip(errorMessage, "email");
  }

  public CreateMobileAccountChecker validateAndCheckPasswordTip(String errorMessage) {
    return checkErrorTip(errorMessage, "password");
  }

  private CreateMobileAccountChecker checkErrorTip(String errorMessage, String fieldName) {
    clickCreateButton();
    UISpecAssert.assertTrue(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox(fieldName), errorMessage);
    return this;
  }

  public CreateMobileAccountChecker validateAndCheckAlreadyCreated() {
    clickCreateButton();
    assertThat(dialog.getTextBox("message").textContains("A mobile account was already created for this email."));
    return this;
  }

  public CreateMobileAccountChecker checkNoErrorsShown() {
    checkNoTipVisible(dialog);
    assertThat(dialog.getTextBox("message").textIsEmpty());
    return this;
  }

  private void clickCreateButton() {
    dialog.getButton(Lang.get("mobile.user.create.button")).click();
  }

  public void close() {
    dialog.getButton(Lang.get("close")).click();
    assertFalse(dialog.isVisible());
  }
}
