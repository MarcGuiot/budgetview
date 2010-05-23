package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class DeleteSubSeriesDialogChecker extends GuiChecker {
  private Window dialog;

  public static DeleteSubSeriesDialogChecker open(Trigger trigger) {
    return new DeleteSubSeriesDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private DeleteSubSeriesDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public DeleteSubSeriesDialogChecker checkDeletionOptions(String... labels) {
    UISpecAssert.assertThat(dialog.getComboBox().contentEquals(labels));
    return this;
  }

  public DeleteSubSeriesDialogChecker selectDeletionOption(String label) {
    dialog.getComboBox().select(label);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

}
