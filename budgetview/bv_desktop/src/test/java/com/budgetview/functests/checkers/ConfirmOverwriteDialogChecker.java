package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ConfirmOverwriteDialogChecker extends GuiChecker {
  private Window dialog;

  public static ConfirmOverwriteDialogChecker open(Trigger trigger) {
    return new ConfirmOverwriteDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ConfirmOverwriteDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ConfirmOverwriteDialogChecker checkOverwriteSelected() {
    assertThat(dialog.getRadioButton("overwriteRadio").isSelected());
    return this;
  }

  public ConfirmOverwriteDialogChecker selectUse() {
    dialog.getRadioButton("useRadio").click();
    return this;
  }

  public ConfirmOverwriteDialogChecker selectOverwrite() {
    dialog.getRadioButton("overwriteRadio").click();
    return this;
  }

  public void validateAndConfirm() {
    MessageDialogChecker.open(dialog.getButton("OK").triggerClick())
      .checkInfoMessageContains(Lang.get("data.path.exit"))
      .close();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    checkHidden();
  }

  public void checkHidden() {
    assertFalse(dialog.isVisible());
  }
}
