package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import org.uispec4j.interception.WindowInterceptor;

public class FeedbackDialogChecker extends GuiChecker{
  private Window dialog;

  public FeedbackDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  static FeedbackDialogChecker init(Trigger trigger){
    return new FeedbackDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public void send(String title, String mail, String content) {
    dialog.getTextBox("mailSubject").setText(title);
    dialog.getTextBox("fromMail").setText(mail);
    dialog.getTextBox("mailContent").setText(content);
    dialog.getButton("Send").click();
    assertFalse(dialog.isVisible());
  }
}
