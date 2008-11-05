package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class AccountEditionChecker extends DataChecker {
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

  public AccountEditionChecker setAsDay() {
    getDayButton().click();
    return this;
  }

  private RadioButton getDayButton() {
    return dialog.getRadioButton("defautAccount");
  }

  public AccountEditionChecker setAsSaving() {
    getSavingButton().click();
    return this;
  }

  private RadioButton getSavingButton() {
    return dialog.getRadioButton("savingAccount");
  }

  public AccountEditionChecker setAsCreditCard() {
    getCardAccount().click();
    return this;
  }

  private RadioButton getCardAccount() {
    return dialog.getRadioButton("cardAccount");
  }

  public AccountEditionChecker checkIsDay() {
    assertThat(getDayButton().isSelected());
    assertFalse(getSavingButton().isSelected());
    assertFalse(getCardAccount().isSelected());
    return this;
  }

  public AccountEditionChecker checkIsSaving() {
    assertFalse(getDayButton().isSelected());
    assertThat(getSavingButton().isSelected());
    assertFalse(getCardAccount().isSelected());
    return this;
  }

  public AccountEditionChecker checkIsCard() {
    assertFalse(getDayButton().isSelected());
    assertFalse(getSavingButton().isSelected());
    assertThat(getCardAccount().isSelected());
    return this;
  }

  public AccountEditionChecker checkValidationError(String message) {
    dialog.getButton("OK").click();
    UISpecAssert.assertTrue(dialog.isVisible());

    TextBox errorLabel = dialog.getTextBox("message");
    assertThat(errorLabel.isVisible());
    assertThat(errorLabel.textEquals(message));
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

  public AccountEditionChecker checkIsImported() {
    assertThat(dialog.getCheckBox("importedAccount").isSelected());
    return this;
  }

  public AccountEditionChecker clickOnImported() {
    dialog.getCheckBox().click();
    return this;
  }

  public AccountEditionChecker checkNotImported() {
    assertFalse(dialog.getCheckBox("importedAccount").isSelected());
    return this;
  }
}
