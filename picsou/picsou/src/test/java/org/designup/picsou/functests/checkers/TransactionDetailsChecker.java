package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class TransactionDetailsChecker extends GuiChecker {
  private Window window;

  public TransactionDetailsChecker(Window window) {
    this.window = window;
  }

  private Panel getPanel() {
    return window.getPanel("transactionDetails");
  }

  public void checkLabel(String expected) {
    checkValue("userLabel", expected);
  }

  public void checkLabelIsNotEditable() {
    assertFalse(getPanel().getTextBox("userLabel").isEditable());
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  private void checkNotVisible(String name) {
    assertFalse(getPanel().getTextBox(name).isVisible());
  }

  public void checkSplitNotVisible() {
    checkComponentVisible(getPanel(), JButton.class, "split", false);
  }

  public void checkSplitVisible() {
    assertTrue(getPanel().getButton("split").isVisible());
  }

  public void checkSplitButtonLabel(String text) {
    assertTrue(getPanel().getButton("split").textEquals(text));
  }

  public void split(String amount, String note) {
    Button splitLink = getPanel().getButton("split");
    SplitDialogChecker splitDialogChecker =
      new SplitDialogChecker(WindowInterceptor.getModalDialog(splitLink.triggerClick()));
    splitDialogChecker.enterAmount(amount);
    splitDialogChecker.enterNote(note);
    splitDialogChecker.validate();
  }

  public void checkOriginalLabelNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "originalLabel", false);
  }

  public void checkOriginalLabel(String originalLabel) {
    assertThat(getPanel().getTextBox("originalLabel").textEquals(originalLabel));
  }

  public void checkType(TransactionType transactionType) {
    TextBox box = getPanel().getTextBox("transactionType");
    assertTrue(box.isVisible());
    assertThat(box.textEquals(Lang.get("transactionType." + transactionType.getName())));
  }

  public void checkTypeNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "transactionType", false);
  }

  public void checkBankDate(String yyyyMMdd) {
    TextBox bankDate = getPanel().getTextBox("bankDate");
    assertTrue(bankDate.isVisible());
    assertThat(bankDate.textEquals(yyyyMMdd));
  }

  public void checkBankDateNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "bankDate", false);
  }

  public SplitDialogChecker openSplitDialog() {
    Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton("splitLink").triggerClick());
    return new SplitDialogChecker(dialog);
  }

  public void checkSplitButtonAvailable() {
    Button splitMessage = getPanel().getButton("splitLink");
    assertTrue(UISpecAssert.and(splitMessage.isVisible(), splitMessage.isEnabled()));
  }

  public TransactionDetailsChecker checkNote(String text) {
    TextBox textBox = getPanel().getInputTextBox("noteField");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(text));
    return this;
  }

  public TransactionDetailsChecker setNote(String text) {
    getPanel().getInputTextBox("noteField").setText(text);
    return this;
  }

  public void checkNotesFieldNotVisible() {
    checkComponentVisible(getPanel(), JTextField.class, "noteField", false);
  }

  public void checkMessage(String message) {
    assertThat(getPanel().getTextBox().textContains(message));
  }

  public void checkAccount(String text) {
    TextBox textBox = getPanel().getTextBox("account");
    assertThat(textBox.textEquals(text));
    assertThat(textBox.isVisible());
  }

  public void checkNoAccountDisplayed() {
    checkComponentVisible(getPanel(), JLabel.class, "account", false);
  }

  public void shift() {
    openShiftDialog().validate();
  }

  public ConfirmationDialogChecker openShiftDialog() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Shift..."));
    return ConfirmationDialogChecker.init(button.triggerClick());
  }

  public void checkShiftEnabled() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Shift..."));
  }

  public void checkShiftInverted() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Cancel shift"));
  }

  public void checkShiftDisabled() {
    checkComponentVisible(getPanel(), JButton.class, "shift", false);
  }

  public void unshift() {
    Button button = getPanel().getButton("shift");
    assertThat(button.textEquals("Cancel shift"));
    button.click();
  }

  public void checkBudgetDate(String yyyyMMdd) {
    TextBox budgetDate = getPanel().getTextBox("budgetDate");
    assertTrue(budgetDate.isVisible());
    assertThat(budgetDate.textEquals(yyyyMMdd));

  }

  public void checkBudgetDateNotVisible(String label) {
    TextBox budgetDate = getPanel().getTextBox("budgetDate");
    assertFalse(budgetDate.isVisible());
  }
}
