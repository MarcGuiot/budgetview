package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Trigger;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.assertion.UISpecAssert;

public class SeriesDeleteDialogChecker extends DataChecker {
  private Window dialog;

  public static SeriesDeleteDialogChecker init(Trigger trigger) {
    return new SeriesDeleteDialogChecker(trigger);
  }
  
  private SeriesDeleteDialogChecker(Trigger trigger) {
    this(WindowInterceptor.getModalDialog(trigger));
  }

  public SeriesDeleteDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SeriesDeleteDialogChecker checkMessage() {
    UISpecAssert.assertThat(dialog.getTextBox("message").textContains("Some operations use this series"));
    return this;
  }

  public void cancel() {
    dialog.getButton("cancel").click();
  }

  public void validate() {
    dialog.getButton("ok").click();
  }
}
