package com.budgetview.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class SeriesGroupNameDialogChecker extends GuiChecker {

  private Window dialog;

  public static SeriesGroupNameDialogChecker open(Trigger trigger) {
    return new SeriesGroupNameDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public SeriesGroupNameDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SeriesGroupNameDialogChecker checkName(String name) {
    assertThat(dialog.getInputTextBox("nameField").textEquals(name));
    return this;
  }

  public SeriesGroupNameDialogChecker setName(String text) {
    dialog.getInputTextBox("nameField").setText(text, false);
    return this;
  }

  public SeriesGroupNameDialogChecker checkNameError(String errorMessage) {
    dialog.getButton("OK").click();
    assertTrue(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox("nameField"), errorMessage);
    return this;
  }

  public SeriesGroupNameDialogChecker checkNoError() {
    checkNoTipVisible(dialog);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public void close() {
    dialog.getButton("close").click();
    assertFalse(dialog.isVisible());
  }
}
