package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class TransactionEditionChecker extends GuiChecker {

  private Window dialog;

  public static TransactionEditionChecker open(Trigger trigger) {
    return new TransactionEditionChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private TransactionEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public TransactionEditionChecker checkTitle(String text) {
    assertThat(dialog.getTextBox("title").textEquals(text));
    return this;
  }

  public TransactionEditionChecker setLabel(String text) {
    dialog.getTextBox("labelEditor").setText(text, false);
    return this;
  }

  public void setLabelAndPressEnter(String text) {
    dialog.getTextBox("labelEditor").setText(text, true);
    assertFalse(dialog.isVisible());
  }

  public TransactionEditionChecker checkLabelSelected() {
    JTextField textEditor = (JTextField)dialog.getInputTextBox("labelEditor").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
    return this;
  }

  public TransactionEditionChecker checkOriginalLabel(String text) {
    assertThat(dialog.getTextBox("originalLabel").textEquals(text));
    return this;
  }

  public TransactionEditionChecker checkValidationError(String errorMessage) {
    dialog.getButton("OK").click();
    assertThat(dialog.isVisible());
    checkTipVisible(dialog, dialog.getTextBox("labelEditor"), errorMessage);
    return this;
  }

  public TransactionEditionChecker checkNoTipShown() {
    checkNoTipVisible(dialog);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }
}
