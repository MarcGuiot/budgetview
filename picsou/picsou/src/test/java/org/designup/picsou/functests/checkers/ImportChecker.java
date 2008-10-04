package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.FileChooserHandler;

public class ImportChecker {
  private Panel dialog;
  private TextBox fileField;
  private Button importButton;

  public ImportChecker(Panel dialog) {
    this.dialog = dialog;
    fileField = dialog.getInputTextBox("fileField");
    importButton = dialog.getButton("Import");
  }

  public ImportChecker setFilePath(String text) {
    fileField.setText(text);
    return this;
  }

  public ImportChecker selectFiles(String... path) {
    StringBuilder builder = new StringBuilder();
    for (String file : path) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file);
    }
    fileField.setText(builder.toString());
    return this;
  }

  public ImportChecker acceptFile() {
    importButton.click();
    return this;
  }

  public ImportChecker checkFileContent(Object[][] expected) {
    Table table = dialog.getTable();
    assertTrue(table.contentEquals(expected));
    return this;
  }

  public ImportChecker checkSelectedFiles(String... files) {
    TextBox fileField = dialog.getTextBox("fileField");
    for (String file : files) {
      assertTrue(fileField.textContains(file));
    }
    return this;
  }

  public ImportChecker checkHeaderMessage(String text) {
    TextBox fileMessage = dialog.findUIComponent(TextBox.class, text);
    assertTrue(fileMessage.isVisible());
    return this;
  }

  public ImportChecker selectBank(String bank) {
    ComboBox accountBankCombo = dialog.getComboBox("accountBank");
    accountBankCombo.select(bank);
    return this;
  }

  public ImportChecker checkDates(String... dates) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertTrue(dateFormatCombo.contentEquals(dates));
    return this;
  }

  public ImportChecker doImport() {
    dialog.getButton("OK").click();
    return this;
  }

  public BalanceEditionChecker doImportWithBalance() {
    return new BalanceEditionChecker(WindowInterceptor.getModalDialog(dialog.getButton("OK").triggerClick()));
  }

  public void close() {
    dialog.getButton("close").click();
  }

  public ImportChecker checkErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("message").textContains(Lang.get(message, arg)));
    return this;
  }

  public ImportChecker selectDate(String dateFormat) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    dateFormatCombo.select(dateFormat);
    return this;
  }

  public ImportChecker enterAccountNumber(String number) {
    dialog.getInputTextBox("number").setText(number);
    return this;
  }

  public void checkCloseButton(String text) {
    assertThat(dialog.getButton("close").textEquals(text));
  }

  public void checkSelectedAccount(String accountNumber) {
    assertThat(dialog.getComboBox("accountCombo").selectionEquals(accountNumber));
  }

  public ImportChecker checkNoErrorMessage() {
    TextBox message = (TextBox)dialog.findUIComponent(ComponentMatchers.innerNameIdentity("message"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    return this;
  }

  public ImportChecker checkFilePath(String path) {
    assertTrue(fileField.textEquals(path));
    return this;
  }

  public ImportChecker browseAndSelect(String path) {
    WindowInterceptor.init(dialog.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();
    return this;
  }

  public ImportChecker selectAccountBank(String bankName) {
    ComboBox accountBankCombo = dialog.getComboBox("accountBank");
    accountBankCombo.select(bankName);
    return this;
  }

  public ImportChecker skipFile() {
    dialog.getButton("Skip").click();
    return this;
  }

  public ImportChecker checkImportMessage(String message) {
    TextBox accountMessage = dialog.getTextBox("importMessage");
    assertTrue(accountMessage.textEquals(message));
    return this;
  }

  public ImportChecker checkAccountsForEntity(String entityId, String[] accounts) {
    assertTrue(dialog.getTextBox("accountNames:" + entityId).textContains(accounts));
    return this;
  }

  public ImportChecker selectBankForEntity(String entityId, String bankName) {
    dialog.getComboBox("bankCombo:" + entityId).select(bankName);
    return this;
  }

  public ImportChecker checkAccountName(String text) {
    TextBox accountNameField = dialog.getInputTextBox("name");
    assertThat(accountNameField.textEquals("Main account"));
    return this;
  }

  public ImportChecker setAccountName(final String name) {
    TextBox accountNameField = dialog.getInputTextBox("name");
    accountNameField.setText(name);
    return this;
  }

  public ImportChecker setAccountNumber(final String number) {
    dialog.getInputTextBox("number").setText(number);
    return this;
  }

  public ImportChecker checkAvailableAccounts(String... accountNames) {
    assertTrue(dialog.getComboBox("accountCombo").contentEquals(accountNames));
    return this;
  }

  public void selectAccount(final String accountName) {
    dialog.getComboBox("accountCombo").select(accountName);
  }

  public ImportChecker checkNoAccountBankSelected() {
    assertTrue(dialog.getComboBox("accountBank").selectionEquals(null));
    return this;
  }
}
