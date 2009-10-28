package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;
import org.globsframework.gui.splits.utils.GuiUtils;
import junit.framework.Assert;

public class MessageAndDetailsDialogChecker extends MessageDialogChecker {

  public static MessageAndDetailsDialogChecker init(Trigger trigger) {
    return new MessageAndDetailsDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private MessageAndDetailsDialogChecker(Window dialog) {
    super(dialog);
  }

  public MessageAndDetailsDialogChecker checkTitle(String title) {
    super.checkTitle(title);
    return this;
  }

  public MessageAndDetailsDialogChecker checkMessageContains(String message) {
    super.checkMessageContains(message);
    return this;
  }

  public MessageAndDetailsDialogChecker checkDetailsContain(String text) {
    TextBox details = dialog.getTextBox("details");
    assertFalse(details.isEditable());
    assertThat(details.textContains(text));
    return this;
  }

  public MessageAndDetailsDialogChecker checkCopy() throws Exception {
    dialog.getButton("copy").click();

    String details = dialog.getTextBox("details").getText();
    Assert.assertEquals(details, Clipboard.getContentAsText());

    return this;
  }

}