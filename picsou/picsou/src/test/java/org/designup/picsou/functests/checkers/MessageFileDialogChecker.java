package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.Key;
import org.uispec4j.utils.KeyUtils;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class MessageFileDialogChecker extends GuiChecker {
  protected Window dialog;

  public MessageFileDialogChecker(Window dialog) {
    this.dialog = dialog;
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

  public Trigger getOkTrigger() {
    return dialog.getButton().triggerClick();
  }
}