package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class MessageFileDialogChecker extends GuiChecker {
  protected Window dialog;

  public static MessageFileDialogChecker init(Trigger trigger) {
    return new MessageFileDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public MessageFileDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MessageFileDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public MessageFileDialogChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }

  public String getFilePath() {
    return dialog.getTextBox("filePath").getText();
  }

  public MessageFileDialogChecker checkFilePath(String path) {
    assertThat(dialog.getTextBox("filePath").textEquals(path));
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
    assertFalse(dialog.isVisible());
  }

  public Trigger getOkTrigger() {
    return dialog.getButton().triggerClick();
  }
}