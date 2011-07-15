package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
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
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .completeImport();

    views.selectData();
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
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .setMainAccount()
      .doImport()
      .checkFileContent(new Object[][]{
        {"20/02/2006", "Menu K", "-2.20"}
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

    views.selectData();
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

    views.selectData();
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
        {"10/01/2006", "Menu K", "-1.10"}
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

  public void testManualInputAccountsNotShownInQifImport() throws Exception {

    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setAccountNumber("012345")
      .setUpdateModeToFileImport()
      .selectBank("CIC")
      .validate();

    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setUpdateModeToManualInput()
      .selectBank("Autre")
      .validate();

    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    operations.openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .checkAvailableAccounts("Main")
      .createNewAccount(SOCIETE_GENERALE, "SG", "12345", 100.0)
      .completeImport();

    views.selectHome();
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

    views.selectHome();
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
      .checkSelectedAccount("Second account")
      .completeImport();

    views.selectHome();
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
      .checkErrorMessage("import.no.account")
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
      .checkFileContent(new Object[][]{
        {"10/01/2006", "First operation", "-1.10"}
      })
      .doImport()
      .checkMessageCreateFirstAccount()
      .skipFile()
      .checkMessageCreateFirstAccount()
      .checkFileContent(new Object[][]{
        {"20/01/2006", "Second operation", "-2.20"}
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

  public void testImportWithCreateAccountChecksAccountBankIsFilled() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      });
    importDialog
      .openAccount()
      .checkNoBankSelected()
      .setAccountNumber("0123546")
      .cancel();
    AccountEditionChecker accountEditionChecker = importDialog.doImport()
      .checkMessageCreateFirstAccount()
      .openAccount();
    accountEditionChecker.openBankSelection()
      .selectBank("CIC")
      .cancel();
    accountEditionChecker.checkNoBankSelected();
    accountEditionChecker.openBankSelection()
      .selectBank("LCL")
      .validate();
    accountEditionChecker.checkSelectedBank("LCL")
      .validate();
    importDialog.skipAndComplete();
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
    importDialog.selectOfxAccountBank(SOCIETE_GENERALE)
      .setMainAccountForAll()
      .checkFileContent(new Object[][]{
        {"10/06/2008", "V'lib", "1.00"},
      })
      .doImport();
    importDialog.selectOfxAccountBank(SOCIETE_GENERALE)
      .setMainAccountForAll()
      .checkFileContent(new Object[][]{
        {"21/06/2008", "V'lib", "1.00"},
      })
      .doImport();
    importDialog
      .checkFileContent(new Object[][]{
        {"10/06/2008", "McDo", "10.00"},
      })
      .selectOfxAccountBank(SOCIETE_GENERALE)
      .setMainAccountForAll()
      .doImport();
    importDialog
      .selectOfxAccountBank(SOCIETE_GENERALE)
      .checkFileContent(new Object[][]{
        {"10/06/2008", "Metro", "71.00"},
      });
    importDialog.openCardTypeChooser()
      .selectDeferredCard("Card n. 1111-2222-3333-4444")
      .validate();

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
      .add("10/06/2008", TransactionType.CREDIT_CARD, "Metro", "", 71.00)
      .add("10/06/2008", TransactionType.VIREMENT, "McDo", "", 10.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00)
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
      .load();

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
      .checkErrorMessage("import.dateformat.undefined")
      .selectDate("Day/Month/Year")
      .checkFileContent(new Object[][]{
        {"02/01/2001", "Menu K", "-1.10"}
      })
      .selectDate("Month/Day/Year")
      .checkFileContent(new Object[][]{
        {"01/02/2001", "Menu K", "-1.10"}
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
                           "Dsdfsdf sdfsf\n" +
                           "^sdfsf");

    ImportDialogChecker importDialog = operations.openImportDialog()
      .selectFiles(path)
      .acceptFile()
      .checkHtmlErrorMessage("import.file.error");

    importDialog
      .clickErrorMessage()
      .checkTitle("Invalid file")
      .checkMessageContains("The content of this file is invalid")
      .checkDetailsContain("InvalidData")
      .close();

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
      .checkHtmlErrorMessage("import.file.error")
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
    operations.importOfxFile(fileName1, "Autre");

    String fileName2 = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .save();
    operations.importOfxFile(fileName2, "Autre");
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

    operations.importQifFile(file2, SOCIETE_GENERALE, "other");

    views.selectData();
    timeline.checkSelection("2006/01");
    transactions
      .initContent()
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .check();
  }

  public void testImportDialogGivesAccessToBankSites() throws Exception {

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
      .checkBankAccessHidden()
      .selectBank("Autre")
      .checkWebsiteUrl("http://support.mybudgetview.fr/entries/20173256")
      .checkHelpAvailable(true)
      .openHelp()
      .checkTitle("Importing data")
      .close();

    bankDownload
      .setFilter("BNP")
      .checkBankList("BNP Paribas", "BNPPF", "Autre")
      .selectBank("BNP Paribas")
      .checkWebsiteUrl("http://www.bnpparibas.net")
      .checkHelpAvailable(true)
      .openHelp()
      .checkTitle("BNP Paribas downloads")
      .close();

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
      .checkNoErrorMessage()
      .defineAccount("CIC", "main", "1111")
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

  public void testImportSameTransactionWitSplited() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/8", 2.0, "V'lib")
      .addTransaction("2008/06/9", 1.0, "V'lib")
      .addTransaction("2008/06/10", 3.0, "V'lib")
      .addTransaction("2008/06/10", 2.0, "other 1")
      .addTransaction("2008/06/10", 2.0, "V'lib")
      .addTransaction("2008/06/10", 4.0, "V lib")
      .load();

//    openApplication();
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


}
