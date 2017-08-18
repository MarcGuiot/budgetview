package com.budgetview.functests.importexport;

import com.budgetview.desktop.time.TimeService;
import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.ImportDialogPreviewChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.QifBuilder;
import com.budgetview.model.TransactionType;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportTest extends LoggedInFunctionalTestCase {

  private static final String SOCIETE_GENERALE = "Société Générale";

  @Test
  public void testStandardImport() throws Exception {

    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog();
    importDialog
      .checkNoErrorMessage()
      .checkHeaderMessage("Select an OFX or QIF file to import")
      .checkFileImportDisabled()
      .setFilePath("blah.ofx")
      .checkFileImportEnabled()
      .checkNoErrorMessage()
      .importFileWithError("login.data.file.not.found");
    importDialog
      .browseAndPreview(path)
      .checkFileNameShown(path)
      .checkTransactions(new Object[][]{
        {"2006/01/10", "Menu K", "-1.10"}
      })
      .setMainAccount()
      .importAccountAndComplete();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testLastMonthIsSelectedAfterImport() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/03/10", -1.1, "Menu K")
      .addTransaction("2006/02/10", -1.1, "Menu K")
      .load();

    timeline.checkSelection("2006/03");
  }

  @Test
  public void testCloseButtonLabelBeforeImport() throws Exception {
    operations.openImportDialog()
      .checkCloseButton(Lang.get("import.close"))
      .close();
  }

  @Test
  public void testImportSeveralFiles() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    final String path2 = OfxBuilder
      .init(this)
      .addTransaction("2006/02/20", -2.2, "Menu K")
      .save();

    operations.openImportDialog()
      .setFilePath(path1 + ";" + path2)
      .importFileAndPreview()
      .checkTransactions(new Object[][]{
        {"2006/01/10", "Menu K", "-1.10"}
      })
      .setMainAccount()
      .importAccountAndOpenNext()
      .checkTransactions(new Object[][]{
        {"2006/02/20", "Menu K", "-2.20"}
      })
      .importAccountAndComplete();

    timeline.checkSelection("2006/02");

    timeline.selectAll();
    views.selectData();
    transactions
      .initContent()
      .add("20/02/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.2)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testAcceptsOfcFiles() throws Exception {
    final String path = OfxBuilder.init(TestUtils.getFileName(this, ".ofc"))
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .browseAndPreview(path)
      .setMainAccount()
      .importAccountAndComplete();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testSameOperations() throws Exception {
    final String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/09", -1.1, "Menu K")
      .save();

    operations.importQifFile(path, SOCIETE_GENERALE, 0.00);

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testImportQifFileWithNoExistingAccount() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogPreviewChecker preview =
      operations.openImportDialog()
        .setFilePath(path1)
        .importFileAndPreview()
        .checkTransactions(new Object[][]{
          {"2006/01/10", "Menu K", "-1.10"}
        })
        .checkNewAccountSelected()
        .defineAccount(SOCIETE_GENERALE, "My SG account", "0123546");
    preview
      .importAndEditPosition()
      .setAmount(0.00)
      .validate();
    preview.toCompletion().validate();

    views.selectData();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testManualInputAccountsAreShownInQifImport() throws Exception {

    // Create account in import mode
    accounts.createNewAccount()
      .setName("Main")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("012345", 1000.00, "2006/01/01")
      .addTransaction("2006/01/01", 10, "monop")
      .loadInAccount("Main");

    // Create account in manual mode
    accounts.createNewAccount()
      .setName("Cash")
      .selectBank("Other")
      .validate();
    transactionCreation.show()
      .createToBeReconciled(12, "BLAH", -100.00);

    // Open QIF import
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .importFileAndPreview()
      .checkAvailableAccounts("Main", "Cash")
      .setNewAccount(SOCIETE_GENERALE, "SG", "12345", 100.0)
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main", "Cash", "SG");
  }

  @Test
  public void testSettingInitialBalanceForQifFiles() throws Exception {
    final String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(path)
      .importFileAndPreview()
      .defineAccount(SOCIETE_GENERALE, "Main", "12345");

    preview
      .importAndEditPosition()
      .checkAccountLabel("Account: Main")
      .checkCancelNotAvailable()
      .checkEscNotAvailable()
      .checkInitialAmountSelected("-1.10")
      .checkInitialMessageDisplayed()
      .setAmount(12.33)
      .validate();

    preview.toCompletion().validate();

    mainAccounts.checkAccount("Main", 12.33, "2006/01/10");
  }

  @Test
  public void testImportTwoQifFilesInTwoDifferentAccounts() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "first")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .importFileAndPreview()
      .defineAccount(SOCIETE_GENERALE, "Main account", "00011")
      .setPosition(0.00)
      .importAccountAndComplete();

    String secondQif = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "second")
      .save();

    operations.openImportDialog()
      .setFilePath(secondQif)
      .importFileAndPreview()
      .checkAvailableAccounts("Main account")
      .setNewAccount(SOCIETE_GENERALE, "Second account", "00022", 12.30)
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account", "Second account");

    String secondSecondQif = QifBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "secondAgain")
      .save();

    operations.openImportDialog()
      .setFilePath(secondSecondQif)
      .importFileAndPreview()
      .checkNewAccountSelected()
      .importAccountAndOpenNext()
      .checkAccountError()
      .selectAccount("Second account")
      .checkNoErrorMessage()
      .importAccountAndComplete();
  }

  @Test
  public void testSkipFirstQifFile() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "First operation")
      .save();
    final String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/20", -2.2, "Second operation")
      .save();

    operations.openImportDialog()
      .setFilePath(path1 + ";" + path2)
      .importFileAndPreview()
      .checkAccountMessage("New operations for account:")
      .checkTransactions(new Object[][]{
        {"2006/01/10", "First operation", "-1.10"}
      })
      .importAccountAndOpenNext()
      .checkMessageCreateFirstAccount()
      .selectSkipFile()
      .checkSkippedFileMessage()
      .importAccountAndOpenNext()
      .checkTransactions(new Object[][]{
        {"2006/01/20", "Second operation", "-2.20"}
      })
      .defineAccount(SOCIETE_GENERALE, "main", "1111")
      .setPosition(0.00)
      .importAccountAndComplete();

    views.selectData();
    transactions
      .initContent()
      .add("20/01/2006", TransactionType.PRELEVEMENT, "Second operation", "", -2.2)
      .check();
  }

  @Test
  public void testImportQifFileWithExistingAccount() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .importFileAndPreview()
      .checkAccountMessage("New operations for account:")
      .defineAccount(SOCIETE_GENERALE, "Main account", "12345")
      .setPosition(0.00)
      .importAccountAndComplete();

    String qifFile = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .setFilePath(qifFile)
      .importFileAndPreview()
      .checkAvailableAccounts("Main account")
      .selectAccount("Main account")
      .importAccountAndComplete();

    views.selectHome();
    mainAccounts.checkAccounts("Main account");
  }

  @Test
  public void testOfxWithUnknownBankEntities() throws Exception {
    String fileName = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/21", 1.0, "V'lib")
      .addBankAccount(777, 1027, "87654321", 21.0, "2008/06/21")
      .addTransaction("2008/06/10", 10.0, "McDo")
      .addCardAccount("11112222", 7.5, "2008/06/21")
      .addTransaction("2008/06/10", 71.0, "Metro")
      .save();

    ImportDialogPreviewChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview();

    importDialog.selectBank(SOCIETE_GENERALE)
      .setMainAccount()
      .checkTransactions(new Object[][]{
        {"2008/06/10", "V'lib", "1.00"},
      })
      .importAccountAndOpenNext();

    importDialog.selectBank(SOCIETE_GENERALE)
      .setMainAccount()
      .checkTransactions(new Object[][]{
        {"2008/06/21", "V'lib", "1.00"},
      })
      .importAccountAndOpenNext();

    importDialog
      .checkTransactions(new Object[][]{
        {"2008/06/10", "McDo", "10.00"},
      })
      .selectBank(SOCIETE_GENERALE)
      .setMainAccount()
      .importAccountAndOpenNext();

    importDialog
      .selectBank(SOCIETE_GENERALE)
      .checkTransactions(new Object[][]{
        {"2008/06/10", "Metro", "71.00"},
      })
      .setMainAccount()
      .setDeferredAccount(25, 28, 0, "Account n. 12345678a");

    importDialog
      .importAccountAndComplete();

    String secondFileName = OfxBuilder.init(this)
      .addBankAccount(666, 2048, "77777777", 77.0, "2008/06/11")
      .addTransaction("2008/06/14", 1.0, "V'lib")
      .save();

    operations.openImportDialog()
      .setFilePath(secondFileName)
      .importFileAndPreview()
      .setMainAccount()
      .importAccountAndComplete();

    views.selectData();
    transactions
      .initContent()
      .add("21/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("14/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("10/06/2008", TransactionType.VIREMENT, "McDo", "", 10.00)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "Metro", "", 71.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .check();
  }

  @Test
  public void testSelectDateFormat() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("02/01/01", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .selectFiles(path1)
      .importFileAndPreview()
      .checkTransactions(new Object[][]{
        {"02/01/01", "Menu K", "-1.10"}
      })
      .defineAccount(SOCIETE_GENERALE, "main acount", "0123546")
      .checkDates("Year/Month/Day", "Month/Day/Year", "Day/Month/Year")
      .importAccountWithError()
      .checkDateFormatMessageShown("import.dateformat.undefined")
      .selectDateFormat("Day/Month/Year")
      .checkTransactions(new Object[][]{
        {"2001/01/02", "Menu K", "-1.10"}
      })
      .selectDateFormat("Month/Day/Year")
      .checkTransactions(new Object[][]{
        {"2001/02/01", "Menu K", "-1.10"}
      })
      .setPosition(0.00)
      .importAccountAndComplete();

    views.selectData();
    transactions.initContent()
      .add("01/02/2001", TransactionType.PRELEVEMENT, "Menu K", "", -1.10)
      .check();
  }

  @Test
  public void testImportQifBadFile() throws Exception {
    String path = TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(path,
                           "T123\n" +
                           "^sdfsf");

    operations.openImportDialog()
      .selectFiles(path)
      .importFileWithHtmlError("import.file.empty", path)
      .close();
  }

  @Test
  public void testBadFormatForOfx() throws Exception {
    String path = org.globsframework.utils.TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(path,
                           "<bad>\n" +
                           "sdfsdfsdf\n" +
                           "</bad>");

    operations.openImportDialog()
      .selectFiles(path)
      .importFileWithHtmlError("import.file.error", path)
      .close();
  }

  @Test
  public void testImportKeepDirectory() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .checkDirectory(System.getProperty("user.home"))
      .browseAndPreview(path1)
      .setMainAccount()
      .importAccountAndComplete();

    operations.openImportDialog()
      .checkDirectory(new File(path1).getAbsoluteFile().getParent())
      .close();
  }

  @Test
  public void testImportReOrderId() throws Exception {
    String file1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -3, "Menu K")
      .addTransaction("2006/01/10", -2, "Menu K")
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();
    operations.importQifFile(file1, SOCIETE_GENERALE, 100.00);

    String file2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/11", -4, "Menu K")
      .addTransaction("2006/01/12", -5, "Menu K")
      .addTransaction("2006/01/12", -6, "Menu K")
      .save();
    operations.importQifFile(file2);

    transactions.getTable().getHeader().click(1);
    transactions
      .initAmountContent()
      .add("12/01/2006", "MENU K", -6.00, "To categorize", 85.00, 85.00, "Main account")
      .add("12/01/2006", "MENU K", -5.00, "To categorize", 91.00, 91.00, "Main account")
      .add("11/01/2006", "MENU K", -4.00, "To categorize", 96.00, 96.00, "Main account")
      .add("10/01/2006", "MENU K", -3.00, "To categorize", 100.00, 100.00, "Main account")
      .add("10/01/2006", "MENU K", -2.00, "To categorize", 103.00, 103.00, "Main account")
      .add("09/01/2006", "MENU K", -1.00, "To categorize", 105.00, 105.00, "Main account")
      .check();
  }

  @Test
  public void testImportFromDifferentAccountsWithSameTransactionsInOfx() throws Exception {
    String fileName1 = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .save();
    operations.importOfxFile(fileName1, "Other");

    String fileName2 = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .save();
    operations.importOfxFile(fileName2, "Other");
    transactions
      .initContent()
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .check();
  }

  @Test
  public void testImportFromDifferentAccountsWithSameTransactionsInQif() throws Exception {
    String file1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();

    operations.importQifFile(file1, SOCIETE_GENERALE, 100.00);

    accounts.createNewAccount()
      .setName("other")
      .setPosition(100)
      .setAccountNumber("1213")
      .selectBank(SOCIETE_GENERALE)
      .validate();

    String file2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();

    operations.importFile(file2, "other", 1000.00);

    views.selectData();
    timeline.checkSelection("2006/01");
    transactions
      .initContent()
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .check();
  }

  @Test
  public void testLastImportCoversAllImportedAccounts() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("0001", 100.00, "2006/01/30")
      .addTransaction("2006/01/11", -10.00, "Menu K 1.1")
      .addTransaction("2006/01/12", -10.00, "Menu K 1.2")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("0002", 200.00, "2006/01/31")
      .addTransaction("2006/01/20", -10.00, "Menu K 2.1")
      .addTransaction("2006/01/21", -10.00, "Menu K 2.2")
      .addBankAccount("0003", 300.00, "2006/01/31")
      .addTransaction("2006/01/30", -10.00, "Menu K 3.1")
      .addTransaction("2006/01/31", -10.00, "Menu K 3.2")
      .load();

    views.selectCategorization();
    categorization.showLastImportedFileOnly();
    categorization.initContent()
      .add("20/01/2006", "", "MENU K 2.1", -10.00)
      .add("21/01/2006", "", "MENU K 2.2", -10.00)
      .add("30/01/2006", "", "MENU K 3.1", -10.00)
      .add("31/01/2006", "", "MENU K 3.2", -10.00)
      .check();
  }

  @Test
  public void testImportingTheSameFileTwice() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.3, "Menu K 1")
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .load(2, 0, 0);

    String sameFile = OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.3, "Menu K 1")
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();

    operations.openImportDialog()
      .setFilePath(sameFile)
      .importFileAndPreview()
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(0, 2, 0);

    views.selectCategorization();
    categorization.showLastImportedFileOnly();
    categorization.selectTableRows(0); // check that there are operations (the last import is the last import with operations)

    String otherFile = OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.3, "Menu K 2")
      .save();

    operations.openImportDialog()
      .setFilePath(sameFile + ";" + otherFile)
      .importFileAndPreview()
      .importAccountAndOpenNext()
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(1, 2, 0);

    transactions.initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MENU K 1", "", -1.30)
      .check();
  }

  @Test
  public void testImportEmptyFile() throws Exception {
    String emptyFile = OfxBuilder
      .init(this)
      .save();

    String notEmpty = OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();

    operations.openImportDialog()
      .setFilePath(emptyFile)
      .importFileWithError()
      .checkMessageEmptyFile()
      .setFilePath(notEmpty)
      .importFileAndPreview()
      .checkAccountMessage("New operations for account:")
      .checkNewAccountSelected()
      .setMainAccount()
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(1, 0, 0);

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  @Test
  public void testImportDirectory() throws Exception {
    operations.openImportDialog()
      .setFilePath("/tmp")
      .importFileWithError("import.file.is.directory", "/tmp")
      .close();
  }

  @Test
  public void testLoadUnknownOfxIfContentOk() throws Exception {
    String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();

    File file = new File(fileName);
    File newFileName = new File(fileName.replace(".ofx", ".toto"));
    file.renameTo(newFileName);

    operations.openImportDialog()
      .setFilePath(newFileName.getPath())
      .importFileAndPreview()
      .checkNoErrorMessage()
      .setMainAccount()
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  @Test
  public void testLoadUnknownQifIfContentOk() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();
    File file = new File(fileName);
    File newFileName = new File(fileName.replace(".qif", ".toto"));
    file.renameTo(newFileName);
    operations.openImportDialog()
      .setFilePath(newFileName.getPath())
      .importFileAndPreview()
      .checkAstericsErrorOnName()
      .checkAstericsErrorOnBank()
      .checkNoErrorMessage()
      .defineAccount("CIC", "main", "1111")
      .checkAstericsClearOnBank()
      .checkAstericsClearOnName()
      .setPosition(0.00)
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  @Test
  public void testMixedInvalidDirAndFile() throws Exception {
    String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();
    operations.openImportDialog()
      .setFilePath(fileName + ";" + "/tmp")
      .importFileAndPreview()
      .checkNoErrorMessage()
      .setMainAccount()
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  @Test
  public void testImportSameTransactionWitSplitted() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .addTransaction("2008/06/9", 1.0, "V'lib")
      .addTransaction("2008/06/10", 3.0, "V'lib")
      .addTransaction("2008/06/10", 2.0, "other 1")
      .addTransaction("2008/06/10", 2.0, "V'lib")
      .addTransaction("2008/06/10", 4.0, "V lib")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(4);
    transactionDetails.split("1", "a pied");

    categorization.selectTableRows(3);

    OfxBuilder.init(this)
      .addTransaction("2008/06/9", 1.0, "V'lib")
      .addTransaction("2008/06/10", 2.0, "other 1")
      .addTransaction("2008/06/10", 2.0, "V'lib")
      .addTransaction("2008/06/10", 3.0, "V'lib")
      .addTransaction("2008/06/10", 4.0, "V'lib")
      .addTransaction("2008/06/11", 2.0, "V'lib")
      .load();

    views.selectData();

    transactions
      .initContent()
      .add("11/06/2008", TransactionType.VIREMENT, "V'lib", "", 2.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V lib", "", 4.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 2.00)
      .add("10/06/2008", TransactionType.VIREMENT, "other 1", "", 2.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 2.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "a pied", 1.00)
      .add("09/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("08/06/2008", TransactionType.VIREMENT, "V'lib", "", 2.00)
      .check();
  }

  @Test
  public void testInvalidSecondFile() throws Exception {
    final String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/09", -1.1, "Menu K")
      .save();

    Files.dumpStringToFile("badFile.ofx", "some bad content");

    operations
      .openImportDialog()
      .selectFiles(path, "badFile.ofx")
      .importFileAndPreview()
      .checkNoErrorMessage()
      .setNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importAccountWithError()
      .checkHtmlErrorMessage("import.file.error", path)
      .skipAndComplete();

    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();

    final String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K2")
      .save();

    operations.importOfxOnAccount(path2, "Main account");

    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU K2", "", -1.10)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  @Test
  public void testMultipleEmptyAccounts() throws Exception {
    String path1 = OfxBuilder.init(this)
      .addBankAccount("111", 100, "2008/08/01")
      .addBankAccount("112", 100, "2008/08/01")
      .addBankAccount("113", 100, "2008/08/01")
      .save();
    operations.openImportDialog()
      .selectFiles(path1)
      .importFileAndPreview()
      .checkAccountMessage("New operations for account 1/3:")
      .checkSkipFileSelected()
      .selectNewAccount()
      .setMainAccount()
      .importAccountAndOpenNext()
      .checkAccountMessage("New operations for account 2/3:")
      .checkSkipFileSelected()
      .selectNewAccount()
      .setMainAccount()
      .importAccountAndOpenNext()
      .checkAccountMessage("New operations for account 3/3:")
      .checkSkipFileSelected()
      .selectNewAccount()
      .setMainAccount()
      .checkNewAccountSelected()
      .importAccountAndComplete();

    mainAccounts.checkPosition("Account n. 111", 100.);
    mainAccounts.checkAccounts("Account n. 111", "Account n. 112", "Account n. 113");

    notifications.checkHidden();

    String path2 = OfxBuilder.init(this)
      .addBankAccount("1114", 100, "2008/08/03")
      .addBankAccount("112", 200, "2008/08/03")
      .addBankAccount("113", 300, "2008/08/03")
      .addTransaction("2008/08/03", 100, "Anniversaire")
      .save();
    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .selectFiles(path2)
      .importFileAndPreview()
      .checkSelectedAccount("Account n. 113")
      .checkAccountNotEditable()
      .checkExistingAccountDescription("Account n.113 Other Position: 100.00 on 2008/08/01")
      .selectNewAccount()
      .checkAstericsErrorOnName()
      .checkAccountPosition(300.00)
      .selectAccount("Account n. 113")
      .importAccountAndOpenNext();
    preview
      .checkSkipFileSelected()
      .selectNewAccount()
      .setMainAccount()
      .importAccountAndOpenNext();
    preview
      .importAccountAndComplete();

    mainAccounts.checkAccount("Account n. 113", 200.00, "2008/08/03");
    mainAccounts.checkAccount("Account n. 112", 100.00, "2008/08/01");
    views.selectHome();

    notifications.openDialog()
      .checkMessageCount(2)
      .checkMessage(0, "The last computed position for 'Account n. 112' (100.00) " +
                       "is not the same as the imported one (200.00)")
      .checkMessage(1, "The last computed position for 'Account n. 113' (200.00) " +
                       "is not the same as the imported one (300.00)")
      .runAction(0)
      .close();

    views.checkDataSelected();
    mainAccounts.checkSelectedAccounts("Account n. 112");
    transactions.checkEmpty();

    notifications.checkVisible(2);
    mainAccounts.edit("Account n. 112")
      .delete();

    notifications.checkVisible(1)
      .openDialog()
      .checkMessageCount(1)
      .checkMessage(0, "The last computed position for 'Account n. 113' (200.00) is not the same as the " +
                       "imported one (300.00)")
      .clearMessage(0)
      .close();

    notifications.checkHidden();
  }

  @Test
  public void testNavigatingFromTheAccountErrorNotificationDialog() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("111", 100.00, "2008/08/01")
      .addTransaction("2008/08/01", -10.00, "Transaction 1a")
      .addBankAccount("112", 100.00, "2008/08/01")
      .addTransaction("2008/08/01", -20.00, "Transaction 2a")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("111", 120.00, "2008/08/02")
      .addTransaction("2008/08/02", -20.00, "Transaction 1b")
      .load();

    mainAccounts.checkPosition("Account n. 111", 80.00);

    views.selectHome();
    notifications.checkVisible(1);
    notifications.openDialog()
      .checkMessageCount(1)
      .checkMessage(0, "The last computed position for 'Account n. 111' (80.00) " +
                       "is not the same as the imported one (120.00)")
      .runAction(0)
      .close();

    views.checkDataSelected();
    mainAccounts.checkSelectedAccounts("Account n. 111");
    transactions.initContent()
      .add("02/08/2008", TransactionType.PRELEVEMENT, "TRANSACTION 1B", "", -20.00)
      .add("01/08/2008", TransactionType.PRELEVEMENT, "TRANSACTION 1A", "", -10.00)
      .check();

    notifications.openDialog()
      .checkMessageCount(1)
      .clearMessage(0)
      .close();
    notifications.checkHidden();
  }

  @Test
  public void testEmptyAssociatedToOldFollowedByNew() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();

    String path = OfxBuilder.init(this)
      .addBankAccount("111", 100, "2008/08/01")
      .addBankAccount("112", 100, "2008/08/01")
      .addBankAccount("113", 100, "2008/08/01")
      .save();

    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .selectFiles(path)
      .importFileAndPreview();

    preview
      .checkFileNameShown(path)
      .checkAccountMessage("New operations for account 1/3:")
      .checkSkipFileSelected()
      .selectNewAccount()
      .selectAccount("Account n. 00001123")
      .importAccountAndOpenNext();

    preview
      .checkSkipFileSelected()
      .selectNewAccount()
      .checkAccountIsEditable()
      .checkAccountMessage("New operations for account 2/3:")
      .selectAccount("Account n. 00001123")
      .checkAccountNotEditable()
      .checkExistingAccountDescription("Account n.00001123 CIC Position: 0.00 on 2008/06/08")
      .selectNewAccount()
      .checkAccountMessage("New operations for account 2/3:")
      .checkAccountIsEditable()
      .setMainAccount()
      .importAccountAndOpenNext();

    preview
      .checkAccountMessage("New operations for account 3/3:")
      .checkSkipFileSelected()
      .selectNewAccount()
      .setMainAccount()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Account n. 00001123", "Account n. 112", "Account n. 113");
  }

  @Test
  public void testAssociatToAccountAndDeleteIt() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();

    mainAccounts.edit("Account n. 00001123").openDelete().validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();
  }

  @Test
  public void testImportWithDateInTheFuture() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/09/02", 2.0, "V'lib")
      .addTransaction("2008/09/02", 2.0, "V'lib")
      .load();

    timeline.selectAll();
//    transactions.initContent()
//      .add("31/08/2008", TransactionType.VIREMENT, "V'LIB", "", 2.00)
//      .add("31/08/2008", TransactionType.VIREMENT, "V'LIB", "", 2.00)
//      .add("08/06/2008", TransactionType.VIREMENT, "V'LIB", "", 2.00)
//      .add("08/06/2008", TransactionType.VIREMENT, "V'LIB", "", 2.00)
//      .check();

    mainAccounts.checkReferencePosition(0., "2008/06/08");
  }

  @Test
  public void testMixDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/5", 2.0, "V'lib")
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load(2, 0, 0);

    timeline.selectAll();
    transactions.initAmountContent()
      .add("08/06/2008", "V'LIB", 2.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("05/06/2008", "V'LIB", 2.00, "To categorize", -2.00, -2.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", 2.0, "V'lib")
      .addTransaction("2008/06/09", 2.0, "V'lib")
      .load(2, 0, 0);

    timeline.selectAll();
    transactions.initAmountContent()
      .add("09/06/2008", "V'LIB", 2.00, "To categorize", 4.00, 4.00, "Account n. 00001123")
      .add("08/06/2008", "V'LIB", 2.00, "To categorize", 2.00, 2.00, "Account n. 00001123")
      .add("06/06/2008", "V'LIB", 2.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("05/06/2008", "V'LIB", 2.00, "To categorize", -2.00, -2.00, "Account n. 00001123")
      .check();

    mainAccounts.checkReferencePosition(4., "2008/06/09");
  }

  @Test
  public void testImportAndChangeDate() throws Exception {
    MutableNow now = new MutableNow();

    OfxBuilder.init(this)
      .addBankAccount("111", 100, now.format())
      .addTransaction(now.format(), -100.00, "Virement 1 ")
      .load();

    now.addOneDay();
    operations.nextSixDays();

    OfxBuilder.init(this)
      .addBankAccount("111", 110., now.format())
      .addTransaction(now.format(), -100.00, "Virement 2")
      .load();

    notifications.checkVisible(1);

    now.set(TimeService.getToday().getTime());
    OfxBuilder.init(this)
      .addBankAccount("111", -100., now.format())
      .addTransaction(now.format(), -100.00, "Virement 3")
      .load();

    notifications.checkHidden();

    timeline.selectAll();
    transactions.initAmountContent()
      .add("06/09/2008", "VIREMENT 3", -100.00, "To categorize", -100.00, -100.00, "Account n. 111")
      .add("01/09/2008", "VIREMENT 2", -100.00, "To categorize", 0.00, 0.00, "Account n. 111")
      .add("31/08/2008", "VIREMENT 1", -100.00, "To categorize", 100.00, 100.00, "Account n. 111")
      .check();

    fail("Si on met -100 a la place de -110 dans la position de compte, changeSet.containChange retourne false car c'est la meme valeur " +
         "meme si c'est un nouvelle import, du coup, on ne fait pas le check car on pense que ce n'est pas un import " +
         "/org/designup/picsou/triggers/PositionTrigger.java:449");
  }

  private static class MutableNow implements TimeService.Now {
    private long time;
    Format format = new SimpleDateFormat("yyyy/MM/dd");

    private MutableNow() {
      time = TimeService.getToday().getTime();
    }

    public long now() {
      return time;
    }

    public void set(long time) {
      this.time = time;
    }

    public void addOneDay() {
      time += 24 * 3600 * 1000;
    }

    public String format() {
      return format.format(new Date(time));
    }
  }
}
