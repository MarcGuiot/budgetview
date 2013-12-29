package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.MonthSliderChecker;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectDuplicationDialogChecker extends GuiChecker {

  private Window dialog;

  public static ProjectDuplicationDialogChecker open(Trigger trigger) {
    return new ProjectDuplicationDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ProjectDuplicationDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ProjectDuplicationDialogChecker checkMessage(String message) {
    assertThat(dialog.getTextBox("message").textEquals(message));
    return this;
  }

  public ProjectDuplicationDialogChecker setName(String newName) {
    dialog.getInputTextBox("nameField").setText(newName);
    return this;
  }

  public ProjectDuplicationDialogChecker checkFirstMonth(String text) {
    MonthSliderChecker.init(dialog, "firstMonth").checkText(text);
    return this;
  }

  public ProjectDuplicationDialogChecker setFirstMonth(int monthId) {
    MonthSliderChecker.init(dialog, "firstMonth").setMonth(monthId);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public ProjectDuplicationDialogChecker validateAndCheckNameError(String errorMessage) {
    dialog.getButton("OK").click();
    assertThat(dialog.isVisible());
    checkTipVisible(dialog, dialog.getInputTextBox("nameField"), errorMessage);
    return this;
  }

  public ProjectDuplicationDialogChecker checkNoTipsShown() {
    checkNoTipVisible(dialog);
    return this;
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }
}
