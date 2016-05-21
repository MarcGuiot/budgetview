package com.budgetview.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PasswordDialogChecker extends GuiChecker {
  protected Window dialog;

  public PasswordDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public PasswordDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public PasswordDialogChecker setPassword(String password) {
    dialog.getPasswordField().setPassword(password);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public Trigger getCancelTrigger() {
    return dialog.getButton("Cancel").triggerClick();
  }

  public Trigger getOkTrigger() {
    return dialog.getButton("Ok").triggerClick();
  }
}