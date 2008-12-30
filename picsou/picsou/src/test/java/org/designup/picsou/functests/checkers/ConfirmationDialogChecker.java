package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class ConfirmationDialogChecker extends GuiChecker {
  private Window dialog;

  public static ConfirmationDialogChecker init(Trigger trigger) {
    return new ConfirmationDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ConfirmationDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ConfirmationDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public ConfirmationDialogChecker checkContainsText(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
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
