package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class FeedbackDialogChecker extends GuiChecker{
  private Window dialog;

  public FeedbackDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  static FeedbackDialogChecker init(Trigger trigger){
    return new FeedbackDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public FeedbackDialogChecker checkComponents() {
    checkComponentVisible(dialog, JButton.class, "send", true);
    checkComponentVisible(dialog, JButton.class, "cancel", true);
    checkComponentVisible(dialog, JTextField.class, "fromMail", true);
    return this;
  }

  public FeedbackDialogChecker checkConnected() {
    assertTrue(dialog.getButton("Send").isEnabled());
    return this;
  }

  public FeedbackDialogChecker checkNotConnected() {
    assertFalse(dialog.getButton("Send").isEnabled());
    return this;
  }

  public FeedbackDialogChecker setLogsAdded() {
    dialog.getCheckBox("addLogs").select();
    return this;
  }

  public void send(String email, String content) {
    dialog.getTextBox("fromMail").setText(email);
    dialog.getTextBox("mailContent").setText(content);
    dialog.getButton("Send").click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    assertFalse(dialog.isVisible());
  }
}
