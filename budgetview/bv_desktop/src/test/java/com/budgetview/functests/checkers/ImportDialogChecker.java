package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.utils.ComponentIsVisibleAssertion;
import com.budgetview.functests.utils.BalloonTipTesting;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.globsframework.utils.TestUtils;
import org.uispec4j.*;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ImportDialogChecker extends GuiChecker {
  private Window dialog;
  private TextBox fileField;
  private Button importButton;
  private AccountEditionChecker accountEditionChecker;
  private BankDownloadChecker bankDownload;

  public static ImportDialogChecker open(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new ImportDialogChecker(window);
  }

  public ImportDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  Window getDialog() {
    return dialog;
  }

  private ImportDialogChecker() {
  }

  public static ImportDialogChecker create(Window dialog) {
    ImportDialogChecker importDialog = new ImportDialogChecker();
    importDialog.dialog = dialog;
    return importDialog;
  }

  public ImportDialogChecker setFilePath(String text) {
    getFileField().setText(text, false);
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
    getFileField().setText(builder.toString());
    return this;
  }

  public ImportDialogChecker checkFileImportEnabled() {
    assertTrue(getImportButton().isEnabled());
    return this;
  }

  public ImportDialogChecker checkFileImportDisabled() {
    assertFalse(getImportButton().isEnabled());
    return this;
  }

  public ImportDialogChecker acceptFile() {
    String titleKey = "import.fileSelection.title";
    checkTitle(titleKey);
    getImportButton().click();
    return this;
  }

  public void checkTitle(String titleKey) {
    assertThat(dialog.getTextBox(ComponentMatchers.innerNameIdentity("title")).textEquals(Lang.get(titleKey)));
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
    if (bankDownload == null) {
      bankDownload = new BankDownloadChecker(dialog);
    }
    return bankDownload;
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
    return dialog.getTextBox("title").getText().contains(Lang.get("import.completion.title"));
  }

  public ImportDialogChecker checkLastStep() {
    checkTitle("import.completion.title");
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
    assertTrue(dialog.getTextBox("importMessage").textIsEmpty());
    validateAndComplete(-1, -1, -1, dialog, "import.preview.ok");
  }

  public void completeImportAndImportSeries() {
    Trigger trigger = dialog.getButton("ok").triggerClick();
    ImportSeriesChecker.init(trigger, dialog)
      .validateAndFinishImport();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportAndSkipSeries() {
    assertTrue(dialog.getTextBox("importMessage").textIsEmpty());
    Trigger trigger = dialog.getButton(Lang.get("import.preview.ok")).triggerClick();
    ImportSeriesChecker.init(trigger, dialog)
      .cancelImportSeries();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportWithNext() {
    assertTrue(dialog.getTextBox("importMessage").textIsEmpty());
    validateAndComplete(-1, -1, -1, dialog, "import.preview.noOperation.ok");
  }

  public void completeImport(double amount) {
    doImportWithPosition().setAmount(amount).validate();
    ImportDialogChecker.complete(-1, -1, -1, dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportStartFromZero(double amount) {
    AccountPositionEditionChecker positionEditionChecker = doImportWithPosition();
    positionEditionChecker.checkInitialAmountSelected(AmountFormat.DECIMAL_FORMAT.format(amount));
    positionEditionChecker.validate();
    ImportDialogChecker.complete(-1, -1, -1, dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImport(final int importedTransactionCount, int ignoredTransactionCount, final int autocategorizedTransactionCount) {
    validateAndComplete(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount, dialog, "import.preview.ok");
  }

  public void skipAndComplete() {
    validateAndComplete(-1, -1, -1, dialog, "import.skip.file");
  }

  public void completeImportAndGotoCategorize(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    dialog.getButton(Lang.get("import.preview.ok")).click();

    CompletionChecker handler =
      new CompletionChecker(importedTransactionCount, ignoredTransactionCount,
                            autocategorizedTransactionCount, Lang.get("import.completion.button"));
    handler.checkAndClose(dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public ImportSeriesChecker importSeries() {
    return new ImportSeriesChecker(WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("import.preview.ok"))
                                                                      .triggerClick()), dialog);
  }

  public static void validateAndComplete(final int importedTransactionCount,
                                         final int ignoredTransactionCount,
                                         final int autocategorizedTransactionCount,
                                         final Panel dialogToClose, final String key) {

    dialogToClose.getButton(Lang.get(key)).click();
    complete(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount, dialogToClose);
    UISpecAssert.assertFalse(dialogToClose.isVisible());
  }

  public static void complete(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount, Panel dialogToClose) {
    CompletionChecker handler = new CompletionChecker(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
    handler.checkAndClose(dialogToClose);
  }

  public AccountPositionEditionChecker doImportWithPosition() {
    return new AccountPositionEditionChecker(dialog, "import.fileSelection.ok");
  }

  public void complete() {
    dialog.getButton(Lang.get("import.completion.button")).click();
    checkClosed();
  }

  public void close() {
    dialog.getButton(Lang.get("import.fileSelection.close")).click();
    checkClosed();
  }

  public void checkClosed() {
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

  public ImportDialogChecker selectDateFormat(String dateFormat) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertThat(dateFormatCombo.isVisible());
    dateFormatCombo.select(dateFormat);
    return this;
  }

  public ImportDialogChecker checkDateFormatHidden() {
    assertFalse(dialog.getComboBox("dateFormatCombo").isVisible());
    return this;
  }

  public ImportDialogChecker checkDateFormatSelected(String selection) {
    ComboBox combo = dialog.getComboBox("dateFormatCombo");
    assertThat(combo.isVisible());
    assertThat(combo.selectionEquals(selection));
    return this;
  }

  public ImportDialogChecker checkDateFormatMessageShown(String messageKey) {
    checkSignpostVisible(dialog, dialog.getComboBox("dateFormatCombo"), Lang.get(messageKey));
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
    TextBox message = (TextBox) dialog.findUIComponent(ComponentMatchers.innerNameIdentity("importMessage"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    getAccountEditionChecker().checkNoErrorDisplayed();
    return this;
  }

  public ImportDialogChecker checkFilePath(String path) {
    assertTrue(getFileField().textEquals(path));
    return this;
  }

  public ImportDialogChecker browseAndSelect(String path) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")))
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
    checkPreviewPanelShown();
    getAccountCombo().select(accountName);
    return this;
  }

  public ImportDialogChecker selectNewAccount() {
    checkPreviewPanelShown();
    getAccountCombo().select("a new account");
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
    getAccountEditionChecker().setName(accountName)
      .setAccountNumber(number)
      .setAsMain();
    if (bank != null) {
      getAccountEditionChecker().selectBank(bank);
    }
    return this;
  }

  public ImportDialogChecker createNewAccount(String bank, String accountName, String number) {
    return createNewAccount(bank, accountName, number, null);
  }

  public ImportDialogChecker createNewAccount(String bank, String accountName, String number, Double initialBalance) {
    addNewAccount();
    AccountEditionChecker editionChecker = getAccountEditionChecker()
      .selectBank(bank)
      .setAsMain()
      .setName(accountName)
      .setAccountNumber(number);
    if (initialBalance != null) {
      editionChecker
        .setPosition(initialBalance);
    }
    return this;
  }

  public ImportDialogChecker checkDirectory(String directory) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")))
      .process(FileChooserHandler.init().assertCurrentDirEquals(new File(directory)).cancelSelection())
      .run();
    return this;
  }

  public ImportDialogChecker selectBank(String bank) {
    getAccountEditionChecker().selectBank(bank);
    return this;
  }

  public ImportDialogChecker addNewAccountBank(String bankName, String url) {
    getAccountEditionChecker().selectNewBank(bankName, url);
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
                                             getAccountEditionChecker().getTypeCombo(),
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
      ((ComboBox) component).select("main");
    }
    return this;
  }

  public ImportDialogChecker setMainAccount() {
    checkPreviewPanelShown();
    getAccountEditionChecker()
      .checkAccountTypeEditable()
      .setAsMain();
    return this;
  }

  public ImportDialogChecker setSavingsAccount() {
    checkPreviewPanelShown();
    getAccountEditionChecker().setAsSavings();
    return this;
  }

  public void checkPreviewPanelShown() {
    checkTitle("import.preview.title");
  }

  private Panel getAccountTypeSelectionPanel() {
    Panel selectionPanel = dialog.getPanel("accountTypeSelection");
    assertThat(selectionPanel.isVisible());
    return selectionPanel;
  }

  public void importDeferred(String accountName, String fileName, boolean withMainAccount, String targetAccountName) {
    setFilePath(fileName)
      .acceptFile();
    if (getAccountEditionChecker().getAccountName().equals(accountName)) {
      if (accountName.length() > 20) {
        getAccountEditionChecker().setName(accountName.substring(0, 20));
      }
      setDeferredAccount(25, 28, 0, targetAccountName);
    }
    else {
      setMainAccount();
    }
    if (withMainAccount) {
      doImport();
      if (getAccountEditionChecker().getAccountName().equals(accountName)) {
        if (accountName.length() > 20) {
          getAccountEditionChecker().setName(accountName.substring(0, 20));
        }
        setDeferredAccount(25, 28, 0, targetAccountName);
      }
      else {
        setMainAccount();
      }
    }
    completeImport();
  }

  public ImportDialogChecker setDeferredAccount(int dayPeriod, int dayPrelevement, int monthShift) {
    getAccountEditionChecker().setAsDeferredCard();
    getAccountEditionChecker().checkDeferredWarning();
    getAccountEditionChecker().setDeferred(dayPeriod, dayPrelevement, monthShift);
    return this;
  }

  public ImportDialogChecker setDeferredAccount(int dayPeriod, int dayPrelevement, int monthShift, String targetAccount) {
    getAccountEditionChecker().setAsDeferredCard()
      .checkDeferredWarning()
      .setDeferred(dayPeriod, dayPrelevement, monthShift)
      .setTargetAccount(targetAccount);
    return this;
  }

  public ImportDialogChecker checkErrorAccount() {
    getAccountEditionChecker().checkNameMissing();
    return this;
  }

  public ImportDialogChecker setAccountNumber(String number) {
    getAccountEditionChecker().setAccountNumber(number);
    return this;
  }

  public ImportDialogChecker setAccountName(String name) {
    getAccountEditionChecker().setName(name);
    return this;
  }

  public ImportDialogChecker setPosition(double amount) {
    getAccountEditionChecker().setPosition(amount);
    return this;
  }

  public boolean isNew() {
    UIComponent component = dialog.findUIComponent(ComponentMatchers.innerNameIdentity("accountCombo"));
    return ((JComboBox) component.getAwtComponent()).getSelectedItem() == null;
  }

  public void waitForPreview() {
    UISpecAssert.waitUntil(new Assertion() {
      private String expectedTitle = Lang.get("import.preview.title");

      public void check() {
        if (!dialog.containsLabel(expectedTitle).isTrue()) {
          UISpecAssert.fail("Current title is '" + dialog.getTextBox("title").getText()
                            + "' instead of '" + expectedTitle + "'");
        }
      }
    }, 10000);
  }

  public ImportDialogChecker setAsCreditCard() {
    getAccountEditionChecker().setAsCreditCard();
    return this;
  }

  public ImportDialogChecker checkAccount(String accountName) {
    getAccountEditionChecker().checkAccountName(accountName);
    return this;
  }

  public boolean accountIsEditable() {
    return getAccountEditionChecker().accountIsEditable();
  }

  public ImportDialogChecker checkAccountNotEditable() {
    getAccountEditionChecker().checkAccountDisabled();
    return this;
  }

  public ImportDialogChecker checkExistingAccountDescription(String text) {
    TextBox description = dialog.getTextBox("readOnlyDescription");
    assertTrue(description.isVisible());
    assertTrue(description.textEquals(text));
    return this;
  }

  public ImportDialogChecker checkAccountEditable() {
    getAccountEditionChecker().checkAccountEditable();
    return this;
  }

  public ImportDialogChecker checkSecurityInfo(String content) {
    getBankDownload().checkSecurityMessage(content);
    return this;
  }

  public ImportDialogChecker selectBankForDownload(String bankName) {
    getBankDownload().selectBank(bankName);
    return this;
  }

  public OfxSynchoChecker openOfxSynchro() {
    return getBankDownload().openOfxSynchro(this);
  }

  public ImportDialogChecker checkAstericsErrorOnName() {
    getAccountEditionChecker().checkAstericsErrorOnName();
    return this;
  }

  public ImportDialogChecker checkAccountPosition(double position) {
    getAccountEditionChecker().checkPosition(position);
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnName() {
    getAccountEditionChecker().checkAstericsClearOnName();
    return this;
  }

  public ImportDialogChecker checkAstericsErrorOnBank() {
    getAccountEditionChecker().checkAstericsErrorOnBank();
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnBank() {
    getAccountEditionChecker().checkAstericsClearOnBank();
    return this;
  }

  public ImportDialogChecker checkAstericsClearOnType() {
    getAccountEditionChecker().checkAstericsClearOnType();
    return this;
  }

  public ImportDialogChecker checkAstericsErrorOnType() {
    getAccountEditionChecker().checkAstericsErrorOnType();
    return this;
  }

  public ImportDialogChecker doNext() {
    dialog.getButton(Lang.get("import.preview.noOperation.ok")).click();
    return this;
  }

  public CsvImporterChecker acceptCsvFile() {
    return new CsvImporterChecker(this, WindowInterceptor.getModalDialog(getImportButton().triggerClick()));
  }

  public BankEditionDialogChecker addNewBank() {
    return getBankDownload().addNewBank();
  }

  private TextBox getFileField() {
    if (fileField == null) {
      fileField = dialog.getInputTextBox("fileField");
    }
    return fileField;
  }

  private Button getImportButton() {
    if (importButton == null) {
      importButton = dialog.getButton("Import");
    }
    return importButton;
  }

  private AccountEditionChecker getAccountEditionChecker() {
    if (accountEditionChecker == null) {
      accountEditionChecker = new AccountEditionChecker(dialog);
    }
    return accountEditionChecker;
  }

  public OtherBankSynchroChecker startSynchro() {
    Button startSynchroButton = dialog.getButton("startSynchro");
    assertThat(startSynchroButton.isVisible());
    startSynchroButton.click();
    return new OtherBankSynchroChecker(this, dialog);
  }

  public ImportDialogChecker checkSynchroAvailableForAccounts(String... expectedAccountNames) {
    return checkAccountLabels(dialog.getPanel("synchroAccountsPanel"), expectedAccountNames);
  }

  public ImportDialogChecker checkManualDownloadAvailableForAccounts(String... expectedAccountNames) {
    return checkAccountLabels(dialog.getPanel("manualAccountsPanel"), expectedAccountNames);
  }

  public ImportDialogChecker checkManualDownloadLink(String bankLabel, String url) {
    TextBox label = dialog.getPanel("manualAccountsPanel").getTextBox(bankLabel);
    Button button = new Button((JButton) getSibling(label, 2, "gotoWebsite"));
    BrowsingChecker.checkDisplay(button, url);
    return this;
  }

  private ImportDialogChecker checkAccountLabels(Panel panel, String[] expectedAccountNames) {
    Set<String> actualNames = new HashSet<String>();
    assertThat(panel.isVisible());
    for (UIComponent component : panel.getUIComponents(TextBox.class, "accountLabel")) {
      TextBox textBox = (TextBox) component;
      actualNames.add(textBox.getText());
    }
    TestUtils.assertSetEquals(actualNames, expectedAccountNames);
    return this;
  }

  public ImportDialogChecker checkSynchroNotShown() {
    checkComponentVisible(dialog, JPanel.class, "synchroAccountGroups", false);
    return this;
  }

  public CloudBankSelectionChecker selectCloud() {
    dialog.getPanel("cloudIntro").getButton("openCloudSynchro").click();
    return new CloudBankSelectionChecker(dialog);
  }

  public static class CompletionChecker {
    private final int importedTransactionCount;
    private final int ignoredTransactionCount;
    private final int autocategorizedTransactionCount;
    private final String buttonMessage;

    public CompletionChecker(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
      this(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount, null);
    }

    public CompletionChecker(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount, String buttonMessage) {
      this.importedTransactionCount = importedTransactionCount;
      this.ignoredTransactionCount = ignoredTransactionCount;
      this.autocategorizedTransactionCount = autocategorizedTransactionCount;
      this.buttonMessage = buttonMessage;
    }

    private String toSummaryString(String imported, String ignored, String autocategorized) {
      return "imported:" + imported + "  - ignored:" + ignored + "  - autocategorized:" + autocategorized;
    }

    public void checkAndClose(Panel dialogToClose) {
      assertThat(dialogToClose.getTextBox("title").textEquals(Lang.get("import.completion.title")));
      if (importedTransactionCount != -1) {
        Assert.assertEquals(toSummaryString(Integer.toString(importedTransactionCount),
                                            Integer.toString(ignoredTransactionCount),
                                            Integer.toString(autocategorizedTransactionCount)),
                            toSummaryString(dialogToClose.getTextBox("importedCount").getText(),
                                            dialogToClose.getTextBox("ignoredCount").getText(),
                                            dialogToClose.getTextBox("categorizedCount").getText()));
      }
      if (buttonMessage == null) {
        dialogToClose.getButton(Lang.get("import.completion.button")).click();
      }
      else {
        dialogToClose.getButton(buttonMessage).click();
      }
    }
  }
}
