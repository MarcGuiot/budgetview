package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

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

  public MessageDialogChecker checkSuccessMessageContains(String message) {
    return checkMessage(message, MessageDialog.SUCCESS_ICON);
  }

  public MessageDialogChecker checkInfoMessageContains(String message) {
    return checkMessage(message, MessageDialog.INFO_ICON);
  }

  public MessageDialogChecker checkErrorMessageContains(String message) {
    return checkMessage(message, MessageDialog.ERROR_ICON);
  }

  private MessageDialogChecker checkMessage(String message, Icon icon) {
    assertThat(dialog.getTextBox("message").textContains(message));
    assertThat(dialog.getTextBox("icon").iconEquals(icon));
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
    assertFalse(dialog.isVisible());
  }

  public Trigger triggerClose() {
    return new Trigger() {
      public void run() throws Exception {
        dialog.getButton("Close").click();
      }
    };
  }
}