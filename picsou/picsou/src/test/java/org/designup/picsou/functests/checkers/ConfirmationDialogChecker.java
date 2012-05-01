package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class ConfirmationDialogChecker extends GuiChecker {
  private Window dialog;

  public static ConfirmationDialogChecker open(Trigger trigger) {
    return new ConfirmationDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ConfirmationDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ConfirmationDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public ConfirmationDialogChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }

  public ConfirmationDialogChecker clickOnHyperlink(String linkText) {
    dialog.getTextBox("message").clickOnHyperlink(linkText);
    return this;
  }

  public void validate() {
    validate("OK");
  }

  public void validate(String okButtonLabel) {
    dialog.getButton(okButtonLabel).click();
    checkHidden();
  }

  public Trigger getOkTrigger() {
    return getOkTrigger("OK");
  }

  public Trigger getOkTrigger(String buttonName) {
    return dialog.getButton(buttonName).triggerClick();
  }

  public Trigger getCancelTrigger(){
    return dialog.getButton("Cancel").triggerClick();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    checkHidden();
  }

  public void checkHidden() {
    assertFalse(dialog.isVisible());
  }
}
