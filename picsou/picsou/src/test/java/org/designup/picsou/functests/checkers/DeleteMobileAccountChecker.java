package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class DeleteMobileAccountChecker extends GuiChecker{
  private Window dialog;

  public static DeleteMobileAccountChecker open(Trigger trigger) {
    return new DeleteMobileAccountChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private DeleteMobileAccountChecker(Window dialog) {
    this.dialog = dialog;
  }

  public DeleteMobileAccountChecker setEmail(String mail, String password) {
    dialog.getInputTextBox("emailField").setText(mail);
    dialog.getInputTextBox("passwordField").setText(password);
    return this;
  }

  public String getPassword() {
    return dialog.getInputTextBox("passwordField").getText();
  }

  public DeleteMobileAccountChecker validateAndClose() {
    clickDeleteButton();
    checkComponentVisible(dialog, JButton.class, "delete", false);
    checkComponentVisible(dialog, JEditorPane.class, "completionMessage", true);
    assertThat(dialog.isVisible());
    close();
    assertFalse(dialog.isVisible());
    return this;
  }

  public DeleteMobileAccountChecker validateAndCheckEmailTip(String errorMessage) {
    return checkErrorTip(errorMessage, "emailField");
  }

  private DeleteMobileAccountChecker checkErrorTip(String errorMessage, String fieldName) {
    clickDeleteButton();
    UISpecAssert.assertTrue(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox(fieldName), errorMessage);
    return this;
  }

  public DeleteMobileAccountChecker validateAndCheckUnknownUser() {
    clickDeleteButton();
    assertThat(dialog.getTextBox("message").textContains("Bad password or bad email"));
    return this;
  }

  public DeleteMobileAccountChecker checkNoErrorsShown() {
    checkNoTipVisible(dialog);
    assertThat(dialog.getTextBox("message").textIsEmpty());
    return this;
  }

  private void clickDeleteButton() {
    dialog.getButton(Lang.get("mobile.user.delete.button")).click();
  }

  public void close() {
    dialog.getButton(Lang.get("close")).click();
    assertFalse(dialog.isVisible());
  }

  public void checkUser(String mail, String password) {
    assertThat(dialog.getInputTextBox("emailField").textEquals(mail));
    assertThat(dialog.getInputTextBox("passwordField").textEquals(password));
  }

  public void checkEmpty() {
    assertThat(dialog.getInputTextBox("emailField").textIsEmpty());
    assertThat(dialog.getInputTextBox("passwordField").textIsEmpty());
  }
}
