package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class TransactionCreationChecker extends GuiChecker {
  private Window window;

  public TransactionCreationChecker(Window window) {
    this.window = window;
  }

  public TransactionCreationChecker setAmount(double amount) {
    TextBox textBox = getPanel().getInputTextBox("amount");
    textBox.setText(toString(amount), false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker checkAmount(double amount) {
    assertThat(getPanel().getInputTextBox("amount").textEquals(toString(amount)));
    return this;
  }

  public TransactionCreationChecker setDay(int day) {
    TextBox textBox = getPanel().getInputTextBox("day");
    textBox.setText(Integer.toString(day), false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker checkDay(int day) {
    assertThat(getPanel().getInputTextBox("day").textEquals(Integer.toString(day)));
    return this;
  }

  public TransactionCreationChecker setLabel(String label) {
    TextBox textBox = getPanel().getInputTextBox("label");
    textBox.setText(label, false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker checkLabel(String label) {
    assertThat(getPanel().getInputTextBox("label").textEquals(label));
    return this;
  }

  public TransactionCreationChecker create() {
    getPanel().getButton("Create").click();
    return this;
  }

  public TransactionCreationChecker checkAccounts(String... accountNames) {
    assertThat(getPanel().getComboBox("account").contentEquals(accountNames));
    return this;
  }

  public TransactionCreationChecker checkAccount(String accountName) {
    assertThat(getPanel().getComboBox("account").selectionEquals(accountName));
    return this;
  }

  public TransactionCreationChecker selectAccount(String accountName) {
    getPanel().getComboBox("account").select(accountName);
    return this;
  }

  public TransactionCreationChecker checkFieldsAreEmpty() {
    assertThat(getPanel().getInputTextBox("amount").textIsEmpty());
    assertThat(getPanel().getInputTextBox("day").textIsEmpty());
    assertThat(getPanel().getInputTextBox("label").textIsEmpty());
    return this;
  }

  public TransactionCreationChecker show() {
    getShowHideButton().click();
    return checkShowing();
  }

  public TransactionCreationChecker checkShowing() {
    checkVisible(true);
    assertThat(getShowHideButton().textEquals("Hide"));
    return this;
  }

  private Button getShowHideButton() {
    return window.getButton("showHideTransactionCreation");
  }

  public TransactionCreationChecker hide() {
    getShowHideButton().click();
    return checkHidden();
  }

  public TransactionCreationChecker checkHidden() {
    checkVisible(false);
    assertThat(getShowHideButton().textEquals("Input operations manually"));
    return this;
  }

  private TransactionCreationChecker checkVisible(boolean visible) {
    checkComponentVisible(window, JPanel.class, "transactionCreation", visible);
    return this;
  }

  public TransactionCreationChecker checkNoAccountAvailableMessage() {
    ConfirmationDialogChecker dialog = ConfirmationDialogChecker.init(getShowHideButton().triggerClick());
    dialog.checkTitle("No manual accounts");
    dialog.checkMessageContains("you must create a dedicated account");
    dialog.cancel();
    return this;
  }

  public AccountEditionChecker checkNoAccountMessageAndOpenCreationDialog() {
    ConfirmationDialogChecker dialog = ConfirmationDialogChecker.init(getShowHideButton().triggerClick());
    dialog.checkTitle("No manual accounts");
    dialog.checkMessageContains("you must create a dedicated account");
    return AccountEditionChecker.open(dialog.getOkTrigger());
  }

  public TransactionCreationChecker checkErrorMessage(String message) {
    TextBox textBox = getPanel().getTextBox("errorMessage");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(message));
    return this;
  }

  public TransactionCreationChecker checkNoErrorMessage() {
    checkComponentVisible(getPanel(), JLabel.class, "errorMessage", false);
    return this;
  }

  public void checkTrialExpiredMessage() {
    LicenseActivationChecker.open(getShowHideButton().triggerClick())
      .checkFieldsAreEmpty()
      .cancel();
  }

  private Panel getPanel() {
    return window.getPanel("transactionCreation");
  }
}
