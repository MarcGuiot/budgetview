package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class SignpostDialogChecker {
  private Window dialog;

  public static SignpostDialogChecker open(Button button) {
    return open(button.triggerClick());
  }

  public static SignpostDialogChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new SignpostDialogChecker(dialog);
  }

  public SignpostDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

}
