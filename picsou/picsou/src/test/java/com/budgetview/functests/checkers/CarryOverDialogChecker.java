package com.budgetview.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CarryOverDialogChecker extends GuiChecker {
  private Window dialog;

  public static CarryOverDialogChecker open(Trigger trigger) {
    return new CarryOverDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private CarryOverDialogChecker(Window dialog) {
    this.dialog = dialog;
  }
  
  public CarryOverDialogChecker checkMessage(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }
  
  public CarryOverDialogChecker selectOption(String text) {
    dialog.getRadioButton(text).click();
    return this;
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
