package com.budgetview.functests.importexport;

import com.budgetview.functests.checkers.CategorizationChecker;
import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.ImportDialogPreviewChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.BankEntity;
import com.budgetview.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;

public class OfxImportTest extends LoggedInFunctionalTestCase {

  @Test
  public void testImportingTheSameFileTwiceDoesNotDuplicateTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .load(2, 0, 0);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .load(0, 2, 0);

    views.selectData();
    transactions
      .initAmountContent()
      .add("11/01/2006", "TX 2", -2.20, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/01/2006", "TX 1", -1.10, "To categorize", 2.20, 2.20, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  @Test
  public void testImportingASecondFileWithNewerTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "TX 1")
      .addTransaction("2006/01/12", -2.2, "TX 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -3.3, "TX 3")
      .addTransaction("2006/01/14", -4.4, "TX 4")
      .load(2, 0, 0);

    views.selectData();
    transactions
      .initAmountContent()
      .add("14/01/2006", "TX 4", -4.40, "To categorize", -7.70, -7.70, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("13/01/2006", "TX 3", -3.30, "To categorize", -3.30, -3.30, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("12/01/2006", "TX 2", -2.20, "To categorize", 0.0, 0.0, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/01/2006", "TX 1", -1.10, "To categorize", 2.20, 2.20, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  @Test
  public void testImportingASecondFileWithOlderTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2005/12/25", -10.0, "Tx 0")
      .load(1, 0, 0);

    timeline.selectMonths("2005/12", "2006/01");
    views.selectData();
    transactions
      .initAmountContent()
      .add("11/01/2006", "TX 2", -2.20, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/01/2006", "TX 1", -1.10, "To categorize", 2.20, 2.20, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("25/12/2005", "TX 0", -10.00, "To categorize", 3.30, 3.30, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  @Test
  public void testImportingOverlappingFilesDoesNotDuplicateTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .load(2, 0, 0);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .addTransaction("2006/01/12", -3.3, "TX 3")
      .load(1, 1, 0);

    views.selectData();
    transactions
      .initAmountContent()
      .add("12/01/2006", "TX 3", -3.30, "To categorize", -3.3, -3.3, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/01/2006", "TX 2", -2.20, "To categorize", 0., 0., OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/01/2006", "TX 1", -1.10, "To categorize", 2.20, 2.20, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  @Test
  public void testImportingFilesWithDuplicatesBeforeAndAfter() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -3.3, "TX 3")
      .addTransaction("2006/01/14", -4.4, "TX 4")
      .addTransaction("2006/01/15", -5.5, "TX 5")
      .load(3, 0, 0);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/16", -6.6, "TX 6")
      .addTransaction("2006/01/15", -5.5, "TX 5")
      .addTransaction("2006/01/13", -3.3, "TX 3")
      .addTransaction("2006/01/12", -2.2, "TX 2")
      .load(2, 2, 0);

    views.selectData();
    transactions
      .initAmountContent()
      .add("16/01/2006", "TX 6", -6.60, "To categorize", -6.60, -6.60, "Account n. 00001123")
      .add("15/01/2006", "TX 5", -5.50, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("14/01/2006", "TX 4", -4.40, "To categorize", 5.50, 5.50, "Account n. 00001123")
      .add("13/01/2006", "TX 3", -3.30, "To categorize", 9.90, 9.90, "Account n. 00001123")
      .add("12/01/2006", "TX 2", -2.20, "To categorize", 13.20, 13.20, "Account n. 00001123")
      .check();
  }

  @Test
  public void testTakesUserAndBankDatesIntoAccountWhenDetectingDuplicates() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", "2006/01/10", -1.1, "Operation 1")
      .load(1, 0, 0);
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", "2006/01/10", -1.1, "Operation 1")
      .load(0, 1, 0);

    views.selectData();
    transactions
      .initContent()
      .add("15/01/2006", "10/01/2006", TransactionType.PRELEVEMENT, "Operation 1", "", -1.1)
      .check();
  }

  @Test
  public void testImportingTheSameFileTwiceOnSplittedDoesNotDuplicateTransactions() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load(2, 0, 0);

    views.selectCategorization();
    categorization.selectTransactions("Tx 2");
    transactionDetails.split("-1", "info");
    categorization.selectVariable().selectNewSeries("Series 1");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load(0, 2, 0);

    views.selectData();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.0, "Series 1")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1)
      .check();
  }

  @Test
  public void testImportingTheSameSplittedFileOnDifferentSplitKeepTheActualSplit() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -4.2, "Tx 2")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("Tx 2");
    transactionDetails.split("-1.5", "info");
    categorization.selectVariable().selectNewSeries("Series 1");

