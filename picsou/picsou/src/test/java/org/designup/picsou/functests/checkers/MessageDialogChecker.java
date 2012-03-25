package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class MessageDialogChecker extends GuiChecker {
  protected Window dialog;

  public static MessageDialogChecker open(Trigger trigger) {
    return new MessageDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public MessageDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MessageDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public MessageDialogChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
    assertFalse(dialog.isVisible());
  }

  public Trigger triggerCloseUndefined() {
    return new Trigger() {
      public void run() throws Exception {
        dialog.getButton().click();
      }
    };
  }

  public Trigger triggerClose() {
    return new Trigger() {
      public void run() throws Exception {
        dialog.getButton("Close").click();
      }
    };
  }

  public Trigger triggerClose(final String buttonMessage) {
    return new Trigger() {
      public void run() throws Exception {
        dialog.getButton(buttonMessage).click();
      }
    };
  }


}