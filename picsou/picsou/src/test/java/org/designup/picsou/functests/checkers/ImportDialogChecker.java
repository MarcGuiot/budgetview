package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.utils.ComponentIsVisibleAssertion;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.gui.importer.ImportCompletionPanel;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
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
    if (step1) {
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

  public BankDownloadChecker getBankDownload() {
    return new BankDownloadChecker(dialog.getPanel("bankDownload"));
  }

  public ImportDialogChecker checkDates(String... dates) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertTrue(dateFormatCombo.contentEquals(dates));
    return this;
  }

  public ImportDialogChecker doImport() {
    dialog.getButton(Lang.get("import.fileSelection.ok")).click();
    return this;
  }

  public boolean isLastStep() {
    return dialog.getTextBox("title").getText().contains("Import done");
  }

  public ImportDialogChecker checkLastStep() {
    assertThat(dialog.getTextBox("title").textEquals("Import done"));
    return this;
  }

  public void completeLastStep() {
    dialog.getButton(Lang.get("ok")).click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport() {
    validateAndComplete(-1, -1, -1, dialog, "import.preview.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport(double amount) {
    doImportWithBalance().setAmount(amount).validate();
    ImportDialogChecker.complete(-1, -1, -1, dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport(final int importedTransactionCount, final int autocategorizedTransactionCount) {
    validateAndComplete(-1, importedTransactionCount, autocategorizedTransactionCount, dialog, "import.preview.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportNone(int loadTransaction) {
    validateAndComplete(loadTransaction, 0, 0, dialog, "import.preview.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void skipAndComplete() {
    validateAndComplete(-1, -1, -1, dialog, "import.skip.file");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportAndGotoCategorize(int importedTransactionCount, int autocategorizedTransactionCount) {
    dialog.getButton(Lang.get("import.preview.ok")).click();

    CompletionChecker handler =
      new CompletionChecker(0, importedTransactionCount,
                            autocategorizedTransactionCount, Lang.get("import.end.button"));
    handler.checkAndClose(dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public static void validateAndComplete(final int loadedTransaction, final int importedTransactionCount, final int autocategorizedTransactionCount,
                                         final Panel dialog, final String key) {
    dialog.getButton(Lang.get(key)).click();
    complete(loadedTransaction, importedTransactionCount, autocategorizedTransactionCount, dialog);
  }

  public static void complete(int loadedTransaction, int importedTransactionCount, int autocategorizedTransactionCount, Panel dialog) {
    CompletionChecker handler = new CompletionChecker(loadedTransaction, importedTransactionCount, autocategorizedTransactionCount);
    handler.checkAndClose(dialog);
  }

  public AccountPositionEditionChecker doImportWithBalance() {
    return new AccountPositionEditionChecker(dialog, "import.fileSelection.ok");
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

  public ImportDialogChecker checkCloseButton(String text) {
    Button close = dialog.getButton("close");
    assertThat(close.textEquals(text));
    return this;
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
    if (bank != null){
      accountEditionChecker
        .selectBank(bank);
    }
    accountEditionChecker
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
    Window window = WindowInterceptor.getModalDialog(dialog.getButton("Select the bank").triggerClick());
    BankChooserChecker chooserChecker = new BankChooserChecker(window);
    chooserChecker.selectBank(bank).validate();
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

  public ImportDialogChecker setMainAccountForAll() {
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
    assertThat(selectionPanel.isVisible());
    return selectionPanel;
  }

  public boolean hasCardType() {
    Button button = dialog.findUIComponent(Button.class, "Select a card type");
    if (button == null) {
      return false;
    }
    return true;
  }

  public void importDeferred(String accountName, String fileName, boolean withMainAccount) {
    setFilePath(fileName)
      .acceptFile();
    if (hasCardType()) {
      selectDeferred(accountName);
    }
    else {
      setMainAccountForAll();
    }
    if (withMainAccount) {
      doImport();
      if (hasCardType()) {
        selectDeferred(accountName);
      }
      else {
        setMainAccountForAll();
      }
    }
    completeImport();
  }

  private void selectDeferred(String accountName) {
    openCardTypeChooser()
      .selectDeferredCard(accountName)
      .validate();
    if (hasAccountType()) {
      setMainAccount();
    }
  }


  public static class CompletionChecker {
    private int loadedTransactionCount;
    private final int importedTransactionCount;
    private final int autocategorizedTransactionCount;
    private String buttonMessage = null;

    public CompletionChecker(int loadedTransactionCount, int importedTransactionCount, int autocategorizedTransactionCount) {
      this.loadedTransactionCount = loadedTransactionCount;
      this.importedTransactionCount = importedTransactionCount;
      this.autocategorizedTransactionCount = autocategorizedTransactionCount;
    }

    public CompletionChecker(int loadedTransactionCount, int importedTransactionCount, int autocategorizedTransactionCount, String buttonMessage) {
      this(loadedTransactionCount, importedTransactionCount, autocategorizedTransactionCount);
      this.buttonMessage = buttonMessage;
    }

    public Trigger checkAndGetTrigger(Panel dialog) {
      TextBox contentBox = dialog.getTextBox("message");
      if (importedTransactionCount != -1) {
        String expectedMessage =
          Lang.get(ImportCompletionPanel.getEndOfImportMessageKey(loadedTransactionCount, importedTransactionCount, autocategorizedTransactionCount),
                   Integer.toString(importedTransactionCount),
                   Integer.toString(autocategorizedTransactionCount),
                   Integer.toString(loadedTransactionCount));
        assertThat(contentBox.textContains(expectedMessage));
      }
      if (buttonMessage == null) {
        return dialog.getButton("OK").triggerClick();
      }
      else {
        return dialog.getButton(buttonMessage).triggerClick();
      }
    }

    public void checkAndClose(Panel dialog) {
      try {
        checkAndGetTrigger(dialog).run();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Panel getDialog() {
    return dialog;
  }
}
