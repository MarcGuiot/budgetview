package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.File;

public class ImportTest extends LoggedInFunctionalTestCase {

  private static final String SOCIETE_GENERALE = "Société Générale";

  public void testStandardImport() throws Exception {

    final String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.openImportDialog()
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
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
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
      .checkCloseButton(Lang.get("import.step1.close"));
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
      .doImport()
      .checkFileContent(new Object[][]{
        {"20/02/2006", "Menu K", "-2.20"}
      })
      .completeImport();

    timeline.checkSelection("2006/02");

    timeline.selectAll();
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

    operations.importQifFile(path, SOCIETE_GENERALE, 0.);

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

    operations.openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .selectAccountBank(SOCIETE_GENERALE)
      .checkAccountName("Main account")
      .setAccountName("My SG account")
      .setAccountNumber("0123546")
      .doImport()
      .completeImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testSettingInitialBalanceForQifFiles() throws Exception {
    final String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    ImportChecker importDialog = operations.openImportDialog()
      .setFilePath(path)
      .acceptFile()
      .selectAccountBank(SOCIETE_GENERALE)
      .setAccountName("Main")
      .setAccountNumber("12345");

    importDialog
      .doImportWithBalance()
      .checkAccountLabel("Account: Main")
      .checkCancelNotAvailable()
      .checkEscNotAvailable()
      .checkInitialAmountSelected("0.0")
      .checkInitialMessageDisplayed()
      .setAmount(12.33)
      .checkDialogClosed();

    importDialog.checkClosed();

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
      .completeImport();

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
      .checkImportMessage("You must select a bank for this account")
      .skipFile()
      .checkImportMessage("")
      .checkFileContent(new Object[][]{
        {"20/01/2006", "Second operation", "-2.20"}
      })
      .selectAccountBank(SOCIETE_GENERALE)
      .setAccountNumber("1111")
      .completeImport();

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
      .completeImport();

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

    operations.openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .checkNoAccountBankSelected()
      .setAccountNumber("0123546")
      .doImport()
      .checkImportMessage("You must select a bank for this account")
      .skipFile();
  }

  public void testOfxWithUnknownBankEntities() throws Exception {
    String fileName = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/21", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .addBankAccount(777, 1027, "87654321", 21.0, "2008/06/21")
      .addTransaction("2008/06/10", 10.0, "McDo")
      .addCardAccount("1111222233334444", 7.5, "2008/06/21")
      .addTransaction("2008/06/10", 71.0, "Metro")
      .save();

    operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"21/06/2008", "V'lib", "1.00"},
        {"10/06/2008", "Metro", "71.00"},
        {"10/06/2008", "McDo", "10.00"},
        {"10/06/2008", "V'lib", "1.00"},
      })
      .checkAccountsForEntity("666", new String[]{"12345678a", "12345678b"})
      .checkAccountsForEntity("777", new String[]{"1111222233334444", "87654321"})
      .selectBankForEntity("777", SOCIETE_GENERALE)
      .completeImport();

    String secondFileName = OfxBuilder.init(this)
      .addBankAccount(666, 2048, "77777777", 77.0, "2008/06/11")
      .addTransaction("2008/06/14", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .save();

    operations.openImportDialog()
      .setFilePath(secondFileName)
      .acceptFile()
      .completeImport();

    transactions
      .initContent()
      .addOccasional("21/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .addOccasional("14/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "Metro", "", 71.00)
      .add("10/06/2008", TransactionType.VIREMENT, "McDo", "", 10.00)
      .addOccasional("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .check();
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
      .selectBank(SOCIETE_GENERALE)
      .checkDates("Year/Month/Day", "Month/Day/Year", "Day/Month/Year")
      .doImport()
      .checkErrorMessage("import.dateformat.undefined")
      .selectDate("Month/Day/Year")
      .checkFileContent(new Object[][]{
        {"01/02/2001", "Menu K", "-1.10"}
      })
      .enterAccountNumber("0123546")
      .completeImport();

    transactions.initContent()
      .add("01/02/2001", TransactionType.PRELEVEMENT, "Menu K", "", -1.10)
      .check();
  }

  public void testImportQifBadFile() throws Exception {
    String path = TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(path,
                           "Dsdfsdf sdfsf\n" +
                           "^sdfsf");

    operations.openImportDialog()
      .selectFiles(path)
      .acceptFile()
      .checkErrorMessage("import.file.error", new File(path).getAbsolutePath())
      .close();
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
      .checkErrorMessage("import.file.error", new File(path).getAbsolutePath())
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
    operations.importQifFile(file1, SOCIETE_GENERALE, 100.);

    views.selectData();

    String file2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/11", -4, "Menu K")
      .addTransaction("2006/01/12", -5, "Menu K")
      .addTransaction("2006/01/12", -6, "Menu K")
      .save();
    operations.importQifFile(file2, SOCIETE_GENERALE);

    transactions
      .getTable()
      .getHeader().click(1);

    transactions
      .initAmountContent()
      .add("Menu K", -6.00, 85.00, 85.00)
      .add("Menu K", -5.00, 91.00, 91.00)
      .add("Menu K", -4.00, 96.00, 96.00)
      .add("Menu K", -3.00, 100.00, 100.00)
      .add("Menu K", -2.00, 103.00, 103.00)
      .add("Menu K", -1.00, 105.00, 105.00)
      .check();
  }


  public void testImportFromDiferentAccountWithSameTransactionsInOfx() throws Exception {
    String fileName1 = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .save();

    operations.importOfxFile(fileName1);

    String fileName2 = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib")
      .save();
    operations.importOfxFile(fileName2);
    views.selectData();
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

    operations.importQifFile(file1, SOCIETE_GENERALE, 100.);

    mainAccounts.createNewAccount()
      .setAccountName("other")
      .setBalance(100)
      .setAccountNumber("1213")
      .selectBank(SOCIETE_GENERALE)
      .validate();

    String file2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/09", -1, "Menu K")
      .save();

    operations.importQifFile(file2, SOCIETE_GENERALE, "other");

    views.selectData();
    transactions
      .initContent()
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .add("09/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.00)
      .check();

  }
}
