package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;
import org.designup.picsou.utils.Lang;
import org.uispec4j.assertion.UISpecAssert;

public class TransactionCreationChecker extends ViewChecker {
  private Panel panel;

  public TransactionCreationChecker(Window mainWindow) {
    super(mainWindow);
  }

  public TransactionCreationChecker setAmount(double amount) {
    checkShowing();
    TextBox textBox = getPanel().getInputTextBox("amountEditor");
    if (amount < 0) {
      getPanel().getToggleButton("negativeAmount").click();
    }
    else {
      getPanel().getToggleButton("positiveAmount").click();
    }
    textBox.setText(toString(Math.abs(amount)), false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker enterAmountWithoutValidating(double amount) {
    checkShowing();
    TextBox textBox = getPanel().getInputTextBox("amountEditor");
    textBox.setText(toString(amount), false);
    return this;
  }

  public TransactionCreationChecker checkAmount(double amount) {
    assertThat(getPanel().getInputTextBox("amountEditor").textEquals(toString(amount)));
    return this;
  }

  public TransactionCreationChecker checkPositiveAmountsSelected() {
    assertThat(getPanel().getToggleButton("positiveAmount").isSelected());
    assertFalse(getPanel().getToggleButton("negativeAmount").isSelected());
    return this;
  }

  public TransactionCreationChecker checkNegativeAmountsSelected() {
    assertThat(getPanel().getToggleButton("negativeAmount").isSelected());
    assertFalse(getPanel().getToggleButton("positiveAmount").isSelected());
    return this;
  }

  public TransactionCreationChecker setDay(int day) {
    checkShowing();
    TextBox textBox = getPanel().getInputTextBox("day");
    textBox.setText(Integer.toString(day), false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker enterDayWithoutValidating(int day) {
    checkShowing();
    TextBox textBox = getPanel().getInputTextBox("day");
    textBox.setText(Integer.toString(day), false);
    return this;
  }

  public TransactionCreationChecker checkDay(int day) {
    assertThat(getPanel().getInputTextBox("day").textEquals(Integer.toString(day)));
    return this;
  }

  public TransactionCreationChecker checkMonth(String text) {
    assertThat(getPanel().getTextBox("month").textEquals(text));
    return this;
  }

  public TransactionCreationChecker setLabel(String label) {
    checkShowing();
    TextBox textBox = getPanel().getInputTextBox("label");
    textBox.setText(label, false);
    textBox.focusLost();
    return this;
  }

  public TransactionCreationChecker enterLabelWithoutValidating(String label) {
    TextBox textBox = getPanel().getInputTextBox("label");
    textBox.setText(label, false);
    return this;
  }

  public TransactionCreationChecker create() {
    checkShowing();
    Panel panel = getPanel();
    panel.getButton("Create").click();
    TextBox errorMessage = panel.getTextBox("errorMessage");
    if (errorMessage.isVisible().isTrue()) {
      Assert.fail("Creation error: " + errorMessage.getText());
    }
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
    assertThat(getPanel().getInputTextBox("amountEditor").textIsEmpty());
    assertThat(getPanel().getInputTextBox("day").textIsEmpty());
    assertThat(getPanel().getInputTextBox("label").textIsEmpty());
    return this;
  }

  public TransactionCreationChecker show() {
    getShowHideButton().click();
    checkShowing();
    return this;
  }

  public TransactionCreationChecker checkShowing() {
    checkPanelVisible(true);
    assertThat(getShowHideButton().textEquals(Lang.get("hide")));
    return this;
  }

  private Button getShowHideButton() {
    views.selectCategorization();
    return mainWindow.getButton("showHideTransactionCreation");
  }

  public TransactionCreationChecker hide() {
    getShowHideButton().click();
    return checkHidden();
  }

  public TransactionCreationChecker checkHidden() {
    views.selectCategorization();
    checkPanelVisible(false);
    assertThat(getShowHideButton().textEquals(Lang.get("transactionCreation.show")));
    return this;
  }

  private TransactionCreationChecker checkPanelVisible(boolean visible) {
    checkComponentVisible(mainWindow, JPanel.class, "transactionCreation", visible);
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
    return AccountEditionChecker.open(dialog.getOkTrigger("Create a manual account"));
  }

  public TransactionCreationChecker createAndCheckErrorMessage(String message) {
    getPanel().getButton("Create").click();
    TextBox textBox = getPanel().getTextBox("errorMessage");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(message));
    return this;
  }

  public void checkDemoMessage() {
    MessageDialogChecker.init(getShowHideButton().triggerClick())
      .checkMessageContains("You cannot create transactions in the demo account")
      .close();
  }

  public TransactionCreationChecker checkNoErrorMessage() {
    checkComponentVisible(getPanel(), JLabel.class, "errorMessage", false);
    return this;
  }

  public void checkTrialExpiredMessage() {
    LicenseActivationChecker.open(getShowHideButton().triggerClick())
      .checkCodeIsEmpty()
      .cancel();
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectCategorization();
      panel = mainWindow.getPanel("transactionCreation");
    }
    return panel;
  }
}
