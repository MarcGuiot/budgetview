package org.designup.picsou.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class AccountEditionChecker extends GuiChecker {
  private Window dialog;

  public static AccountEditionChecker open(Trigger trigger) {
    return new AccountEditionChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private AccountEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public AccountEditionChecker selectBank(String bankName) {
    ComboBox accountBankCombo = dialog.getComboBox("accountBank");
    accountBankCombo.select(bankName);
    return this;
  }

  public AccountEditionChecker checkNoBankSelected() {
    ComboBox accountBankCombo = dialog.getComboBox("accountBank");
    assertThat(accountBankCombo.selectionEquals(null));
    return this;
  }

  public AccountEditionChecker checkSelectedBank(String name) {
    ComboBox accountBankCombo = dialog.getComboBox("accountBank");
    assertThat(accountBankCombo.selectionEquals(name));
    return this;
  }

  public AccountEditionChecker checkAccountName(String name) {
    TextBox accountNameField = dialog.getInputTextBox("name");
    assertThat(accountNameField.textEquals(name));
    return this;
  }

  public AccountEditionChecker setAccountName(final String name) {
    TextBox accountNameField = dialog.getInputTextBox("name");
    accountNameField.setText(name);
    return this;
  }

  public AccountEditionChecker setAccountNumber(final String number) {
    dialog.getInputTextBox("number").setText(number);
    return this;
  }

  public AccountEditionChecker checkAccountNumber(String number) {
    assertThat(dialog.getInputTextBox("number").textEquals(number));
    return this;
  }

  public AccountEditionChecker setBalance(double initialBalance) {
    dialog.getInputTextBox("balance").setText(Double.toString(initialBalance));
    return this;
  }

  public AccountEditionChecker checkBalanceDisplayed(boolean visible) {
    checkComponentVisible(dialog, JTextField.class, "balance", false);
    return this;
  }

  public AccountEditionChecker checkTypes(String... expectedTypeNames) {
    assertThat(getTypeCombo().contentEquals(expectedTypeNames));
    return this;
  }

  public AccountEditionChecker setAsMain() {
    getTypeCombo().select("Main");
    return this;
  }

  public AccountEditionChecker setAsCard() {
    getTypeCombo().select("Card");
    return this;
  }

  public AccountEditionChecker setAsSavings() {
    getTypeCombo().select("Savings");
    return this;
  }

  public AccountEditionChecker checkSavingsWarning() {
    TextBox label = dialog.getTextBox("savingsMessageWarning");
    assertThat(label.isVisible());
    assertThat(label.foregroundNear("red"));
    return this;
  }

  public AccountEditionChecker checkNoSavingsWarning() {
    TextBox label = dialog.getTextBox("savingsMessageWarning");
    assertFalse(label.isVisible());
    return this;
  }

  public AccountEditionChecker checkIsMain() {
    assertThat(getTypeCombo().selectionEquals("Main"));
    return this;
  }

  public AccountEditionChecker checkIsCard() {
    assertThat(getTypeCombo().selectionEquals("Card"));
    return this;
  }

  public AccountEditionChecker checkIsSavings() {
    assertThat(getTypeCombo().selectionEquals("Savings"));
    return this;
  }

  private ComboBox getTypeCombo() {
    return dialog.getComboBox("type");
  }

  public AccountEditionChecker checkValidationError(String message) {
    dialog.getButton("OK").click();
    UISpecAssert.assertTrue(dialog.isVisible());

    TextBox errorLabel = dialog.getTextBox("message");
    assertThat(errorLabel.isVisible());
    assertThat(errorLabel.textEquals(message));
    return this;
  }

  public ConfirmationDialogChecker delete() {
    return ConfirmationDialogChecker.init(dialog.getButton("Delete...").triggerClick());
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
