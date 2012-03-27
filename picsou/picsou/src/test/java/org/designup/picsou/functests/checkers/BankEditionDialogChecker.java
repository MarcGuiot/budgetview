package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class BankEditionDialogChecker extends GuiChecker {
  private Window dialog;
  
  public static BankEditionDialogChecker open(Trigger trigger) {
    return new BankEditionDialogChecker(WindowInterceptor.getModalDialog(trigger)); 
  }

  private BankEditionDialogChecker(Window dialog) {
    this.dialog = dialog;
  }
  
  public BankEditionDialogChecker setName(String text) {
    dialog.getTextBox("nameField").setText(text);
    return this;
  }

  public BankEditionDialogChecker checkValidationError(String text) {
    dialog.getButton("OK").click();
    checkTipVisible(dialog, dialog.getTextBox("nameField"), text);
    UISpecAssert.assertTrue(dialog.isVisible());
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

  public void validate() {
    dialog.getButton("OK").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
