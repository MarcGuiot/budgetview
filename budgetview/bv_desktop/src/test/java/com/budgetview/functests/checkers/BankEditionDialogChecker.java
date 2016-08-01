package com.budgetview.functests.checkers;

import org.uispec4j.Clickable;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BankEditionDialogChecker extends GuiChecker {
  private Window dialog;

  public static BankEditionDialogChecker open(Clickable clickable) {
    return new BankEditionDialogChecker(WindowInterceptor.getModalDialog(clickable.triggerClick()));
  }

  public static BankEditionDialogChecker open(Trigger trigger) {
    return new BankEditionDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private BankEditionDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public BankEditionDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public BankEditionDialogChecker setName(String text) {
    dialog.getTextBox("nameField").setText(text);
    return this;
  }

  public BankEditionDialogChecker checkName(String text) {
    assertThat(dialog.getTextBox("nameField").textEquals(text));
    return this;
  }

  public BankEditionDialogChecker checkValidationError(String text) {
    dialog.getButton("OK").click();
    checkTipVisible(dialog, dialog.getTextBox("nameField"), text);
    assertTrue(dialog.isVisible());
    return this;
  }

  public BankEditionDialogChecker checkNoErrorDisplayed() {
    checkNoTipVisible(dialog);
    return this;
  }

  public BankEditionDialogChecker setUrl(String text) {
    dialog.getTextBox("urlField").setText(text);
    return this;
  }

  public BankEditionDialogChecker checkUrl(String text) {
    assertThat(dialog.getTextBox("urlField").textEquals(text));
    return this;
  }


  public void setUrlAndValidate(String text) {
    dialog.getTextBox("urlField").setText(text, true);
    assertFalse(dialog.isVisible());
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }
}
