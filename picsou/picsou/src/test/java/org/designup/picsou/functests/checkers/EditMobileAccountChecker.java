package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class EditMobileAccountChecker extends GuiChecker{
  private Window dialog;

  public static EditMobileAccountChecker open(Trigger trigger) {
    return new EditMobileAccountChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private EditMobileAccountChecker(Window dialog) {
    this.dialog = dialog;
  }

  public EditMobileAccountChecker setEmail(String mail) {
    dialog.getInputTextBox("emailField").setText(mail);
    return this;
  }

  public String getPassword() {
    return dialog.getTextBox("passwordLabel").getText();
  }

  public EditMobileAccountChecker validateAndClose() {
    clickDeleteButton();
    checkComponentVisible(dialog, JButton.class, "delete", false);
    checkComponentVisible(dialog, JEditorPane.class, "deletionMessage", true);
    assertThat(dialog.isVisible());
    close();
    assertFalse(dialog.isVisible());
    return this;
  }

  public EditMobileAccountChecker validateAndCheckEmailTip(String errorMessage) {
    return checkErrorTip(errorMessage, "emailField");
  }

  private EditMobileAccountChecker checkErrorTip(String errorMessage, String fieldName) {
    clickDeleteButton();
    UISpecAssert.assertTrue(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox(fieldName), errorMessage);
    return this;
  }

  public EditMobileAccountChecker validateAndCheckUnknownUser() {
    clickDeleteButton();
    assertThat(dialog.getTextBox("message").textContains("Bad password or bad email"));
    return this;
  }

  public EditMobileAccountChecker checkNoErrorsShown() {
    checkNoTipVisible(dialog);
    assertThat(dialog.getTextBox("message").textIsEmpty());
    return this;
  }

  private void clickDeleteButton() {
    dialog.getButton(Lang.get("mobile.user.delete.button")).click();
  }

  public EditMobileAccountChecker checkUser(String mail, String password) {
    assertThat(dialog.getInputTextBox("emailField").textEquals(mail));
    assertThat(dialog.getTextBox("passwordLabel").textEquals(password));
    return this;
  }

  public void close() {
    dialog.getButton(Lang.get("close")).click();
    assertFalse(dialog.isVisible());
  }
}
