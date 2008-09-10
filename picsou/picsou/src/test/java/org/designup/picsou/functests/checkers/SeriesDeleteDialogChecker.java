package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class SeriesDeleteDialogChecker extends DataChecker {
  private Window dialog;

  public SeriesDeleteDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SeriesDeleteDialogChecker checkMessage() {
    UISpecAssert.assertThat(dialog.getTextBox("message").textContains("Some operations use this series"));
    return this;
  }

  public void validate() {
    dialog.getButton("ok").click();
  }
}
