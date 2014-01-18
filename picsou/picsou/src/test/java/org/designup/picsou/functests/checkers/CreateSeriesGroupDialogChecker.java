package org.designup.picsou.functests.checkers;

import org.uispec4j.MenuItem;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class CreateSeriesGroupDialogChecker extends GuiChecker {

  private Window dialog;

  public static CreateSeriesGroupDialogChecker open(Trigger trigger) {
    return new CreateSeriesGroupDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public CreateSeriesGroupDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CreateSeriesGroupDialogChecker setName(String text) {
    dialog.getInputTextBox("nameField").setText(text, false);
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
