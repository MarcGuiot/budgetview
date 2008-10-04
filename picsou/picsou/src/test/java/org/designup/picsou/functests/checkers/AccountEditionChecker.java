package org.designup.picsou.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class AccountEditionChecker {
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

  public AccountEditionChecker checkAccountName(String text) {
    TextBox accountNameField = dialog.getInputTextBox("name");
    assertThat(accountNameField.textEquals("Main account"));
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

  public AccountEditionChecker setBalance(double initialBalance) {
    dialog.getInputTextBox("balance").setText(Double.toString(initialBalance));
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
