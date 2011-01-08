package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.utils.ComponentIsVisibleAssertion;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.gui.importer.ImportDialog;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;

public class ImportDialogChecker extends GuiChecker {
  private Panel dialog;
  private TextBox fileField;
  private Button importButton;

  public static ImportDialogChecker open(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new ImportDialogChecker(window, true);
  }

  public static ImportDialogChecker openInStep2(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new ImportDialogChecker(window, false);
  }

  public ImportDialogChecker(Panel dialog, final boolean step1) {
    this.dialog = dialog;
    if (step1){
      fileField = dialog.getInputTextBox("fileField");
      importButton = dialog.getButton("Import");
    }
  }

  private ImportDialogChecker() {
  }

  static public ImportDialogChecker create(Panel dialog) {
    ImportDialogChecker importDialog = new ImportDialogChecker();
    importDialog.dialog = dialog;
    return importDialog;
  }

  public ImportDialogChecker setFilePath(String text) {
    fileField.setText(text);
    return this;
  }

  public ImportDialogChecker selectFiles(String... path) {
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

  public ImportDialogChecker acceptFile() {
    importButton.click();
    return this;
  }

  public ImportDialogChecker checkFileContent(Object[][] expected) {
    Table table = dialog.getTable();
    assertTrue(table.contentEquals(expected));
    return this;
  }

  public ImportDialogChecker checkHeaderMessage(String text) {
    TextBox fileMessage = dialog.findUIComponent(TextBox.class, text);
    assertTrue(fileMessage.isVisible());
    return this;
  }

  public BankGuideChecker openBankGuide() {
    return BankGuideChecker.open(dialog.getTextBox("bankMessage").triggerClickOnHyperlink("download guide"));
  }

  public ImportDialogChecker checkDates(String... dates) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertTrue(dateFormatCombo.contentEquals(dates));
    return this;
  }

  public ImportDialogChecker doImport() {
    dialog.getButton(Lang.get("import.step1.ok")).click();
    return this;
  }

  public void completeImport() {
    validate(-1, -1, -1, dialog, "import.step2.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport(double amount) {
    doImportWithBalance().setAmount(amount).validateFromImport();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport(final int importedTransactionCount, final int autocategorizedTransactionCount) {
    validate(-1, importedTransactionCount, autocategorizedTransactionCount, dialog, "import.step2.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportNone(int loadTransaction) {
    validate(loadTransaction, 0, 0, dialog, "import.step2.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void skipAndComplete(){
    validate(-1, -1, -1, dialog, "import.skip.file");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportAndGotoCategorize(int importedTransactionCount, int autocategorizedTransactionCount) {
    WindowInterceptor.init(dialog.getButton(Lang.get("import.step2.ok")).triggerClick())
      .process(new ImportCompleteWindowHandler(0,
                                               importedTransactionCount,
                                               autocategorizedTransactionCount, Lang.get("import.end.button"))).run();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public static void validate(final int loadedTransaction, final int importedTransactionCount, final int autocategorizedTransactionCount,
                              final Panel dialog, final String key) {
    WindowInterceptor.init(dialog.getButton(Lang.get(key)).triggerClick())
      .process(new ImportCompleteWindowHandler(loadedTransaction, importedTransactionCount, autocategorizedTransactionCount)).run();
  }

  public AccountPositionEditionChecker doImportWithBalance() {
    return new AccountPositionEditionChecker(WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("import.step1.ok")).triggerClick()));
  }

  public void close() {
    dialog.getButton("close").click();
  }

  public ImportDialogChecker checkErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("importMessage").textEquals(Lang.get(message, arg)));
    return this;
  }

  public ImportDialogChecker checkHtmlErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("importMessage").htmlEquals(Lang.get(message, arg)));
    return this;
  }

  public MessageAndDetailsDialogChecker clickErrorMessage() {
    return MessageAndDetailsDialogChecker.init(
      dialog.getTextBox("importMessage").triggerClickOnHyperlink("Click here"));
  }

