package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class ThresholdDialogChecker extends GuiChecker {
  private Window dialog;

  public static ThresholdDialogChecker open(Button button) {
    return new ThresholdDialogChecker(WindowInterceptor.getModalDialog(button.triggerClick()));
  }

  public ThresholdDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ThresholdDialogChecker setThreshold(double value) {
    dialog.getInputTextBox("thresholdField").setText(Formatting.toString(value));
    return this;
  }

  public ThresholdDialogChecker checkThreshold(double value) {
    UISpecAssert.assertThat(dialog.getInputTextBox("thresholdField").textEquals(Formatting.toString(value)));
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