    categorization.selectTableRow(categorization.getTable()
                                    .getRowIndex(CategorizationChecker.AMOUNT_COLUMN_INDEX, (-4.2 + 1.5) + "0"));
    transactionDetails.split("-1.5", "info2");
    categorization.selectVariable().selectNewSeries("Series 2");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -4.2, "Tx 2")
      .load(0, 2, 0);

    views.selectData();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info2", -1.5, "Series 2")
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.5, "Series 1")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1)
      .check();
    transactions.initAmountContent()
      .add("11/01/2006", "TX 2", -1.20, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("11/01/2006", "TX 2", -1.50, "Series 2", 1.20, 1.20, "Account n. 00001123")
      .add("11/01/2006", "TX 2", -1.50, "Series 1", 2.70, 2.70, "Account n. 00001123")
      .add("10/01/2006", "TX 1", -1.10, "To categorize", 4.20, 4.20, "Account n. 00001123")
      .check();
  }

  private static final String TEXT =
    "OFXHEADER:100\n" +
    "DATA:OFXSGML\n" +
    "VERSION:102\n" +
    "SECURITY:NONE\n" +
    "ENCODING:USASCII\n" +
    "CHARSET:1252\n" +
    "COMPRESSION:NONE\n" +
    "OLDFILEUID:NONE\n" +
    "NEWFILEUID:NONE\n" +
    "<OFX>\n" +
    "  <SIGNONMSGSRSV1>\n" +
    "    <SONRS>\n" +
    "      <STATUS>\n" +
    "        <CODE>0\n" +
    "        <SEVERITY>INFO\n" +
    "      </STATUS>\n" +
    "      <DTSERVER>20060716000000\n" +
    "      <LANGUAGE>FRA\n" +
    "    </SONRS>\n" +
    "  </SIGNONMSGSRSV1>\n" +
    "  <BANKMSGSRSV1>\n" +
    "    <STMTTRNRS>\n" +
    "      <TRNUID>20060716000000\n" +
    "      <STATUS>\n" +
    "        <CODE>0\n" +
    "        <SEVERITY>INFO\n" +
    "      </STATUS>\n" +
    "      <STMTRS>\n" +
    "        <CURDEF>EUR\n" +
    "        <BANKACCTFROM>\n" +
    "          <BANKID>30066\n" +
    "          <BRANCHID>10674\n" +
    "          <ACCTID>00010063701\n" +
    "          <ACCTTYPE>CHECKING\n" +
    "        </BANKACCTFROM>\n" +
    "        <BANKTRANLIST>\n" +
    "          <DTSTART>20060131000000\n" +
    "          <DTEND>20060203000000\n" +
    "          <STMTTRN>\n" +
    "            <TRNTYPE>DEBIT\n" +
    "            <DTPOSTED>20060131\n" +
    "            <DTUSER>20060131\n" +
    "            <TRNAMT>-21.53\n" +
    "            <FITID>LOIB4G3LLF\n" +
    "            <NAME>DROITS DE GARDE 1 SEM. 2006 3006\n" +
    "          </STMTTRN>\n" +
    "        </BANKTRANLIST>\n" +
    "        <LEDGERBAL>\n" +
    "          <BALAMT>-683.25\n" +
    "          <DTASOF>20060704000000\n" +
    "        </LEDGERBAL>\n" +
    "        <AVAILBAL>\n" +
    "          <BALAMT>0.0\n" +
    "          <DTASOF>20060704000000\n" +
    "        </AVAILBAL>\n" +
    "      </CCSTMTRS>\n" +
    "    </CCSTMTTRNRS>\n" +
    "  </CREDITCARDMSGSRSV1>\n" +
    "</OFX>\n";

  @Test
  public void testDateFormat() throws Exception {
    String text = OfxImportTest.TEXT;
    String fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);

    transactions
      .initAmountContent()
      .add("31/01/2006", "DROITS DE GARDE 1 SEM. 2006 3006", -21.53, "To categorize", -683.25, -683.25, "Account n. 00010063701")
      .check();

    text = OfxImportTest.TEXT.replace("<DTASOF>20060704000000", "<DTASOF>2006/07/04");
    fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);

    text = OfxImportTest.TEXT.replace("<DTASOF>20060704000000", "<DTASOF>04/07/2006");
    fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);

    text = OfxImportTest.TEXT.replace("<DTASOF>20060704000000", "<DTASOF>20120510100000.000");
    fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);

    text = OfxImportTest.TEXT.replace("<DTASOF>20060704000000", "<DTASOF>20120510100000.000 EST");
    fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);
  }

  @Test
  public void testInlineOfx() throws Exception {
    int i = TEXT.indexOf("<OFX>");
    String text = TEXT.substring(0, i) + TEXT.substring(i).replaceAll("\n", "");
    String fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);

    int i1 = text.indexOf("DROITS DE GARDE") + 5;
    text = text.substring(0, i1) + "\n" + text.substring(i1);
    fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, text);
    operations.importOfxFile(fileName);
  }

  @Test
  public void testTruncatedFile() throws Exception {
    checkInvalidFileImport("<OFX>\n" +
                           "<SIGNONMSGSRSV1>\n" +
                           "  <SONRS>\n" +
                           "    <STATUS>\n" +
                           "      <CODE>0\n" +
                           "      <SEVERITY>INFO\n" +
                           "    </STATUS>\n" +
                           "    <DTSERVER>20060716000000\n" +
                           "    <LANGUAGE>FRA\n" +
                           "  </SONRS>\n" +
                           "</SIGNONMSGSRSV1>\n" +
                           "<BANKMSGSRSV1>\n" +
                           "  <STMTTRNRS>\n" +
                           "    <TRNUID>20060716000000\n" +
                           "    <STATUS>\n" +
                           "      <CODE>0\n" +
                           "      <SEVERITY>INFO\n" +
                           "    </STATUS>\n" +
                           "    <STMTRS>\n" +
                           "      <CURDEF>EUR\n" +
                           "      <BANKACCTFROM>\n" +
                           "        <BANKID>30066\n" +
                           "        <BRANCHID>10674\n" +
                           "        <ACCTID>123123123123\n" +
                           "        <ACCTTYPE>CHECKING\n" +
                           "      </BANKACCTFROM>\n" +
                           "      <BANKTRANLIST>\n" +
                           "        <DTSTART>20060517000000\n" +
                           "        <DTEND>20060713000000\n" +
                           "        <STMTTRN>\n" +
                           "          <TRNTYPE>DEBIT\n" +
                           "          <DTPOSTED>20060117\n" +
                           "          <DTUSER>20060117\n" +
                           "          <TRNAMT>-63.00\n" +
                           "          <FITID>LLDTHJTOCF\n" +
                           "          <NAME>CHEQUE 0366943\n" +
                           "        </STMTTRN>\n" +
                           "        <STMTTRN>");
  }

  @Test
  public void testInvalidContent() throws Exception {
    checkInvalidFileImport("Hello world!");
  }

  private void checkInvalidFileImport(String fileContent) {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -45.0, "Dr Lecter")
      .load();

    views.selectData();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -45.0)
      .check();

    final String fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, fileContent);
    WindowInterceptor
      .init(operations.getImportTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("fileField").setText(fileName);
          window.getButton("Import").click();
          assertTrue(window.getTextBox("errorMessage").textContains("Invalid file content"));
          return window.getButton("Close").triggerClick();
        }
      })
      .run();

    views.selectData();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -45.0)
      .check();
  }

  @Test
  public void testInvalidFileExtension() throws Exception {
    final File file = File.createTempFile("pref", "suf");
    file.deleteOnExit();
    WindowInterceptor
      .init(operations.getImportTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("fileField").setText(file.getAbsolutePath());
          window.getButton("Import").click();
          assertTrue(window.getTextBox("errorMessage").textContains("only OFX, QIF and CSV or TSV files are supported"));
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
  }

  @Test
  public void testImportWithUnknownBank() throws Exception {
    String path = OfxBuilder.init(this)
      .addBankAccount(12345, 54321, "111", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .save();
    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(path)
      .importFileAndPreview();
//      .checkMessageSelectABank();
    preview
//      .openEntityEditor()
      .selectBank("Other");
//      .validate();
    preview
      .setMainAccount()
      .checkNoErrorMessage()
      .importAccountAndComplete();

    views.selectHome();
    mainAccounts.checkAccount("Account n. 111", 1000.00, "2008/08/10");
    mainAccounts.edit("Account n. 111")
      .checkSelectedBank("Other")
      .validate();

    String secondAccountOnSameBank = OfxBuilder.init(this)
      .addBankAccount(12345, 54321, "111.222", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "other")
      .save();

    operations.openImportDialog()
      .setFilePath(secondAccountOnSameBank)
      .importFileAndPreview()
      .setMainAccount()
      .importAccountAndComplete();

    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "OTHER", "", -50.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "VIREMENT", "", -50.00)
      .check();
  }

  @Test
  public void testImportOfxWithDateInThePast() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .addTransaction("2008/08/06", -30.00, "Virement")
      .load();

    views.selectHome();
    mainAccounts.checkAccount("Account n. 111", 1000.00, "2008/08/10");
  }

  @Test
  public void testLettersInBankId() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("unknown", 111, "111", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .addTransaction("2008/08/06", -30.00, "Virement")
      .loadUnknown("Other");

    views.selectHome();
    mainAccounts.checkAccount("Account n. 111", 1000.00, "2008/08/10");
    mainAccounts.edit("Account n. 111")
      .checkSelectedBank("Other")
      .checkIsMain()
      .cancel();
  }

  @Test
  public void testAccountTypeSelection() throws Exception {

    operations.hideSignposts();

    String file = OfxBuilder.init(this)
      .addBankAccount(SOCIETE_GENERALE, 111, "111", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .addBankAccount(SOCIETE_GENERALE, 222, "222", 1000.00, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Misc")
      .save();

    views.selectHome();
    ImportDialogPreviewChecker preview = importPanel.openImport()
      .selectFiles(file)
      .importFileAndPreview();

    preview
      .checkAstericsErrorOnBank()
      .selectBank("Other")
      .checkNoAccountTypeMessageDisplayed()
      .checkAstericsErrorOnType()
      .importAccountAndOpenNext()
      .checkAccountTypeWarningDisplayed("Account n. 222")
      .setSavingsAccount()
      .checkNoErrorMessage()
      .checkAstericsClearOnType()
      .importAccountAndOpenNext();

    preview
      .selectBank("Other")
      .setMainAccount()
      .checkNoAccountTypeMessageDisplayed()
      .importAccountAndComplete();

    views.selectHome();
    savingsAccounts.edit("Account n. 111")
      .checkAccountNumber("111")
      .cancel();
    mainAccounts.edit("Account n. 222")
      .checkAccountNumber("222")
      .cancel();
  }

  @Test
  public void testImportDeferredCardAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    String file = OfxBuilder.init(this)
      .addBankAccount("unknown", 111, "111", 1000.00, "2008/08/07")
      .addTransaction("2008/08/07", 50.00, "something")
      .addCardAccount("1234", 10, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .save();

    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(file)
      .importFileAndPreview();

    preview
      .checkTransactions(new Object[][]{
        {"2008/08/07", "something", "50.00"}
      })
      .selectBank("Other")
      .setMainAccount()
      .importAccountAndOpenNext();

    preview
      .checkTransactions(new Object[][]{
        {"2008/08/10", "Virement", "-50.00"}
      })
      .setDeferredAccount(25, 28, 0)
      .selectBank("Other")
      .importAccountAndComplete();

    views.selectHome();
    mainAccounts.edit("Card n. 1234")
      .checkIsDeferredCard()
      .cancel();
  }

  @Test
  public void testImportCreditCardAccount() throws Exception {
    String file = OfxBuilder.init(this)
      .addCardAccount("1234", 10, "2008/08/07")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .save();
    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(file)
      .importFileAndPreview();
    preview
      .selectBank("Other")
      .setAsCreditCard()
      .importAccountAndComplete();

    views.selectHome();
    mainAccounts.edit("Card n. 1234")
      .checkIsCreditCard()
      .cancel();

    views.selectData();
    transactions
      .initContent()
      .add("10/08/2008", TransactionType.CREDIT_CARD, "VIREMENT", "", -50.00)
      .check();
    mainAccounts.checkEndOfMonthPosition("Card n. 1234", 10.00);
  }

  @Test
  public void testIfAnAccountAlreadyExistWeAskToAssociateToIt() throws Exception {
    accounts.createMainAccount("First account", "4321", 100);

    String ofxFile = OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .addTransaction("2008/08/06", -30.00, "Virement")
      .save();

    operations.openImportDialog()
      .setFilePath(ofxFile)
      .importFileAndPreview()
      .selectAccount("First account")
      .importAccountAndComplete();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 950.00, "2008/08/12")
      .addTransaction("2008/08/11", -50.00, "Virement")
      .load();

    views.selectHome();
    mainAccounts.checkAccountOrder("First account");

    views.selectData();
    transactions.initContent()
      .add("11/08/2008", TransactionType.PRELEVEMENT, "VIREMENT", "", -50.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "VIREMENT", "", -50.00)
      .add("06/08/2008", TransactionType.PRELEVEMENT, "VIREMENT", "", -30.00)
      .check();
  }

  @Test
  public void testCreateTwoAccountsInOfxAndCheckOnlyOneIsAvailable() throws Exception {
    accounts.createMainAccount("First account", "4321", 100);
    String ofxFile = OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "Virement 111")
      .addTransaction("2008/08/06", -30.00, "Virement 111")
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 222, "222", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "Virement 222")
      .addTransaction("2008/08/06", -30.00, "Virement 222")
      .save();

    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(ofxFile)
      .importFileAndPreview();

    preview
      .checkAvailableAccounts("First account")
      .selectAccount("First account")
      .importAccountAndOpenNext()
      .selectBank("Other")
      .setMainAccount()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Account n. 222", "First account");
  }

  @Test
  public void testCreateTwoAccountsEndSkipFirst() throws Exception {
    String ofxFile = OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "Virement 111")
      .addTransaction("2008/08/06", -30.00, "Virement 111")
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 222, "222", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "Virement 222")
      .addTransaction("2008/08/06", -30.00, "Virement 222")
      .save();

    operations.openImportDialog()
      .setFilePath(ofxFile)
      .importFileAndPreview()
      .selectSkipFile()
      .checkSkippedFileMessage()
      .importAccountAndOpenNext()
      .setMainAccount()
      .selectBank("Other")
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Account n. 222");
  }

  @Test
  public void testAutomaticallySelectsAccountWhenSeveralAccountsHaveTheSameNumber() throws Exception {

    // Cas de l'import de fichiers LCL où les comptes courant et carte ont le même numéro de compte

    accounts.createMainAccount("Account A", "4321", 100.00);
    accounts.createMainAccount("Account B", "4321", 100.00);

    OfxBuilder
      .init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/14")
      .addTransaction("2006/01/11", -1.1, "Operation AAA 1")
      .addTransaction("2006/01/12", -2.2, "Operation AAA 2")
      .addTransaction("2006/01/13", -3.3, "Operation AAA 3")
      .addTransaction("2006/01/14", -4.4, "Operation AAA 4")
      .loadInAccount("Account A");

    OfxBuilder
      .init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 2000.00, "2008/08/14")
      .addTransaction("2006/01/01", -1.1, "Operation BBB 1")
      .addTransaction("2006/01/02", -2.2, "Operation BBB 2")
      .addTransaction("2006/01/03", -3.3, "Operation BBB 3")
      .addTransaction("2006/01/04", -4.4, "Operation BBB 4")
      .loadInAccount("Account B");

    String pathA = OfxBuilder
      .init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 990.00, "2008/08/20")
      .addTransaction("2006/01/15", -10.00, "Operation AAA 5")
      .save();
    operations.openImportDialog()
      .selectFiles(pathA)
      .importFileAndPreview()
      .checkSelectedAccount("Account A")
      .importAccountAndComplete();

    String pathB = OfxBuilder
      .init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1990.00, "2008/08/20")
      .addTransaction("2006/01/20", -10.00, "Operation BBB 5")
      .save();
    operations.openImportDialog()
      .selectFiles(pathB)
      .importFileAndPreview()
      .checkSelectedAccount("Account B")
      .importAccountAndComplete();

    views.selectData();
    transactions.initAmountContent()
      .add("20/01/2006", "OPERATION BBB 5", -10.00, "To categorize", 79.00, 158.00, "Account B")
      .add("15/01/2006", "OPERATION AAA 5", -10.00, "To categorize", 79.00, 168.00, "Account A")
      .add("14/01/2006", "OPERATION AAA 4", -4.40, "To categorize", 89.00, 178.00, "Account A")
      .add("13/01/2006", "OPERATION AAA 3", -3.30, "To categorize", 93.40, 182.40, "Account A")
      .add("12/01/2006", "OPERATION AAA 2", -2.20, "To categorize", 96.70, 185.70, "Account A")
      .add("11/01/2006", "OPERATION AAA 1", -1.10, "To categorize", 98.90, 187.90, "Account A")
      .add("04/01/2006", "OPERATION BBB 4", -4.40, "To categorize", 89.00, 189.00, "Account B")
      .add("03/01/2006", "OPERATION BBB 3", -3.30, "To categorize", 93.40, 193.40, "Account B")
      .add("02/01/2006", "OPERATION BBB 2", -2.20, "To categorize", 96.70, 196.70, "Account B")
      .add("01/01/2006", "OPERATION BBB 1", -1.10, "To categorize", 98.90, 198.90, "Account B")
      .check();
  }

  @Test
  public void testImportWithoutAccountAndDoubleMinus() throws Exception {
    final String TEXT =
      "OFXHEADER:100\n" +
      "DATA:OFXSGML\n" +
      "VERSION:102\n" +
      "SECURITY:NONE\n" +
      "ENCODING:USASCII\n" +
      "CHARSET:1252\n" +
      "COMPRESSION:NONE\n" +
      "OLDFILEUID:NONE\n" +
      "NEWFILEUID:NONE\n" +
      "<OFX>\n" +
      "  <SIGNONMSGSRSV1>\n" +
      "    <SONRS>\n" +
      "      <STATUS>\n" +
      "        <CODE>0\n" +
      "        <SEVERITY>INFO\n" +
      "      </STATUS>\n" +
      "      <DTSERVER>20060716000000\n" +
      "      <LANGUAGE>FRA\n" +
      "    </SONRS>\n" +
      "  </SIGNONMSGSRSV1>\n" +
      "  <BANKMSGSRSV1>\n" +
      "    <STMTTRNRS>\n" +
      "      <TRNUID>20060716000000\n" +
      "      <STATUS>\n" +
      "        <CODE>0\n" +
      "        <SEVERITY>INFO\n" +
      "      </STATUS>\n" +
      "      <STMTRS>\n" +
      "        <CURDEF>EUR\n" +
      "        <BANKTRANLIST>\n" +
      "          <DTSTART>20060131000000\n" +
      "          <DTEND>20060203000000\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20060131\n" +
      "            <DTUSER>20060131\n" +
      "            <TRNAMT>-21.53\n" +
      "            <FITID>LOIB4G3LLF\n" +
      "            <NAME>DROITS DE GARDE 1 SEM. 2006 3006\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>--683.25\n" +
      "          <DTASOF>20060704000000\n" +
      "        </LEDGERBAL>\n" +
      "        <AVAILBAL>\n" +
      "          <BALAMT>0.0\n" +
      "          <DTASOF>20060704000000\n" +
      "        </AVAILBAL>\n" +
      "      </CCSTMTRS>\n" +
      "    </CCSTMTTRNRS>\n" +
      "  </CREDITCARDMSGSRSV1>\n" +
      "</OFX>\n";
    File file = File.createTempFile("import", ".ofx");
    file.deleteOnExit();
    Files.dumpStringToFile(file, TEXT);
    ImportDialogChecker importDialogChecker = operations.openImportDialog();
    importDialogChecker.setFilePath(file.getAbsolutePath())
      .importFileAndPreview()
      .setMainAccount()
      .selectBank("CIC")
      .setAccountName("main account")
      .importAccountAndComplete();

    transactions.initContent()
      .add("31/01/2006", TransactionType.CREDIT_CARD, "DROITS DE GARDE 1 SEM. 2006 3006", "", -21.53)
      .check();
    mainAccounts.checkPosition("main account", -683.25);


  }
}
