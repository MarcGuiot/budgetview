package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;

public class ImportChecker {
  private Panel dialog;
  private TextBox fileField;
  private Button importButton;

  public static ImportChecker open(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new ImportChecker(window);
  }

  public ImportChecker(Panel dialog) {
    this.dialog = dialog;
    fileField = dialog.getInputTextBox("fileField");
    importButton = dialog.getButton("Import");
  }

  private ImportChecker() {
  }

  static public ImportChecker create(Panel dialog){
    ImportChecker importChecker = new ImportChecker();
    importChecker.dialog = dialog;
    return importChecker;
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
    dialog.getButton(Lang.get("import.ok")).click();
    return this;
  }

  public void completeImport() {
    dialog.getButton(Lang.get("import.ok")).click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public AccountPositionEditionChecker doImportWithBalance() {
    return new AccountPositionEditionChecker(WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("import.ok")).triggerClick()));
  }

  public void close() {
    dialog.getButton("close").click();
  }

  public ImportChecker checkErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("importMessage").textContains(Lang.get(message, arg)));
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

  public ImportChecker checkSelectedAccount(String accountNumber) {
    assertThat(dialog.getComboBox("accountCombo").selectionEquals(accountNumber));
    return this;
  }

  public ImportChecker checkNoErrorMessage() {
    TextBox message = (TextBox)dialog.findUIComponent(ComponentMatchers.innerNameIdentity("importMessage"));
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

  public ImportChecker skipFile() {
    dialog.getButton("Skip").click();
    return this;
  }

  public ImportChecker checkMessageCreateFirstAccount() {
    dialog.getTextBox("You must create an account");
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

  public ImportChecker selectAccount(final String accountName) {
    dialog.getComboBox("accountCombo").select(accountName);
    return this;
  }

  public ImportChecker checkNoAccountBankSelected() {
    assertTrue(dialog.getComboBox("accountBank").selectionEquals(null));
    return this;
  }

  public AccountEditionChecker openAccount(){
    return AccountEditionChecker.open(dialog.getButton("Create an account").triggerClick());
  }

  public ImportChecker defineAccount(String bank, String accountName, String number) {
    AccountEditionChecker accountEditionChecker = 
      AccountEditionChecker.open(dialog.getButton("Create an account").triggerClick());
    accountEditionChecker.selectBank(bank)
      .checkAccountName("Main account")
      .setAccountName(accountName)
      .setAccountNumber(number);
    accountEditionChecker.validate();
    return this;
  }


  public ImportChecker createNewAccount(String bank, String accountName, String number, double initialBalance) {
    AccountEditionChecker.open(dialog.getButton("newAccount").triggerClick())
      .selectBank(bank)
      .checkUpdateModeIsDisabled()
      .checkUpdateModeIsFileImport()
      .checkUpdateModes()
      .setAccountName(accountName)
      .setAccountNumber(number)
      .setPosition(initialBalance)
      .validate();
    return this;
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }

  public ImportChecker checkDirectory(String directory) {
    WindowInterceptor.init(dialog.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().assertCurrentDirEquals(new File(directory)).cancelSelection())
      .run();
    return this;
  }

  public BankEntityEditionChecker openEntityEditionChecker() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton("Set the bank").triggerClick());
    return new BankEntityEditionChecker(window);
  }

  public ImportChecker checkMessageSelectABank() {
    dialog.getTextBox("You must select a bank for this account");
    return this;
  }

  public ImportChecker selectOfxAccountBank(String bank) {
    openEntityEditionChecker().selectBank(bank)
      .validate();
    return this;
  }

  public ImportChecker checkSelectACardTypeMessage() {
    dialog.getTextBox("You must select a card type");
    return this;
  }

  public CardTypeChooserChecker openCardType() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton("Select a card type").triggerClick());
    return new CardTypeChooserChecker(window);
  }
}