  public ImportDialogChecker selectDate(String dateFormat) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    dateFormatCombo.select(dateFormat);
    return this;
  }

  public void checkCloseButton(String text) {
    assertThat(dialog.getButton("close").textEquals(text));
  }

  public ImportDialogChecker checkSelectedAccount(String accountNumber) {
    assertThat(dialog.getComboBox("accountCombo").selectionEquals(accountNumber));
    return this;
  }

  public ImportDialogChecker checkNoErrorMessage() {
    TextBox message = (TextBox)dialog.findUIComponent(ComponentMatchers.innerNameIdentity("importMessage"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    return this;
  }

  public ImportDialogChecker checkFilePath(String path) {
    assertTrue(fileField.textEquals(path));
    return this;
  }

  public ImportDialogChecker browseAndSelect(String path) {
    WindowInterceptor.init(dialog.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();
    return this;
  }

  public ImportDialogChecker skipFile() {
    dialog.getButton("Skip").click();
    return this;
  }

  public ImportDialogChecker checkMessageCreateFirstAccount() {
    dialog.getTextBox("You must create an account");
    return this;
  }

  public ImportDialogChecker checkAvailableAccounts(String... accountNames) {
    assertTrue(dialog.getComboBox("accountCombo").contentEquals(accountNames));
    return this;
  }

  public ImportDialogChecker selectAccount(final String accountName) {
    dialog.getComboBox("accountCombo").select(accountName);
    return this;
  }

  public AccountEditionChecker openAccount() {
    return AccountEditionChecker.open(dialog.getButton("Create an account").triggerClick());
  }

  public AccountEditionChecker addNewAccount() {
    return AccountEditionChecker.open(dialog.getButton("newAccount").triggerClick());
  }

  public AccountChooserChecker openChooseAccount() {
    return AccountChooserChecker.open(this, dialog.getButton("Associate").triggerClick());
  }

  public ImportDialogChecker defineAccount(String bank, String accountName, String number) {
    AccountEditionChecker accountEditionChecker =
      AccountEditionChecker.open(dialog.getButton("Create an account").triggerClick());
    accountEditionChecker
      .selectBank(bank)
      .checkAccountName("Main account")
      .setAccountName(accountName)
      .setAccountNumber(number);
    accountEditionChecker.validate();
    return this;
  }

  public ImportDialogChecker createNewAccount(String bank, String accountName, String number, double initialBalance) {
    addNewAccount()
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

  public ImportDialogChecker checkDirectory(String directory) {
    WindowInterceptor.init(dialog.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().assertCurrentDirEquals(new File(directory)).cancelSelection())
      .run();
    return this;
  }

  public BankEntityEditionChecker openEntityEditor() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton("Select the bank").triggerClick());
    return new BankEntityEditionChecker(window);
  }

  public ImportDialogChecker checkMessageSelectABank() {
    dialog.getTextBox("You must select a bank for this account");
    return this;
  }

  public ImportDialogChecker selectOfxAccountBank(String bank) {
    openEntityEditor().selectBank(bank)
      .validate();
    return this;
  }

  public ImportDialogChecker checkSelectACardTypeMessage() {
    dialog.getTextBox("You must select a card type");
    return this;
  }

  public CardTypeChooserChecker openCardTypeChooser() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton("Select a card type").triggerClick());
    return new CardTypeChooserChecker(window);
  }

  public ImportDialogChecker checkMessageEmptyFile() {
    dialog.getTextBox("This file contains no operations");
    return this;
  }

  public ImportDialogChecker checkAccountTypeSelectionDisplayedFor(String... accounts) {
    for (String account : accounts) {
      getAccountTypeSelectionCombo(account);
    }
    return this;
  }

  public ImportDialogChecker checkAccountTypeWarningDisplayed(String accountName) {
    BalloonTipTesting.checkBalloonTipVisible(dialog,
                                             getAccountTypeSelectionCombo(accountName),
                                             "Select the account type"
    );
    return this;
  }

  public ImportDialogChecker checkNoAccountTypeMessageDisplayed() {
    BalloonTipTesting.checkNoBalloonTipVisible(dialog);
    return this;
  }

  public ImportDialogChecker checkNoMessageSelectAnAccountType() {
    try {
      dialog.getTextBox("You must choose the account type");
      fail("message is present");
    }
    catch (ItemNotFoundException e) {
      // OK
    }
    return this;
  }

  public boolean hasAccountType() {
    return new ComponentIsVisibleAssertion<JPanel>(dialog, JPanel.class, "accountTypeSelection", true).isTrue();
  }

  public ImportDialogChecker setMainAccountForAll(){
    UIComponent[] uiComponents = getAccountTypeSelectionPanel().getUIComponents(ComboBox.class);
    for (UIComponent component : uiComponents) {
      ((ComboBox)component).select("main");
    }
    return this;
  }

  public ImportDialogChecker setMainAccount(String... accounts) {
    return setAccountType("main", accounts);
  }

  public ImportDialogChecker setSavingsAccount(String... accounts) {
    return setAccountType("savings", accounts);
  }

  private ImportDialogChecker setAccountType(String accountType, String[] accounts) {
    if (accounts.length == 0) {
      getAccountTypeSelectionPanel().getComboBox().select(accountType);
    }
    else {
      for (String account : accounts) {
        getAccountTypeSelectionCombo(account).select(accountType);
      }
    }
    return this;
  }

  private ComboBox getAccountTypeSelectionCombo(String account) {
    return getAccountTypeSelectionPanel().getComboBox("Combo:" + account);
  }

  private Panel getAccountTypeSelectionPanel() {
    Panel selectionPanel = dialog.getPanel("accountTypeSelection");
    UISpecAssert.assertThat(selectionPanel.isVisible());
    return selectionPanel;
  }

  public static class ImportCompleteWindowHandler extends WindowHandler {
    private int loadedTransactionCount;
    private final int importedTransactionCount;
    private final int autocategorizedTransactionCount;
    private String buttonMessage = null;

    public ImportCompleteWindowHandler(int loadedTransactionCount, int importedTransactionCount, int autocategorizedTransactionCount) {
      this.loadedTransactionCount = loadedTransactionCount;
      this.importedTransactionCount = importedTransactionCount;
      this.autocategorizedTransactionCount = autocategorizedTransactionCount;
    }

    public ImportCompleteWindowHandler(int loadedTransactionCount, int importedTransactionCount, int autocategorizedTransactionCount, String buttonMessage) {
      this(loadedTransactionCount, importedTransactionCount, autocategorizedTransactionCount);
      this.buttonMessage = buttonMessage;
    }

    public Trigger process(Window window) throws Exception {
      MessageDialogChecker checker = new MessageDialogChecker(window);
      if (importedTransactionCount != -1) {
        checker
          .checkMessageContains(Lang.get(ImportDialog.getEndOfImportMessageKey(loadedTransactionCount, importedTransactionCount, autocategorizedTransactionCount),
                                         Integer.toString(importedTransactionCount),
                                         Integer.toString(autocategorizedTransactionCount),
                                         Integer.toString(loadedTransactionCount)));
      }
      if (buttonMessage == null) {
        return checker.triggerCloseUndefined();
      }
      else {
        return checker.triggerClose(buttonMessage);
      }
    }
  }
}
