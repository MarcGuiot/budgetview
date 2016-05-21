package com.budgetview.functests.checkers;

import junit.framework.Assert;
import com.budgetview.functests.checkers.components.AmountEditorChecker;
import com.budgetview.utils.Lang;
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

  public TransactionEditionChecker checkLabelError(String errorMessage) {
    dialog.getButton("OK").click();
    assertThat(dialog.isVisible());
    checkTipVisible(dialog, dialog.getTextBox("labelEditor"), errorMessage);
    return this;
  }

  public TransactionEditionChecker checkDateAndAmountShown() {
    checkEditsLabelOnly(false);
    return this;
  }

  public TransactionEditionChecker checkImportedTransactionsMessage() {
    checkEditsLabelOnly(true);
    dialog.getTextBox("notice").textEquals(Lang.get("transaction.edition.notice.imported"));
    return this;
  }

  public TransactionEditionChecker checkMultiselectionMessage() {
    checkEditsLabelOnly(true);
    dialog.getTextBox("notice").textEquals(Lang.get("transaction.edition.notice.imported"));
    return this;
  }

  private void checkEditsLabelOnly(boolean labelOnly) {
    checkComponentVisible(dialog, JPanel.class, "dateAndAmount", !labelOnly);
    checkComponentVisible(dialog, JEditorPane.class, "notice", labelOnly);
  }

  public TransactionEditionChecker checkNoTipShown() {
    checkNoTipVisible(dialog);
    return this;
  }

  public TransactionEditionChecker checkDay(String text) {
    assertThat(dialog.getTextBox("day").textEquals(text));
    return this;
  }

  public TransactionEditionChecker setDay(String text) {
    dialog.getTextBox("day").setText(text);
    return this;
  }

  public TransactionEditionChecker validateAndCheckDayError(String errorMessage) {
    dialog.getButton("OK").click();
    checkTipVisible(dialog, dialog.getInputTextBox("day"), errorMessage);
    assertTrue(dialog.isVisible());
    return this;
  }

  public TransactionEditionChecker checkMonth(int monthId) {
    MonthChooserChecker.open(dialog.getButton("month").triggerClick()).checkSelectedInCurrentMonth(monthId);
    return this;
  }

  public TransactionEditionChecker setMonth(int monthId) {
    MonthChooserChecker.open(dialog.getButton("month").triggerClick()).selectMonth(monthId);
    return this;
  }

  public TransactionEditionChecker checkAmount(double amount) {
    AmountEditorChecker.init(dialog, "amountEditor").checkAmount(amount);
    return this;
  }

  public TransactionEditionChecker setAmount(double amount) {
    AmountEditorChecker.init(dialog, "amountEditor").set(amount);
    return this;
  }

  public TransactionEditionChecker clearAmount() {
    AmountEditorChecker.init(dialog, "amountEditor").clear();
    return this;
  }

  public TransactionEditionChecker validateAndCheckAmountError(String errorMessage) {
    dialog.getButton("OK").click();
    checkTipVisible(dialog, dialog.getInputTextBox("amountEditionField"), errorMessage);
    assertTrue(dialog.isVisible());
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    checkNoTipVisible(dialog);
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }
}
