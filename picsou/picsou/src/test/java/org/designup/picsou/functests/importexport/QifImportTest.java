package org.designup.picsou.functests.importexport;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

public class QifImportTest extends LoggedInFunctionalTestCase {

  public void testImportsQifFilesFromSG() throws Exception {
    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(QifImportTest.class.getResourceAsStream("/testfiles/sg1.qif"), fileName);

    operations.importQifFile(fileName, SOCIETE_GENERALE, 0.00);

    views.selectData();
    transactions
      .initContent()
      .add("22/04/2006", TransactionType.CREDIT_CARD, "REL. SACLAY", "", -55.49)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70)
      .check();

    transactions.initAmountContent()
      .add("22/04/2006", "REL. SACLAY", -55.49, "To categorize", 0.00, 0.00, "Main account")
      .add("20/04/2006", "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", -17.65, "To categorize", 55.49, 55.49, "Main account")
      .add("20/04/2006", "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", -49.00, "To categorize", 73.14, 73.14, "Main account")
      .add("19/04/2006", "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", -14.50, "To categorize", 122.14, 122.14, "Main account")
      .add("13/04/2006", "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", -18.70, "To categorize", 136.64, 136.64, "Main account")
      .check();

    views.selectHome();
    mainAccounts.changePosition("Main account", 100, "REL. SACLAY");

