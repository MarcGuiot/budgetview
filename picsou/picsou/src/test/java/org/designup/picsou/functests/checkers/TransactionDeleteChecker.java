package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class TransactionDeleteChecker extends GuiChecker {
  private Window dialog;

  public TransactionDeleteChecker(Window dialog) {
    this.dialog = dialog;
  }

  public TransactionDeleteChecker selectUpdatePosition() {
    dialog.getCheckBox("updateAccountPosition").select();
    return this;
  }

  public TransactionDeleteChecker selectNoUpdateOfPosition() {
    dialog.getCheckBox("updateAccountPosition").unselect();
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

  public TransactionDeleteChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public TransactionDeleteChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }
}
