package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.Dates;
import org.globsframework.model.format.Formats;
import org.jdesktop.swingx.JXDatePicker;
import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;

public class AccountEditionChecker extends GuiChecker {
  private org.uispec4j.Panel dialog;
  private CardEditionPanelChecker cardEditionPanelChecker;

  public static AccountEditionChecker open(Trigger trigger) {
    return new AccountEditionChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public AccountEditionChecker(org.uispec4j.Panel dialog) {
    this.dialog = dialog;
    cardEditionPanelChecker = new CardEditionPanelChecker(dialog);
  }

  public AccountEditionChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public AccountEditionChecker selectBank(String bankName) {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(getBankButton().triggerClick());
    BankChooserChecker chooserChecker = new BankChooserChecker(bankChooserWindow);
    chooserChecker.selectBank(bankName);
    chooserChecker.validate();
    return this;
  }

  private org.uispec4j.Button getBankButton() {
    return dialog.getButton("bankSelector");
  }

  public BankChooserChecker openBankSelection() {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(getBankButton().triggerClick());
    return new BankChooserChecker(bankChooserWindow);
  }

  public AccountEditionChecker checkNoBankSelected() {
    assertThat(dialog.getTextBox("bankLabel").textEquals(""));
    assertThat(getBankButton().textEquals("Click to select a bank"));
    return this;
  }

  public AccountEditionChecker checkSelectedBank(String name) {
    assertThat(dialog.getTextBox("bankLabel").textEquals(name));
    assertThat(getBankButton().textEquals("Modify"));
    return this;
  }

  public AccountEditionChecker checkAccountName(String name) {
    TextBox accountNameField = getNameEditor();
    assertThat(accountNameField.textEquals(name));
    return this;
  }

  public AccountEditionChecker checkAstericsErrorOnName(){
    assertThat(dialog.getTextBox("nameFlag").foregroundNear("FF0000"));
    return this;
  }

  public AccountEditionChecker checkAstericsClearOnName(){
    assertThat(dialog.getTextBox("nameFlag").foregroundNear("FFFFFF"));
    return this;
  }

  public AccountEditionChecker checkAstericsErrorOnBank(){
    assertThat(dialog.getTextBox("bankFlag").foregroundNear("FF0000"));
    return this;
  }

  public AccountEditionChecker checkAstericsClearOnBank(){
    assertThat(dialog.getTextBox("bankFlag").foregroundNear("FFFFFF"));
    return this;
  }

  public AccountEditionChecker checkAstericsErrorOnType(){
    assertThat(dialog.getTextBox("accountTypeFlag").foregroundNear("FF0000"));
    return this;
  }

  public AccountEditionChecker checkAstericsClearOnType(){
    assertThat(dialog.getTextBox("accountTypeFlag").foregroundNear("FFFFFF"));
    return this;
  }

  public AccountEditionChecker setAccountName(final String name) {
    TextBox accountNameField = getNameEditor();
    accountNameField.setText(name);
    return this;
  }

  public AccountEditionChecker checkNameValidationError(String message) {
    dialog.getButton("OK").click();
    UISpecAssert.assertTrue(dialog.isVisible());

    checkErrorTipVisible(dialog, getNameEditor(), message);
    return this;
  }

  private TextBox getNameEditor() {
    return dialog.getInputTextBox("name");
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

  public AccountEditionChecker checkPosition(double amount) {
    TextBox textBox = dialog.getInputTextBox("position");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(Formats.DEFAULT_DECIMAL_FORMAT.format(amount)));
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

  public AccountEditionChecker checkTypesHelp(String title) {
    HelpChecker.open(dialog.getButton("accountTypeHelp").triggerClick()).checkTitle(title).close();
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
    dialog.getTextBox("messageWarning")
      .textContains("Operations in main acount should be clasified in Budget Area 'other'");
    return this;
  }

  public AccountEditionChecker checkDeferredWarning() {
    dialog.getTextBox("messageWarning").textContains("Credit card are not managed");
    return this;
  }

  public AccountEditionChecker setAsSavings() {
    getTypeCombo().select("Savings");
    return this;
  }

  public AccountEditionChecker checkSavingsWarning() {
    TextBox label = dialog.getTextBox("messageWarning");
    assertThat(label.isVisible());
    assertThat(label.foregroundNear("red"));
    return this;
  }

  public AccountEditionChecker checkNoSavingsWarning() {
    TextBox label = dialog.getTextBox("messageWarning");
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

  public AccountEditionChecker checkAccountTypeEditable() {
    assertTrue(getTypeCombo().isEnabled());
    return this;
  }

  public AccountEditionChecker checkAccountTypeNotEditable() {
    assertFalse(getTypeCombo().isEnabled());
    return this;
  }

  public ComboBox getTypeCombo() {
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

  public AccountEditionChecker checkBankValidationError(String message) {
    dialog.getButton("OK").click();
    UISpecAssert.assertTrue(dialog.isVisible());

    checkErrorTipVisible(dialog, getBankButton(), message);
    return this;
  }

  public AccountEditionChecker checkNoErrorDisplayed() {
    checkNoErrorTip(dialog);
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
      if (dialog.getTextBox("message").isVisible().isTrue()) {
        final String message = dialog.getTextBox("message").getText();
        Assert.fail("Validation failed. Error message: " + message);
      }
      else {
        checkNoErrorTip(dialog);
      }
      Assert.fail("Creation failed");
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

  public AccountEditionChecker checkDisplayedStartDate(String text) {
    TextBox textBox = dialog.getTextBox("openDateField");
    UISpecAssert.assertThat(textBox.textEquals(text));
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

  public AccountEditionChecker checkDisplayedEndDate(String text) {
    TextBox textBox = dialog.getTextBox("closed");
    UISpecAssert.assertThat(textBox.textEquals(text));
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

  public void checkNameMissing() {
    checkSignpostVisible(dialog, getNameEditor(), "You must enter a name for this account");
  }

  public String getAccountName() {
    return dialog.getInputTextBox("name").getText();
  }

  public void checkAccountDisable() {
    assertFalse(getTypeCombo().isEnabled());
    assertFalse(getBankButton().isEnabled());
    assertFalse(getNameEditor().isEnabled());
  }
}
