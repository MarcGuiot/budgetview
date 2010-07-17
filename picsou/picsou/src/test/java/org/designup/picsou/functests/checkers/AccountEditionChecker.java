package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.Dates;
import org.jdesktop.swingx.JXDatePicker;
import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class AccountEditionChecker extends GuiChecker {
  private Window dialog;
  private CardEditionPanelChecker cardEditionPanelChecker;

  public static AccountEditionChecker open(Trigger trigger) {
    return new AccountEditionChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private AccountEditionChecker(Window dialog) {
    this.dialog = dialog;
    cardEditionPanelChecker = new CardEditionPanelChecker(dialog);
  }

  public AccountEditionChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public AccountEditionChecker selectBank(String bankName) {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(dialog.getButton("bankSelector").triggerClick());
    BankChooserChecker chooserChecker = new BankChooserChecker(bankChooserWindow);
    chooserChecker.selectBank(bankName);
    chooserChecker.validate();
    return this;
  }

  public BankChooserChecker openBankSelection() {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(dialog.getButton("bankSelector").triggerClick());
    return new BankChooserChecker(bankChooserWindow);
  }


  public AccountEditionChecker checkNoBankSelected() {
    assertThat(dialog.getTextBox("bankLabel").textEquals(""));
    assertThat(dialog.getButton("bankSelector").textEquals("Click to select a bank"));
    return this;
  }

  public AccountEditionChecker checkSelectedBank(String name) {
    assertThat(dialog.getTextBox("bankLabel").textEquals(name));
    assertThat(dialog.getButton("bankSelector").textEquals("Modify"));
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

  public AccountEditionChecker setPosition(double amount) {
    TextBox textBox = dialog.getInputTextBox("position");
    assertThat(textBox.isVisible());
    textBox.setText(Double.toString(amount));
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

  public AccountEditionChecker setAsDeferredCard() {
    getTypeCombo().select("Deferred debit card");
    return this;
  }

  public AccountEditionChecker setAsCreditCard() {
    getTypeCombo().select("Credit card");
    return this;
  }

  public AccountEditionChecker checkCreditCardWarning() {
    checkComponentVisible(dialog, JTextArea.class, "creditMessage", true);
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

  public AccountEditionChecker checkIsDeferredCard() {
    assertThat(getTypeCombo().selectionEquals("Deferred debit card"));
    return this;
  }

  public AccountEditionChecker checkIsCreditCard() {
    assertThat(getTypeCombo().selectionEquals("Credit card"));
    return this;
  }

  public AccountEditionChecker checkIsSavings() {
    assertThat(getTypeCombo().selectionEquals("Savings"));
    return this;
  }

  public AccountEditionChecker checkAccountTypeNotEditable() {
    assertFalse(getTypeCombo().isEnabled());
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

  public AccountEditionChecker checkUpdateModeIsEditable() {
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

  public AccountEditionChecker setStartDate(String yyyyMMdd) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "startDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    ((JXDatePicker)swingComponents[0]).setDate(Dates.parse(yyyyMMdd));
    return this;
  }

  public AccountEditionChecker checkStartDate(String yyyyMMdd) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "startDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    Assert.assertEquals(Dates.parse(yyyyMMdd), ((JXDatePicker)swingComponents[0]).getDate());
    return this;
  }

  public AccountEditionChecker setEndDate(String yyyyMMdd) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    ((JXDatePicker)swingComponents[0]).setDate(Dates.parse(yyyyMMdd));
    return this;
  }

  public AccountEditionChecker checkEndDate(String yyyyMMdd) {
    Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
    Assert.assertEquals(1, swingComponents.length);
    Assert.assertEquals(Dates.parse(yyyyMMdd), ((JXDatePicker)swingComponents[0]).getDate());
    return this;
  }

  public AccountEditionChecker cancelEndDate() {
    dialog.getButton("removeEndDate").click();
    assertThat(new Assertion() {
      public void check() {
        Component[] swingComponents = dialog.getSwingComponents(JXDatePicker.class, "endDatePicker");
        Assert.assertEquals(1, swingComponents.length);
        Assert.assertNull(((JXDatePicker)swingComponents[0]).getDate());
      }
    });
    return this;
  }

  public CardEditionPanelChecker getCardEditionPanelChecker() {
    return cardEditionPanelChecker;
  }

  public AccountEditionChecker setFromBeginningDay(int day) {
    cardEditionPanelChecker.setDayFromBegining(day);
    return this;
  }

  public AccountEditionChecker addMonth() {
    cardEditionPanelChecker.addMonth();
    return this;
  }

  public AccountEditionChecker checkMonth(int month) {
    cardEditionPanelChecker.checkMonth(month);
    return this;
  }

  public AccountEditionChecker setDay(int month, int day) {
    cardEditionPanelChecker.setDay(month, day);
    return this;
  }

  public AccountEditionChecker checkFromBeginningDay(int day) {
    cardEditionPanelChecker.checkFromBeginningDay(day);
    return this;
  }

  public AccountEditionChecker checkBeginningUnchangeable() {
    cardEditionPanelChecker.checkBeginningUnchangeable();
    return this;
  }

  public AccountEditionChecker checkDay(int month, int day) {
    cardEditionPanelChecker.checkDay(month, day);
    return this;
  }

  public AccountEditionChecker changeMonth(int month, int newMonth) {
    cardEditionPanelChecker.changeMonth(month, newMonth);
    checkMonth(newMonth);
    return this;
  }

  public AccountEditionChecker checkFromBeginning() {
    cardEditionPanelChecker.checkFromBeginning();
    return this;
  }

  public AccountEditionChecker checkPeriod(Integer[][] periods) {
    cardEditionPanelChecker.checkPeriod(periods);
    return this;
  }

  public AccountEditionChecker delete(int month) {
    cardEditionPanelChecker.delete(month);
    return this;
  }
}