    views.selectData();
    transactions.initAmountContent()
      .add("22/04/2006", "REL. SACLAY", -55.49, "To categorize", 100.00, 100.00, "Main account")
      .add("20/04/2006", "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", -17.65, "To categorize", 155.49, 155.49, "Main account")
      .add("20/04/2006", "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", -49.00, "To categorize", 173.14, 173.14, "Main account")
      .add("19/04/2006", "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", -14.50, "To categorize", 222.14, 222.14, "Main account")
      .add("13/04/2006", "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", -18.70, "To categorize", 236.64, 236.64, "Main account")
      .check();

    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D30/04/2006\n" +
                    "T-20.00\n" +
                    "N\n" +
                    "MAuchan\n" +
                    "^");
    operations.importQifFile(file, SOCIETE_GENERALE);

    views.selectData();
    transactions.initAmountContent()
      .add("30/04/2006", "AUCHAN", -20.00, "To categorize", 80.00, 80.00, "Main account")
      .add("22/04/2006", "REL. SACLAY", -55.49, "To categorize", 100.00, 100.00, "Main account")
      .add("20/04/2006", "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", -17.65, "To categorize", 155.49, 155.49, "Main account")
      .add("20/04/2006", "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", -49.00, "To categorize", 173.14, 173.14, "Main account")
      .add("19/04/2006", "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", -14.50, "To categorize", 222.14, 222.14, "Main account")
      .add("13/04/2006", "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", -18.70, "To categorize", 236.64, 236.64, "Main account")
      .check();

    views.selectHome();
    mainAccounts.changePosition("Main account", 80, "Auchan");
    mainAccounts.editPosition("Main account")
      .checkAccountLabel("Account: Main account")
      .validate();
  }

  public void testImportAmountWithVariousDecimalSeparator() throws Exception {
    String[] blocks = {
      "D20/04/2006" + "\n" +
      "T-17,65\n" +
      "MPARIS\n" +
      "^\n" +
      "D21/04/2006" + "\n" +
      "T,117.65\n" +
      "MPARIS\n" +
      "^\n" +
      "D22/04/2006" + "\n" +
      "T-,65\n" +
      "MPARIS\n" +
      "^" +
      ""};
    importBlocks(blocks);
    transactions.initContent()
      .add("22/04/2006", TransactionType.PRELEVEMENT, "PARIS", "", -0.65)
      .add("21/04/2006", TransactionType.VIREMENT, "PARIS", "", 117.65)
      .add("20/04/2006", TransactionType.PRELEVEMENT, "PARIS", "", -17.65)
      .check();
  }

  public void testTakesUserAndBankDatesIntoAccountWhenDetectingDuplicates() throws Exception {
    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "PFAC.FRANCE 4561409\n" +
                    "MFAC.FRANCE 4561409787231717 19/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS\n" +
                    "^");
    operations.importQifFile(file, SOCIETE_GENERALE, 0.);
    operations.importQifFile(file, SOCIETE_GENERALE);

    timeline.selectMonth("2006/04");
    views.selectData();
    transactions.initContent()
      .add("19/04/2006", "20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .check();
  }

  public void testBankDateWithFrenchFormat() throws Exception {
    checkBankDate("20/04/2006", "20/04/2006");
  }

  public void testBankDateWithFrenchFormatAndShortDate() throws Exception {
    checkBankDate("20/04/06", "20/04/2006");
  }

  public void testUserDateWithFrenchFormat() throws Exception {
    String[] blocks = {
      "D20/04/2006" + "\n" +
      "T-17.65\n" +
      "N\n" +
      "PFAC.FRANCE 4561409\n" +
      "MFAC.FRANCE 4561409787231717 19/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS\n" +
      "^"};
    importBlocks(blocks);
    transactions.initContent()
      .add("19/04/2006", "20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .check();
  }

  public void testBankDateWithEnglishFormat() throws Exception {
    checkBankDate("04/20/2006", "20/04/2006");
  }

  public void testBankDateWithEnglishFormatAndShortDate() throws Exception {
    checkBankDate("12/20/06", "20/12/2006");
  }

  public void testImportingTheSameFileTwiceOnSplittedDoesNotDuplicateTransactions() throws Exception {

    QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.23, "Tx 2")
      .load(0.);
    views.selectCategorization();
    categorization.showAllTransactions();
    categorization.selectTransactions("Tx 2");
    transactionDetails.split("-1.19", "info 1");
    categorization.selectVariable().selectNewSeries("Income");

    transactionDetails.split("-0.69", "info 2");
    categorization.selectVariable().selectSeries("Income");
    transactionDetails.split("-0.01", "info 3");

    QifBuilder
      .init(this)
      .addTransaction("2006/01/11", -2.23, "Tx 2")
      .addTransaction("2006/01/12", -2.2, "Tx 3")
      .load();

    views.selectData();
    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "", -2.2)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -0.34)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info 3", -0.01)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info 2", -0.69, "Income")
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info 1", -1.19, "Income")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1)
      .check();
  }


  public void testNoAccountPositionStartAtZero() throws Exception {
    QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "VIR.LOGITEL Tx 1 with ${toto}")
      .addTransaction("2006/01/11", -2.23, "Tx 2")
      .loadFirstStartingAtZero(-3.33);

    mainAccounts.checkPosition("Main account", -3.33);
  }

  public void testAutomaticallySelectAccount() throws Exception {
    QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.23, "Tx 2")
      .load(0.);
    accounts.createMainAccount("other Account", "4321", 0);

    String newFile = QifBuilder
      .init(this)
      .addTransaction("2006/01/12", -2.23, "Tx 2")
      .save();

    operations.openImportDialog()
      .setFilePath(newFile)
      .acceptFile()
      .checkSelectedAccount("Main account")
      .completeImport();
    views.selectData();
    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "TX 2", "", -2.23)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "TX 2", "", -2.23)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "TX 1", "", -1.10)
      .check();
  }

  private void checkBankDate(String input, String expected) {
    String[] blocks = {
      "D" + input + "\n" +
      "T-17.65\n" +
      "N\n" +
      "PPRELEVEMENT 666152\n" +
      "MPRELEVEMENT 6661529970  TPS FRA01107365A040606/T.P.S. 000103017914\n" +
      "^"};
    importBlocks(blocks);
    transactions.initContent()
      .add(expected, TransactionType.PRELEVEMENT, "TPS FRA01107365A040606/T.P.S. 000103017914", "", -17.65)
      .check();
  }

  private void importBlocks(String[] blocks) {
    StringBuilder builder = new StringBuilder();
    builder.append("!Type:Bank\n");
    for (String block : blocks) {
      builder.append(block);
    }
    String file = createQifFile("file", builder.toString());
    operations.importQifFile(file, SOCIETE_GENERALE, 0.0);
    views.selectData();
  }

  private String createQifFile(String discriminant) {
    String content =
      "!Type:Bank\n" +
      "D28/03/07\n" +
      "T12,345.67\n" +
      "PVIR AVRIL\n" +
      "^\n" +
      "D26/03/07\n" +
      "T-45.00\n" +
      "PCHEQUE 0416063\n" +
      "^\n" +
      "D27/03/07\n" +
      "T-30.58\n" +
      "PMONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX";

    return createQifFile(discriminant, content);
  }

  public void testReadUnknownTag() throws Exception {
    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "ZTransfert\n" +
                    "MFAC.FRANCE 4561409787231717 19/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS\n" +
                    "^");

    operations.importQifFile(file, SOCIETE_GENERALE, 0.);

    views.selectData();
    transactions.initContent()
      .add("19/04/2006", "20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .check();

  }

  public void testInvalidFormatDoubleEnd() throws Exception {
    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "MSome info\n" +
                    "^\n" +
                    "^\n");

    operations.importQifFile(file, SOCIETE_GENERALE, 0.);

    views.selectData();
    transactions.initContent()
      .add("20/04/2006", TransactionType.PRELEVEMENT, "SOME INFO", "", -17.65)
      .check();
  }

  public void testNoLabel() throws Exception {
    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D30/04/2006\n" +
                    "T-20.00\n" +
                    "N\n" +
                    "^");
    operations.importQifFile(file, SOCIETE_GENERALE, 0.);

    transactionCreation
      .show()
      .setAmount(-12.50)
      .setDay(15)
      .setLabel("toto")
      .create();
    views.selectData();
    transactions.initContent()
      .add("30/04/2006", TransactionType.PRELEVEMENT, "", "", -20.00)
      .add("15/04/2006", TransactionType.MANUAL, "TOTO", "", -12.50)
      .check();
  }

  public void testPartialInvalidFormat() throws Exception {
    String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "MSome info\n" +
                    "^\n" +
                    "D20/04/2006\n" +
                    "^\n" +
                    "T-17.65\n" +
                    "^\n" +
                    "N\n" +
                    "MSome info\n" +
                    "^\n" +
                    "^\n");

    operations.importQifFile(file, SOCIETE_GENERALE, 0.);

    views.selectData();
    transactions.initContent()
      .add("20/04/2006", TransactionType.PRELEVEMENT, "SOME INFO", "", -17.65)
      .check();
  }

  private String createQifFile(String discriminant, String content) {
    String fileName = TestUtils.getFileName(this, discriminant + ".qif");
    Files.dumpStringToFile(fileName, content);
    return fileName;
  }
}
