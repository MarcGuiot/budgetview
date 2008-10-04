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
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;

public class ImportTest extends LoggedInFunctionalTestCase {

  private static final String SOCIETE_GENERALE = "Société Générale";

  private ImportChecker openImportDialog() {
    Window window = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    return new ImportChecker(window);
  }

  public void testStandardImport() throws Exception {

    final String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    openImportDialog()
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
      .doImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testCloseButtonLabelBeforeImport() throws Exception {
    openImportDialog()
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

    openImportDialog()
      .setFilePath(path1 + ";" + path2)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .doImport()
      .checkFileContent(new Object[][]{
        {"20/02/2006", "Menu K", "-2.20"}
      })
      .doImport();

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

    openImportDialog()
      .browseAndSelect(path)
      .acceptFile()
      .doImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testImportQifFileWithNoExistingAccount() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .selectAccountBank(SOCIETE_GENERALE)
      .checkAccountName("Main account")
      .setAccountName("My SG account")
      .doImport()
      .checkImportMessage("You must enter the account number")
      .setAccountNumber("0123546")
      .doImport();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void ENCOURS_testImportTwoQifFilesInTwoDifferentAccounts() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "first")
      .save();

    openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .doImport();

    String secondQif = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "second")
      .save();

    openImportDialog()
      .setFilePath(secondQif)
      .selectAccount("Main account");
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

    openImportDialog()
      .setFilePath(path1 + ";" + path2)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "First operation", "-1.10"}
      })
      .doImport()
      .checkImportMessage("You must select the account bank")
      .skipFile()
      .checkImportMessage("")
      .checkFileContent(new Object[][]{
        {"20/01/2006", "Second operation", "-2.20"}
      })
      .selectAccountBank(SOCIETE_GENERALE)
      .setAccountNumber("1111")
      .doImport();

    transactions
      .initContent()
      .add("20/01/2006", TransactionType.PRELEVEMENT, "Second operation", "", -2.2)
      .check();
  }

  public void testImportQifFileWithExistingAccount() throws Exception {
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();

    openImportDialog()
      .setFilePath(firstQif)
      .acceptFile()
      .doImport();

    String qifFile = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    openImportDialog()
      .setFilePath(qifFile)
      .acceptFile()
      .checkAvailableAccounts("Main account")
      .selectAccount("Main account");
  }

  public void testImportWithCreateAccountChecksAccountBankIsFilled() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    openImportDialog()
      .setFilePath(path1)
      .acceptFile()
      .checkFileContent(new Object[][]{
        {"10/01/2006", "Menu K", "-1.10"}
      })
      .checkNoAccountBankSelected()
      .setAccountNumber("0123546")
      .doImport()
      .checkImportMessage("You must select the account bank")
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

    openImportDialog()
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
      .doImport();

    String secondFileName = OfxBuilder.init(this)
      .addBankAccount(666, 2048, "77777777", 77.0, "2008/06/11")
      .addTransaction("2008/06/14", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .save();

    openImportDialog()
      .setFilePath(secondFileName)
      .acceptFile()
      .doImport();

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

    openImportDialog()
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
      .doImport();

    transactions.initContent()
      .add("01/02/2001", TransactionType.PRELEVEMENT, "Menu K", "", -1.10)
      .check();
  }

  public void testImportQifBadFile() throws Exception {
    String path = TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(path,
                           "Dsdfsdf sdfsf\n" +
                           "^sdfsf");

    openImportDialog()
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

    openImportDialog()
      .selectFiles(path)
      .acceptFile()
      .checkErrorMessage("import.file.error", new File(path).getAbsolutePath())
      .close();
  }
}
