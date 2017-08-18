package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.utils.ComponentIsVisibleAssertion;
import com.budgetview.functests.utils.BalloonTipTesting;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.utils.Lang;
import org.junit.Assert;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ImportDialogPreviewChecker extends DialogChecker {
  private AccountEditionChecker accountEditionChecker;

  public ImportDialogPreviewChecker(Window dialog) {
    super(dialog);
    checkPanelShown("importPreviewPanel");
  }

  public ImportDialogPreviewChecker checkNewAccountSelected() {
    assertThat(getTargetAccountCombo().selectionEquals(Lang.get("import.preview.targetCombo.newAccount")));
    return this;
  }

  public ImportDialogPreviewChecker checkSelectedAccount(String accountNumber) {
    assertThat(getTargetAccountCombo().selectionEquals(Lang.get("import.preview.targetCombo.existingAccount", accountNumber)));
    return this;
  }

  public ImportDialogPreviewChecker checkAvailableAccounts(String... accountNames) {
    List<String> expected = new ArrayList<String>();
    expected.add(Lang.get("import.preview.targetCombo.newAccount"));
    for (String accountName : accountNames) {
      expected.add(Lang.get("import.preview.targetCombo.existingAccount", accountName));
    }
    expected.add(Lang.get("import.preview.targetCombo.skip"));
    assertTrue(getTargetAccountCombo().contentEquals(expected.toArray(new String[expected.size()])));
    return this;
  }

  public ImportDialogPreviewChecker checkSkipFileSelected() {
    assertThat(getTargetAccountCombo().selectionEquals(Lang.get("import.preview.targetCombo.skip")));
    return this;
  }

  public ImportDialogPreviewChecker selectAccount(final String accountName) {
    getTargetAccountCombo().select(Lang.get("import.preview.targetCombo.existingAccount", accountName));
    return this;
  }

  public ImportDialogPreviewChecker selectNewAccount() {
    getTargetAccountCombo().select(Lang.get("import.preview.targetCombo.newAccount"));
    return this;
  }

  public ImportDialogPreviewChecker selectSkipFile() {
    getTargetAccountCombo().select(Lang.get("import.preview.targetCombo.skip"));
    return this;
  }

  private ComboBox getTargetAccountCombo() {
    return dialog.getComboBox("targetAccountCombo");
  }

  public ImportDialogPreviewChecker checkSkippedFileMessage() {
    getAccountEditionChecker().checkReadOnlyMessage(Lang.get("import.preview.skippedFileMessage"));
    return this;
  }

  public ImportDialogPreviewChecker checkNoErrorMessage() {
    TextBox message = (TextBox) dialog.findUIComponent(ComponentMatchers.innerNameIdentity("importMessage"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    getAccountEditionChecker().checkNoErrorDisplayed();
    return this;
  }

  public ImportDialogPreviewChecker defineAccount(String bank, String accountName, String number) {
    getAccountEditionChecker().setName(accountName)
      .setAccountNumber(number)
      .setAsMain();
    if (bank != null) {
      getAccountEditionChecker().selectBank(bank);
    }
    return this;
  }

  public ImportDialogPreviewChecker setNewAccount(String bank, String accountName, String number) {
    return setNewAccount(bank, accountName, number, null);
  }

  public ImportDialogPreviewChecker setNewAccount(String bank, String accountName, String number, Double initialBalance) {
    selectNewAccount();
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

  public ImportDialogPreviewChecker checkTransactions(Object[][] expected) {
    Table table = dialog.getTable();
    assertTrue(table.contentEquals(expected));
    return this;
  }

  public ImportDialogPreviewChecker checkNoTransactions() {
    checkComponentVisible(dialog, JEditorPane.class, "noOperationLabel", true);
    return this;
  }

  public ImportDialogPreviewChecker setDeferredAccount(int dayPeriod, int dayPrelevement, int monthShift) {
    getAccountEditionChecker().setAsDeferredCard();
    getAccountEditionChecker().checkDeferredWarning();
    getAccountEditionChecker().setDeferred(dayPeriod, dayPrelevement, monthShift);
    return this;
  }

  public ImportDialogPreviewChecker setDeferredAccount(int dayPeriod, int dayPrelevement, int monthShift, String targetAccount) {
    getAccountEditionChecker().setAsDeferredCard()
      .checkDeferredWarning()
      .setDeferred(dayPeriod, dayPrelevement, monthShift)
      .setTargetAccount(targetAccount);
    return this;
  }

  public ImportDialogPreviewChecker checkAccountError() {
    getAccountEditionChecker().checkNameMissing();
    return this;
  }

  public ImportDialogPreviewChecker setAccountNumber(String number) {
    getAccountEditionChecker().setAccountNumber(number);
    return this;
  }

  public ImportDialogPreviewChecker setAccountName(String name) {
    getAccountEditionChecker().setName(name);
    return this;
  }

  public ImportDialogPreviewChecker setPosition(double amount) {
    getAccountEditionChecker().setPosition(amount);
    return this;
  }

  public ImportDialogPreviewChecker selectBank(String bank) {
    getAccountEditionChecker().selectBank(bank);
    return this;
  }

  public ImportDialogPreviewChecker addNewAccountBank(String bankName, String url) {
    getAccountEditionChecker().selectNewBank(bankName, url);
    return this;
  }

  public ImportDialogPreviewChecker checkFileNameShown(String path) {
    checkComponentVisible(dialog, JLabel.class, "fileIntroLabel", true);
    checkComponentVisible(dialog, JLabel.class, "fileName", true);
    assertThat(dialog.getTextBox("fileName").textEquals(path));
    return this;
  }

  public ImportDialogPreviewChecker checkFileNameHidden() {
    checkComponentVisible(dialog, JLabel.class, "fileIntroLabel", false);
    checkComponentVisible(dialog, JLabel.class, "fileName", false);
    return this;
  }

  public ImportDialogPreviewChecker checkAccountMessage(String text) {
    assertThat(dialog.getTextBox("accountCountInfo").textContains(text));
    return this;
  }

  public ImportDialogPreviewChecker checkAccountTypeWarningDisplayed(String accountName) {
    BalloonTipTesting.checkBalloonTipVisible(dialog,
                                             getAccountEditionChecker().getTypeCombo(),
                                             Lang.get("account.error.missing.account.type"));
    return this;
  }

  public ImportDialogPreviewChecker checkNoAccountTypeMessageDisplayed() {
    BalloonTipTesting.checkNoBalloonTipVisible(dialog);
    return this;
  }

  public boolean hasAccountType() {
    return new ComponentIsVisibleAssertion<JPanel>(dialog, JPanel.class, "accountTypeSelection", true).isTrue();
  }

  public ImportDialogPreviewChecker setMainAccountForAll() {
    UIComponent[] uiComponents = getAccountTypeSelectionPanel().getUIComponents(ComboBox.class);
    for (UIComponent component : uiComponents) {
      ((ComboBox) component).select("main");
    }
    return this;
  }

  public ImportDialogPreviewChecker setMainAccount() {
    getAccountEditionChecker()
      .checkAccountTypeEditable()
      .setAsMain();
    return this;
  }

  public ImportDialogPreviewChecker setSavingsAccount() {
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

  public ImportDialogPreviewChecker checkAccountPosition(double position) {
    getAccountEditionChecker().checkPosition(position);
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsErrorOnName() {
    getAccountEditionChecker().checkAstericsErrorOnName();
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsClearOnName() {
    getAccountEditionChecker().checkAstericsClearOnName();
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsErrorOnBank() {
    getAccountEditionChecker().checkAstericsErrorOnBank();
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsClearOnBank() {
    getAccountEditionChecker().checkAstericsClearOnBank();
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsClearOnType() {
    getAccountEditionChecker().checkAstericsClearOnType();
    return this;
  }

  public ImportDialogPreviewChecker checkAstericsErrorOnType() {
    getAccountEditionChecker().checkAstericsErrorOnType();
    return this;
  }

  private AccountEditionChecker getAccountEditionChecker() {
    if (accountEditionChecker == null) {
      accountEditionChecker = new AccountEditionChecker(dialog);
    }
    return accountEditionChecker;
  }

  public ImportDialogPreviewChecker setAsCreditCard() {
    getAccountEditionChecker().setAsCreditCard();
    return this;
  }

  public ImportDialogPreviewChecker checkAccount(String accountName) {
    getAccountEditionChecker().checkAccountName(accountName);
    return this;
  }

  public boolean accountIsEditable() {
    return getAccountEditionChecker().accountIsEditable();
  }

  public ImportDialogPreviewChecker checkAccountNotEditable() {
    getAccountEditionChecker().checkAccountDisabled();
    return this;
  }

  public ImportDialogPreviewChecker checkAccountIsEditable() {
    getAccountEditionChecker().checkAccountEditable();
    return this;
  }

  public ImportDialogPreviewChecker importAndPreviewNextAccount() {
    dialog.getButton("next").click();
    return this;
  }

  public void importDeferred(String accountName, String fileName, boolean withMainAccount, String targetAccountName) {
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
      importAccountAndOpenNext();
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
    importAccountAndGetSummary().validate();
  }

  public ImportDialogPreviewChecker importAccountAndOpenNext() {
    importAndPreviewNextAccount();
    return this;
  }

  public ImportDialogPreviewChecker importAccountWithError() {
    importAndPreviewNextAccount();
    checkPreviewPanelShown();
    return this;
  }

  public ImportDialogCompletionChecker importAccountAndGetSummary() {
    importAndPreviewNextAccount();
    return new ImportDialogCompletionChecker(dialog);
  }

  public ImportDialogCompletionChecker importAccountWithAllSeriesAndGetSummary() {
    ImportSeriesChecker.init(dialog.getButton("next").triggerClick(), dialog)
      .validate();
    return new ImportDialogCompletionChecker(dialog);
  }

  public void importAccountAndComplete() {
    importAndPreviewNextAccount();
    try {
      new ImportDialogCompletionChecker(dialog).validate();
    }
    catch (Exception e) {
      Assert.fail("Could not validate import. " +
                  "A possible explanation is that the series import dialog was unexpectedly shown (dialog disposed) " +
                  "or there is another account to preview. Dialog content:\n" + dialog.getDescription());
    }
  }

  public void importAccountWithAllSeriesAndComplete() {
    ImportSeriesChecker.init(dialog.getButton("next").triggerClick(), dialog)
      .validateAndFinishImport();
    checkClosed();
  }

  public ImportDialogPreviewChecker checkMessageCreateFirstAccount() {
//    accountEditionChecker.checkErrorTipVisible();
//    accountEditionChecker.
    // verifier le sign post
//    dialog.getTextBox("You must create an account");
    return this;
  }

  public static void validateAndComplete(final int importedTransactionCount,
                                         final int ignoredTransactionCount,
                                         final int autocategorizedTransactionCount,
                                         final Panel dialogToClose) {

    ImportDialogCompletionChecker
      .checkAndClose(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount, dialogToClose);
    UISpecAssert.assertFalse(dialogToClose.isVisible());
  }

  public boolean isCompletion() {
    return dialog.getTextBox("title").getText().contains(Lang.get("import.completion.title"));
  }

  public ImportDialogCompletionChecker toCompletion() {
    assertThat(dialog.getTextBox("title").textContains(Lang.get("import.completion.title")));
    return new ImportDialogCompletionChecker(dialog);
  }

  public AccountPositionEditionChecker importAndEditPosition() {
    return new AccountPositionEditionChecker(dialog, "import.preview.next");
  }

  public void completeImportAndImportSeries() {
    Trigger trigger = dialog.getButton("ok").triggerClick();
    ImportSeriesChecker.init(trigger, dialog)
      .validateAndFinishImport();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void completeImportAndSkipSeries() {
    assertTrue(dialog.getTextBox("errorMessage").textIsEmpty());
    Trigger trigger = dialog.getButton(Lang.get("import.preview.next")).triggerClick();
    ImportSeriesChecker.init(trigger, dialog)
      .cancelImportSeries();
    UISpecAssert.assertFalse(dialog.isVisible());
  }


  public ImportDialogPreviewChecker checkDates(String... dates) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertTrue(dateFormatCombo.contentEquals(dates));
    return this;
  }

  public void completeImportStartFromZero(double amount) {
    AccountPositionEditionChecker positionEditionChecker = importAndEditPosition();
    positionEditionChecker.checkInitialAmountSelected(AmountFormat.DECIMAL_FORMAT.format(amount));
    positionEditionChecker.validate();
    ImportDialogCompletionChecker.checkAndClose(-1, -1, -1, dialog);
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public boolean isNewAccount() {
    UIComponent component = dialog.findUIComponent(ComponentMatchers.innerNameIdentity("targetAccountCombo"));
    return ((JComboBox) component.getAwtComponent()).getSelectedIndex() == 0;
  }

  public ImportDialogPreviewChecker selectDateFormat(String dateFormat) {
    ComboBox dateFormatCombo = dialog.getComboBox("dateFormatCombo");
    assertThat(dateFormatCombo.isVisible());
    dateFormatCombo.select(dateFormat);
    return this;
  }

  public ImportDialogPreviewChecker checkDateFormatHidden() {
    assertFalse(dialog.getComboBox("dateFormatCombo").isVisible());
    return this;
  }

  public ImportDialogPreviewChecker checkDateFormatSelected(String selection) {
    ComboBox combo = dialog.getComboBox("dateFormatCombo");
    assertThat(combo.isVisible());
    assertThat(combo.selectionEquals(selection));
    return this;
  }

  public ImportDialogPreviewChecker checkDateFormatMessageShown(String messageKey) {
    checkSignpostVisible(dialog, dialog.getComboBox("dateFormatCombo"), Lang.get(messageKey));
    return this;
  }

  public ImportDialogPreviewChecker checkErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("errorMessage").textEquals(Lang.get(message, arg)));
    return this;
  }

  public ImportDialogPreviewChecker checkHtmlErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("errorMessage").htmlEquals(Lang.get(message, arg)));
    return this;
  }

  public void skipAndComplete() {
    selectSkipFile();
    importAndPreviewNextAccount();
    validateAndComplete(-1, -1, -1, dialog);
  }

  public ImportDialogPreviewChecker checkExistingAccountDescription(String text) {
    TextBox description = dialog.getTextBox("readOnlyDescription");
    assertTrue(description.isVisible());
    assertTrue(description.textEquals(text));
    return this;
  }

  public ImportSeriesChecker importSeries() {
    return new ImportSeriesChecker(WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("import.preview.next"))
                                                                      .triggerClick()), dialog);
  }

  public CardTypeChooserChecker openCardTypeChooser() {
    Window window = WindowInterceptor.getModalDialog(dialog.getButton(Lang.get("account.error.missing.cardType.button")).triggerClick());
    return new CardTypeChooserChecker(window);
  }

  public void close() {
    dialog.getButton("close").click();
    checkClosed();
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }
}
