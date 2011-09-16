package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class UserEvaluationDialogChecker extends GuiChecker {

  private Window dialog;

  public static UserEvaluationDialogChecker open(Trigger trigger) {
    return new UserEvaluationDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public UserEvaluationDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public UserEvaluationDialogChecker selectYes() {
    dialog.getToggleButton("yesToggle").click();
    return this;
  }

  public UserEvaluationDialogChecker selectNo() {
    dialog.getToggleButton("noToggle").click();
    return this;
  }

  public UserEvaluationDialogChecker enterComment(String comment) {
    dialog.getInputTextBox("comment").setText(comment);
    return this;
  }

  public UserEvaluationDialogChecker enterEmailAddress(String address) {
    dialog.getInputTextBox("email").setText(address);
    return this;
  }

  public UserEvaluationDialogChecker checkSendEnabled() {
    UISpecAssert.assertTrue(dialog.getButton("send").isEnabled());
    return this;
  }

  public UserEvaluationDialogChecker checkSendDisabled() {
    assertFalse(dialog.getButton("send").isEnabled());
    return this;
  }

  public void send() {
    dialog.getButton("send").click();
  }

  public void cancel() {
    dialog.getButton("cancel").click();
  }
}

