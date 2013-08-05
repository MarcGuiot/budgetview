package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Clipboard;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class SynchroErrorDialogChecker {

  private final Window dialog;

  public static SynchroErrorDialogChecker init(Trigger trigger) {
    return new SynchroErrorDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private SynchroErrorDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SynchroErrorDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public SynchroErrorDialogChecker checkMessageContains(String message) {
    assertThat(dialog.getTextBox("message").textContains(message));
    return this;
  }

  public SynchroErrorDialogChecker checkDetailsIntroContains(String text) {
    TextBox details = dialog.getTextBox("detailsIntro");
    assertFalse(details.isEditable());
    assertThat(details.textContains(text));
    return this;
  }

  public SynchroErrorDialogChecker checkDetailsContain(String text) {
    TextBox details = dialog.getTextBox("details");
    assertFalse(details.isEditable());
    assertThat(details.textContains(text));
    return this;
  }

  public SynchroErrorDialogChecker checkCopy() throws Exception {
    dialog.getButton("copy").click();

    String details = dialog.getTextBox("details").getText();
    Assert.assertEquals(details, Clipboard.getContentAsText());

    return this;
  }

  public SynchroErrorDialogChecker switchToDetails() {
    dialog.getButton("link").click();
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
    assertFalse(dialog.isVisible());
  }
}