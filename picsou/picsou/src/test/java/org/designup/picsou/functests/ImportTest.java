package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.BankDownloadChecker;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.File;

public class ImportTest extends LoggedInFunctionalTestCase {

  private static final String SOCIETE_GENERALE = "Société Générale";

  public void testStandardImport() throws Exception {

    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .checkNoErrorMessage()
      .checkHeaderMessage("Select an OFX or QIF file to import")
      .acceptFile()
      .checkErrorMessage("login.data.file.required")
      .setFilePath("blah.ofx")
      .checkNoErrorMessage()
      .acceptFile()
      .checkErrorMessage("login.data.file.not.found")
      .browseAndSelect(path)
      .checkFilePath(path)
      .checkNoErrorMessage()
      .acceptFile()
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2006/01/10", "Menu K", "-1.10"}
      })
      .completeImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testLastMonthIsSelectedAfterImport() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/03/10", -1.1, "Menu K")
      .addTransaction("2006/02/10", -1.1, "Menu K")
      .load();

    timeline.checkSelection("2006/03");
  }

  public void testCloseButtonLabelBeforeImport() throws Exception {
    operations.openImportDialog()
      .checkCloseButton(Lang.get("import.fileSelection.close"))
      .close();
  }

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
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"2006/01/10", "Menu K", "-1.10"}
      })
      .setMainAccount()
      .doImport()
      .checkFileContent(new Object[][]{
        {"2006/02/20", "Menu K", "-2.20"}
      })
      .completeImport();

    timeline.checkSelection("2006/02");

    timeline.selectAll();
    views.selectData();
    transactions
      .initContent()
      .add("20/02/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.2)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testAcceptsOfcFiles() throws Exception {
    final String path = OfxBuilder.init(TestUtils.getFileName(this, ".ofc"))
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .browseAndSelect(path)
      .acceptFile()
      .setMainAccount()
      .completeImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

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

  public void testImportQifFileWithNoExistingAccount() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"2006/01/10", "Menu K", "-1.10"}
      })
      .defineAccount(SOCIETE_GENERALE, "My SG account", "0123546");
    importDialog.doImportWithBalance()
      .setAmount(0.00)
      .validate();
    importDialog.completeLastStep();

    views.selectData();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    views.selectHome();
    mainAccounts.edit("My SG account")
      .checkUpdateModeIsFileImport()
      .validate();
  }

  public void testManualInputAccountsAreShownInQifImport() throws Exception {

    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setAccountNumber("012345")
      .setUpdateModeToFileImport()
      .selectBank("CIC")
      .validate();

    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setUpdateModeToManualInput()
      .selectBank("Other")
      .validate();

    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .checkAvailableAccounts("Cash", "Main")
      .createNewAccount(SOCIETE_GENERALE, "SG", "12345", 100.0)
      .completeImport();

    mainAccounts.checkAccountNames("Main", "Cash", "SG");
    mainAccounts.edit("Main")
      .checkUpdateModeIsFileImport()
      .cancel();
    mainAccounts.edit("Cash")
      .checkUpdateModeIsManualInput()
      .cancel();
    mainAccounts.edit("SG")
      .checkUpdateModeIsFileImport()
      .cancel();
  }

  public void testSettingInitialBalanceForQifFiles() throws Exception {
    final String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(path)
      .acceptFile()
      .defineAccount(SOCIETE_GENERALE, "Main", "12345");

    importDialog
      .doImportWithBalance()
      .checkAccountLabel("Account: Main")
      .checkCancelNotAvailable()
      .checkEscNotAvailable()
      .checkInitialAmountSelected("0.0")
      .checkInitialMessageDisplayed()
      .setAmount(12.33)
      .validate();

    importDialog.completeLastStep();

    mainAccounts.checkAccount("Main", 12.33, "2006/01/10");
  }

  public void testImportTwoQifFilesInTwoDifferentAccounts() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "first")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .defineAccount(SOCIETE_GENERALE, "Main account", "00011")
      .completeImport(0.00);

    String secondQif = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "second")
      .save();

    operations.openImportDialog()
      .setFilePath(secondQif)
      .acceptFile()
      .checkAvailableAccounts("Main account")
      .createNewAccount(SOCIETE_GENERALE, "Second account", "00022", 12.30)
      .completeImport();

    mainAccounts.checkAccountNames("Main account", "Second account");

    String secondSecondQif = QifBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "secondAgain")
      .save();

    operations.openImportDialog()
      .setFilePath(secondSecondQif)
      .acceptFile()
      .checkSelectedAccount(null)
      .doImport()
      .checkErrorAccount()
      .selectAccount("Second account")
      .checkNoErrorMessage()
      .completeImport();
  }

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
      .acceptFile()
      .checkAccountMessage("Operations for single account:")
      .checkFileContent(new Object[][]{
        {"2006/01/10", "First operation", "-1.10"}
      })
      .doImport()
      .checkMessageCreateFirstAccount()
      .skipFile()
      .checkMessageCreateFirstAccount()
      .checkFileContent(new Object[][]{
        {"2006/01/20", "Second operation", "-2.20"}
      })
      .defineAccount(SOCIETE_GENERALE, "main", "1111")
      .completeImport(0.00);

    views.selectData();
    transactions
      .initContent()
      .add("20/01/2006", TransactionType.PRELEVEMENT, "Second operation", "", -2.2)
      .check();
  }

  public void testImportQifFileWithExistingAccount() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .checkAccountMessage("Operations for single account:")
      .defineAccount(SOCIETE_GENERALE, "Main account", "12345")
      .completeImport(0.00);

    String qifFile = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .setFilePath(qifFile)
      .acceptFile()
      .checkAvailableAccounts("Main account")
      .selectAccount("Main account")
      .completeImport();

    views.selectHome();
    mainAccounts.checkAccountNames("Main account");
  }

  public void testOfxWithUnknownBankEntities() throws Exception {
    String fileName = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/21", 1.0, "V'lib")
      .addBankAccount(777, 1027, "87654321", 21.0, "2008/06/21")
      .addTransaction("2008/06/10", 10.0, "McDo")
      .addCardAccount("1111222233334444", 7.5, "2008/06/21")
      .addTransaction("2008/06/10", 71.0, "Metro")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile();

    importDialog
      .selectBank(SOCIETE_GENERALE)
      .checkFileContent(new Object[][]{
        {"2008/06/10", "Metro", "71.00"},
      })
      .setMainAccount()
      .setDeferredAccount()
      .doImport();

    importDialog.selectBank(SOCIETE_GENERALE)
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2008/06/10", "V'lib", "1.00"},
      })
      .doImport();

    importDialog.selectBank(SOCIETE_GENERALE)
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2008/06/21", "V'lib", "1.00"},
      })
      .doImport();
    importDialog
      .checkFileContent(new Object[][]{
        {"2008/06/10", "McDo", "10.00"},
      })
      .selectBank(SOCIETE_GENERALE)
      .setMainAccount();

    importDialog
      .completeImport();

    String secondFileName = OfxBuilder.init(this)
      .addBankAccount(666, 2048, "77777777", 77.0, "2008/06/11")
      .addTransaction("2008/06/14", 1.0, "V'lib")
      .save();

    operations.openImportDialog()
      .setFilePath(secondFileName)
      .acceptFile()
      .setMainAccount()
      .completeImport();

    views.selectData();
    transactions
      .initContent()
      .add("21/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("14/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("10/06/2008", TransactionType.VIREMENT, "McDo", "", 10.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "Metro", "", 71.00)
      .check();
  }

  public void testUsingAnOfxImportForAManualAccountTurnsItIntoFileImportMode() throws Exception {
    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .setPosition(0.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount(666, 1024, "012345", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .loadInAccount("Cash");

    mainAccounts.edit("Cash")
      .checkUpdateModeIsFileImport()
      .cancel();
  }

  public void testSelectDateFormat() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("02/01/01", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .selectFiles(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"02/01/01", "Menu K", "-1.10"}
      })
      .defineAccount(SOCIETE_GENERALE, "main acount", "0123546")
      .checkDates("Year/Month/Day", "Month/Day/Year", "Day/Month/Year")
      .doImport()
      .checkDateFormatMessageShown("import.dateformat.undefined")
      .selectDateFormat("Day/Month/Year")
      .checkFileContent(new Object[][]{
        {"2001/01/02", "Menu K", "-1.10"}
      })
      .selectDateFormat("Month/Day/Year")
      .checkFileContent(new Object[][]{
        {"2001/02/01", "Menu K", "-1.10"}
      })
      .completeImport(0.00);

    views.selectData();
    transactions.initContent()
      .add("01/02/2001", TransactionType.PRELEVEMENT, "Menu K", "", -1.10)
      .check();
  }

  public void testImportQifBadFile() throws Exception {
    String path = TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(path,
                           "T123\n" +
                           "^sdfsf");

    ImportDialogChecker importDialog = operations.openImportDialog()
      .selectFiles(path)
      .acceptFile()
      .checkHtmlErrorMessage("import.file.empty", path);

//    importDialog
//      .clickErrorMessage()
//      .checkTitle("Invalid file")
//      .checkMessageContains("The content of this file is invalid")
//      .checkDetailsContain("Invalid date: / - items: []")
//      .close();

    importDialog.close();
  }

  public void testBadFormatForOfx() throws Exception {
    String path = org.globsframework.utils.TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(path,
                           "<bad>\n" +
                           "sdfsdfsdf\n" +
                           "</bad>");

    operations.openImportDialog()
      .selectFiles(path)
      .acceptFile()
      .checkHtmlErrorMessage("import.file.error", path)
      .close();
  }

  public void testImportKeepDirectory() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
      .checkDirectory(System.getProperty("user.home"))
      .browseAndSelect(path1)
      .acceptFile()
      .setMainAccount()
      .completeImport();

    operations.openImportDialog()
      .checkDirectory(new File(path1).getAbsoluteFile().getParent())
      .close();
  }

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
    operations.importQifFile(file2, SOCIETE_GENERALE);

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

  public void testImportFromDiferentAccountWithSameTransactionsInOfx() throws Exception {
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

  public void testImportFromDiferentAccountWithSameTransactionsInQif() throws Exception {
    String file1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();

    operations.importQifFile(file1, SOCIETE_GENERALE, 100.00);

    mainAccounts.createNewAccount()
      .setAccountName("other")
      .setPosition(100)
      .setAccountNumber("1213")
      .selectBank(SOCIETE_GENERALE)
      .validate();

    String file2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();

    operations.importFile(file2, "other");

    views.selectData();
    timeline.checkSelection("2006/01");
    transactions
      .initContent()
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .check();
  }

  public void testImportDialogGivesAccessToBankGuides() throws Exception {

    OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "00000111", 0.0, "2009/12/22")
      .addTransaction("2009/12/21", -15.00, "Menu K")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog();

    BankDownloadChecker bankDownload = importDialog.getBankDownload();

    bankDownload
      .checkContainsBanks("BNP Paribas", "CIC", "Crédit Agricole", "ING Direct", "Société Générale")
      .checkNoBankSelected()
      .checkManualDownloadHidden()
      .selectBank("CIC")
      .checkManualDownloadAvailable()
      .selectManualDownload()
      .checkManualDownloadHelp("CIC", "http://www.cic.fr");

    bankDownload
      .goBackToBankSelection()
      .setFilter("crédit")
      .checkContainsBanks("Crédit Agricole", "Chesterfield Federal Credit Union", "Other")
      .setFilter("BNP")
      .checkBankList("BNP Paribas", "BNPPF", "Other")
      .selectBank("BNP Paribas")
      .checkManualDownloadAvailable()
      .selectManualDownload()
      .checkManualDownloadHelp("BNP Paribas", "http://www.bnpparibas.net");

    importDialog.close();
  }

  public void testImportTwiceTheSameFile() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.3, "Menu K 1")
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .load(2, 0);

    String sameFile = OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.3, "Menu K 1")
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();

    operations.openImportDialog()
      .setFilePath(sameFile)
      .acceptFile()
      .completeImportNone(2);

    views.selectCategorization();
    categorization.showLastImportedFileOnly();
    categorization.selectTableRows(0); // check that there are operations (the last import is the last import with operations)

    String otherFile = OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.3, "Menu K 2")
      .save();

    operations.openImportDialog()
      .setFilePath(sameFile + ";" + otherFile)
      .acceptFile()
      .doImport()
      .completeImport();

    transactions.initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MENU K 1", "", -1.30)
      .check();
  }

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
      .acceptFile()
      .checkMessageEmptyFile()
      .setFilePath(notEmpty)
      .acceptFile()
      .checkAccountMessage("Operations for single account:")
      .checkAccountSelectionMessage("Import operations in:")
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  public void testImportDirectory() throws Exception {
    operations.openImportDialog()
      .setFilePath("/tmp")
      .acceptFile()
      .checkErrorMessage("import.file.is.directory", "/tmp")
      .close();
  }

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
      .acceptFile()
      .checkNoErrorMessage()
      .setMainAccount()
      .doImport()
      .completeLastStep();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  public void testLoadUnknownQifIfContentOk() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();
    File file = new File(fileName);
    File newFileName = new File(fileName.replace(".qif", ".toto"));
    file.renameTo(newFileName);
    operations.openImportDialog()
      .setFilePath(newFileName.getPath())
      .acceptFile()
      .checkAstericsErrorOnName()
      .checkAstericsErrorOnBank()
      .checkNoErrorMessage()
      .defineAccount("CIC", "main", "1111")
      .checkAstericsClearOnBank()
      .checkAstericsClearOnName()
      .completeImport(0.00);

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

  public void testMixedInvalidDirAndFile() throws Exception {
    String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .save();
    operations.openImportDialog()
      .setFilePath(fileName + ";" + "/tmp")
      .acceptFile()
      .checkNoErrorMessage()
      .setMainAccount()
      .doImport()
      .completeLastStep();

    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MENU K 2", "", -1.30)
      .check();
  }

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
      .acceptFile()
      .checkNoErrorMessage()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .doImport()
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

  public void testMultipleEmptyAccounts() throws Exception {
    String path1 = OfxBuilder.init(this)
      .addBankAccount("111", 100, "2011/10/01")
      .addBankAccount("112", 100, "2011/10/01")
      .addBankAccount("113", 100, "2011/10/01")
      .save();
    operations.openImportDialog()
      .selectFiles(path1)
      .acceptFile()
      .checkAccountMessage("Account 1/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .setMainAccount()
      .doNext()
      .setMainAccount()
      .checkAccountMessage("Account 2/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .doNext()
      .setMainAccount()
      .checkAccountMessage("Account 3/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .completeImportWithNext();

    mainAccounts.checkAccountNames("Account n. 111", "Account n. 112", "Account n. 113");

    String path2 = OfxBuilder.init(this)
      .addBankAccount("1114", 100, "2011/10/03")
      .addBankAccount("112", 200, "2011/10/03")
      .addBankAccount("113", 300, "2011/10/03")
      .addTransaction("2011/10/03", 100, "Anniversaire")
      .save();
    operations.openImportDialog()
      .selectFiles(path2)
      .acceptFile()
      .checkSelectedAccount("Account n. 113")
      .checkAccountNotEditable()
      .checkAccountDescription("Account n.113 Other Position: 100.00 on 2011/10/01")
      .selectAccount("a new account")
      .checkAstericsErrorOnName()
      .checkAccountPosition(300.)
      .selectAccount("Account n. 113")
      .doImport()
      .setMainAccount()
      .doNext()
      .completeImportWithNext();

    mainAccounts.checkAccount("Account n. 113", 300., "2011/10/03");
    mainAccounts.checkAccount("Account n. 112", 200., "2011/10/03");
  }

  public void testEmptyAssociatedToOldFollowedByNew() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();

    String path = OfxBuilder.init(this)
      .addBankAccount("111", 100, "2011/10/01")
      .addBankAccount("112", 100, "2011/10/01")
      .addBankAccount("113", 100, "2011/10/01")
      .save();

    ImportDialogChecker checker = operations.openImportDialog();
    checker
      .selectFiles(path)
      .acceptFile()
      .checkAccountMessage("Account 1/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .selectAccount("Account n. 00001123")
      .doNext();
    checker
      .checkAccountEditable()
      .checkAccountMessage("Account 2/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .selectAccount("Account n. 00001123")
      .checkAccountNotEditable()
      .checkAccountDescription("Account n.00001123 CIC Position: 100.00 on 2011/10/01")
      .selectAccount("a new account")
      .checkAccountMessage("Account 2/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .checkAccountEditable()
      .setMainAccount()
      .doNext();
    checker
      .checkAccountMessage("Account 3/3 - No operations")
      .checkAccountSelectionMessage("Update:")
      .setMainAccount()
      .completeImportWithNext();

    mainAccounts.checkAccountNames("Account n. 00001123", "Account n. 112", "Account n. 113");
  }

  public void testAssociatToAccountAndDeleteIt() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();

    mainAccounts.edit("Account n. 00001123").delete().validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .load();
  }

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

    mainAccounts.checkSummary(0., "2008/09/02");

  }
}
