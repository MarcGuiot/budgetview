package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.assertion.Assertion;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.jdesktop.swingx.JXDatePicker;
import org.globsframework.utils.Dates;

import javax.swing.*;
import java.awt.*;

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
    TextBox textBox = dialog.getInputTextBox("position");
    assertThat(textBox.isVisible());
    textBox.setText(Double.toString(initialBalance));
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

  public AccountEditionChecker setUpdateModeToManualInput() {
    return setUpdateMode("Manual input");
  }

  public AccountEditionChecker setUpdateModeToFileImport() {
    return setUpdateMode("File import");
  }

  private AccountEditionChecker setUpdateMode(String mode) {
    getUpdateModeCombo().select(mode);
    return this;
  }

  public AccountEditionChecker checkUpdateModes() {
    assertThat(getUpdateModeCombo().contentEquals("File import", "Manual input"));
    return this;
  }

  public AccountEditionChecker checkUpdateModeIsFileImport() {
    assertThat(getUpdateModeCombo().selectionEquals("File import"));
    return this;
  }
  public AccountEditionChecker checkUpdateModeIsManualInput() {
    assertThat(getUpdateModeCombo().selectionEquals("Manual input"));
    return this;
  }

  private AccountEditionChecker checkUpdateMode(String mode) {
    assertThat(getUpdateModeCombo().selectionEquals(mode));
    return this;
  }

  public AccountEditionChecker checkUpdateModeIsEnabled() {
    assertThat(getUpdateModeCombo().isEnabled());
    return this;
  }

  public AccountEditionChecker checkUpdateModeIsDisabled() {
    assertFalse(getUpdateModeCombo().isEnabled());
    return this;
  }

  public ComboBox getUpdateModeCombo() {
    return dialog.getComboBox("updateMode");
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

  public void doDelete() {
    delete().validate();
  }

  public void validate() {
    dialog.getButton("OK").click();
    if (dialog.isVisible().isTrue()) {
      final String message = dialog.getTextBox("message").getText();
      Assert.fail("Validation failed. Error message: " + message);
    }
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }

  public AccountEditionChecker setStartDate(String date) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "startDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    ((JXDatePicker)swingComponents[0]).setDate(Dates.parse(date));
    return this;
  }

  public AccountEditionChecker checkStartDate(String date) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "startDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    Assert.assertEquals(Dates.parse(date), ((JXDatePicker)swingComponents[0]).getDate());
    return this;
  }

  public AccountEditionChecker setEndDate(String date) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    ((JXDatePicker)swingComponents[0]).setDate(Dates.parse(date));
    return this;
  }

  public AccountEditionChecker checkEndDate(String date) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    Assert.assertEquals(Dates.parse(date), ((JXDatePicker)swingComponents[0]).getDate());
    return this;
  }

  public AccountEditionChecker cancelStartDate(){
    dialog.getButton("removeStartDate").click();
    assertThat(new Assertion() {
      public void check() throws Exception {
        Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "startDatePicker");
        Assert.assertEquals(1, swingComponents.length);
        Assert.assertNull(((JXDatePicker)swingComponents[0]).getDate());
      }
    });
    return this;
  }

  public AccountEditionChecker cancelEndDate(){
    dialog.getButton("removeEndDate").click();
    assertThat(new Assertion() {
      public void check() throws Exception {
        Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
        Assert.assertEquals(1, swingComponents.length);
        Assert.assertNull(((JXDatePicker)swingComponents[0]).getDate());
      }
    });
    return this;
  }
}
