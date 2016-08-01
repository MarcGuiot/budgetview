package com.budgetview.functests.checkers;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import junit.framework.Assert;
import org.uispec4j.Clipboard;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class MessageAndDetailsDialogChecker {

  private final Window dialog;

  public static MessageAndDetailsDialogChecker init(Trigger trigger) {
    return new MessageAndDetailsDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private MessageAndDetailsDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MessageAndDetailsDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public MessageAndDetailsDialogChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }

  public MessageAndDetailsDialogChecker checkDetailsContain(String text) {
    TextBox details = dialog.getTextBox("details");
    assertFalse(details.isEditable());
    assertThat(details.textContains(text));
    return this;
  }

  public MessageAndDetailsDialogChecker checkCopy() throws Exception {
    LoggedInFunctionalTestCase.callFailIfClipBoardDisable();
    dialog.getButton("copy").click();

    String details = dialog.getTextBox("details").getText();
    Assert.assertEquals(details, Clipboard.getContentAsText());

    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
    assertFalse(dialog.isVisible());
  }
}