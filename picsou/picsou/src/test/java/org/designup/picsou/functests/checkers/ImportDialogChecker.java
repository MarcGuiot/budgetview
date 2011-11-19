package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.utils.ComponentIsVisibleAssertion;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.gui.importer.ImportCompletionPanel;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ImportDialogChecker extends GuiChecker {
  private Panel dialog;
  private TextBox fileField;
  private Button importButton;
  private AccountEditionChecker accountEditionChecker;

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
    accountEditionChecker = new AccountEditionChecker(dialog);
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
    dialog.getButton(Lang.get("import.preview.ok")).click();
    return this;
  }

  public ImportDialogChecker importThisAccount() {
    dialog.getButton(Lang.get("load")).click();
    return this;
  }

  public boolean isLastStep() {
    return dialog.getTextBox("title").getText().contains(Lang.get("import.end.info.title"));
  }

  public ImportDialogChecker checkLastStep() {
    assertThat(dialog.getTextBox("title").textEquals(Lang.get("import.end.info.title")));
    return this;
  }

  public void completeLastStep() {
    dialog.getButton(Lang.get("ok")).click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public OtherBankSynchroChecker openSynchro(String bankName) {
    return getBankDownload().selectBank(bankName).openSynchro(this);
  }

  public void completeImport() {
    TextBox box = dialog.getTextBox("importMessage");
    assertTrue(box.textIsEmpty());
    validateAndComplete(-1, -1, -1, dialog, "import.preview.ok");
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportWithNext() {
    TextBox box = dialog.getTextBox("importMessage");
    assertTrue(box.textIsEmpty());
    validateAndComplete(-1, -1, -1, dialog, "import.preview.noOperation.ok");
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

  public ImportSeriesChecker importSeries() {
    return new ImportSeriesChecker(WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("import.preview.ok"))
                                                                      .triggerClick()), dialog);
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

  public void complete() {
    dialog.getButton(Lang.get("import.end.button")).click();
    assertFalse(dialog.isVisible());
  }

  public void close() {
    dialog.getButton(Lang.get("import.fileSelection.close")).click();
    assertFalse(dialog.isVisible());
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
    Button close = dialog.getButton(Lang.get("close"));
    assertThat(close.textEquals(text));
    return this;
  }

  public ImportDialogChecker checkSelectedAccount(String accountNumber) {
    assertThat(getAccountCombo().selectionEquals(accountNumber));
    return this;
  }

  public ImportDialogChecker checkNoErrorMessage() {
    TextBox message = (TextBox)dialog.findUIComponent(ComponentMatchers.innerNameIdentity("importMessage"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    accountEditionChecker.checkNoErrorDisplayed();
    return this;
  }

  public ImportDialogChecker checkFilePath(String path) {
    assertTrue(fileField.textEquals(path));
    return this;
  }

  public ImportDialogChecker browseAndSelect(String path) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")).triggerClick())
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();
    return this;
  }

  public ImportDialogChecker skipFile() {
    dialog.getButton(Lang.get("import.skip.file")).click();
    return this;
  }

  public ImportDialogChecker checkMessageCreateFirstAccount() {
//    accountEditionChecker.checkErrorTipVisible();
//    accountEditionChecker.
    // verifier le sign post
//    dialog.getTextBox("You must create an account");
    return this;
  }

  public ImportDialogChecker checkAvailableAccounts(String... accountNames) {
    String[] tmp = new String[accountNames.length + 1];
    System.arraycopy(accountNames, 0, tmp, 1, accountNames.length);
    tmp[0] = Lang.get("import.account.combo.empty");
    assertTrue(getAccountCombo().contentEquals(tmp));
    return this;
  }

  public ImportDialogChecker selectAccount(final String accountName) {
    getAccountCombo().select(accountName);
    return this;
  }

  private ComboBox getAccountCombo() {
    return dialog.getComboBox("accountCombo");
  }

  public ImportDialogChecker addNewAccount() {
    getAccountCombo().select(Lang.get("import.account.combo.empty"));
    return this;
  }

  public ImportDialogChecker defineAccount(String bank, String accountName, String number) {
    accountEditionChecker.setAccountName(accountName)
      .setAccountNumber(number)
      .setAsMain();
    if (bank != null) {
      accountEditionChecker.selectBank(bank);
    }
    return this;
  }

  public ImportDialogChecker createNewAccount(String bank, String accountName, String number, double initialBalance) {
    addNewAccount();
    accountEditionChecker
      .selectBank(bank)
      .setAsMain()
      .setAccountName(accountName)
      .setAccountNumber(number)
      .setPosition(initialBalance);
    return this;
  }

  public ImportDialogChecker checkDirectory(String directory) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")).triggerClick())
      .process(FileChooserHandler.init().assertCurrentDirEquals(new File(directory)).cancelSelection())
      .run();
    return this;
  }

  public ImportDialogChecker selectBank(String bank) {
    accountEditionChecker.selectBank(bank);
    return this;
  }

  public CardTypeChooserChecker openCardTypeChooser() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("account.error.missing.cardType.button")).triggerClick());
    return new CardTypeChooserChecker(window);
  }

  public ImportDialogChecker checkMessageEmptyFile() {
    dialog.getTextBox(Lang.get("import.file.empty"));
    return this;
  }

  public ImportDialogChecker checkAccountMessage(String text) {
    assertThat(dialog.getTextBox("accountCountInfo").textContains(text));
    return this;
  }

  public ImportDialogChecker checkAccountSelectionMessage(String text) {
    assertThat(dialog.getTextBox("accountSelectionLabel").textEquals(text));
    return this;
  }

  public ImportDialogChecker checkAccountTypeWarningDisplayed(String accountName) {
    BalloonTipTesting.checkBalloonTipVisible(dialog,
                                             accountEditionChecker.getTypeCombo(),
                                             Lang.get("account.error.missing.account.type"));
    return this;
  }

  public ImportDialogChecker checkNoAccountTypeMessageDisplayed() {
    BalloonTipTesting.checkNoBalloonTipVisible(dialog);
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

  public ImportDialogChecker setMainAccount() {
    accountEditionChecker.setAsMain();
    return this;
  }

  public ImportDialogChecker setSavingsAccount() {
    accountEditionChecker.setAsSavings();
    return this;
  }

  private Panel getAccountTypeSelectionPanel() {
    Panel selectionPanel = dialog.getPanel("accountTypeSelection");
    assertThat(selectionPanel.isVisible());
    return selectionPanel;
  }

  public void importDeferred(String accountName, String fileName, boolean withMainAccount) {
    setFilePath(fileName)
      .acceptFile();
    if (accountEditionChecker.getAccountName().equals(accountName)) {
      setDeferredAccount();
    }
    else {
      setMainAccount();
    }
    if (withMainAccount) {
      doImport();
      if (accountEditionChecker.getAccountName().equals(accountName)) {
        setDeferredAccount();
      }
      else {
        setMainAccount();
      }
    }
    completeImport();
  }

  public ImportDialogChecker setDeferredAccount() {
    accountEditionChecker.setAsDeferredCard();
    return this;
  }

  public ImportDialogChecker checkErrorAccount() {
    accountEditionChecker.checkNameMissing();
    return this;
  }

  public ImportDialogChecker setAccountNumber(String number) {
    accountEditionChecker.setAccountNumber(number);
    return this;
  }

  public ImportDialogChecker setAccountName(String name) {
    accountEditionChecker.setAccountName(name);
    return this;
  }

  public ImportDialogChecker setPosition(double amount) {
    accountEditionChecker.setPosition(amount);
    return this;
  }

  public boolean isNew() {
    UIComponent component = dialog.findUIComponent(ComponentMatchers.innerNameIdentity("accountCombo"));
    return ((JComboBox)component.getAwtComponent()).getSelectedItem() == null;
  }

  public void waitAcceptFiles() {
    UISpecAssert.waitUntil(dialog.containsLabel(Lang.get("import.preview.title")), 10000);
  }

  public ImportDialogChecker setAsCreditCard() {
    accountEditionChecker.setAsCreditCard();
    return this;
  }

  public ImportDialogChecker checkAccount(String accountName) {
    accountEditionChecker.checkAccountName(accountName);
    return this;
  }

  public boolean accountIsEditable() {
    return accountEditionChecker.accountIsEditable();
  }

  public ImportDialogChecker checkAccountNotEditable() {
    accountEditionChecker.checkAccountDisabled();
    return this;
  }

  public ImportDialogChecker checkAccountDescription(String text) {
    TextBox description = dialog.getTextBox("readOnlyDescription");
    assertTrue(description.isVisible());
    assertTrue(description.textEquals(text));
    return this;
  }

  public ImportDialogChecker checkAccountEditable() {
    accountEditionChecker.checkAccountEditable();
    return this;
  }

  public OfxSynchoChecker openOfxSynchro(String bankName) {
    return getBankDownload().selectBank(bankName)
      .openOfxSynchro(this);
  }

  public ImportDialogChecker checkAstericsErrorOnName() {
    accountEditionChecker.checkAstericsErrorOnName();
    return this;
  }

  public ImportDialogChecker checkAccountPosition(double position) {
    accountEditionChecker.checkPosition(position);
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnName() {
    accountEditionChecker.checkAstericsClearOnName();
    return this;
  }

  public ImportDialogChecker checkAstericsErrorOnBank() {
    accountEditionChecker.checkAstericsErrorOnBank();
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnBank() {
    accountEditionChecker.checkAstericsClearOnBank();
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnType() {
    accountEditionChecker.checkAstericsClearOnType();
    return this;
  }

  public ImportDialogChecker checkAstericsErrorOnType() {
    accountEditionChecker.checkAstericsErrorOnType();
    return this;
  }

  public ImportDialogChecker doNext() {
    dialog.getButton(Lang.get("import.preview.noOperation.ok")).click();
    return this;
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
        return dialog.getButton(Lang.get("import.end.button")).triggerClick();
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
}
