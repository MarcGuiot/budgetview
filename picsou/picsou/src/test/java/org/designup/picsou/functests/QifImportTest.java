package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

public class QifImportTest extends LoggedInFunctionalTestCase {

  public void testImportsQifFilesFromSG() throws Exception {
    learn("BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", MasterCategory.FOOD);
    learn("STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", MasterCategory.TRANSPORTS);
    learn("STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", MasterCategory.TRANSPORTS);
    learn("SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", MasterCategory.FOOD);

    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(QifImportTest.class.getResourceAsStream("/testfiles/sg1.qif"),
                           fileName);

    operations.importQifFile(100.0, fileName, "Societe Generale");
    transactions
      .initContent()
      .add("22/04/2006", TransactionType.CREDIT_CARD, "SACLAY", "", -55.49)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00, MasterCategory.FOOD)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65, MasterCategory.TRANSPORTS)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50, MasterCategory.FOOD)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70, MasterCategory.TRANSPORTS)
      .check();
    double totalForAutomobile = -(18.70 + 17.65);
    double totalForAlimentation = -(14.50 + 49.00);
    double total = totalForAutomobile + totalForAlimentation - 55;
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, total, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 55, 55 / total)
      .add(MasterCategory.FOOD, 0.0, 0.0, totalForAlimentation, totalForAlimentation / total)
      .add(MasterCategory.TRANSPORTS, 0.0, 0.0, totalForAutomobile, totalForAutomobile / total)
      .check();

    categories.assertSelectionEquals(MasterCategory.ALL);
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
    operations.importQifFile(12.50, file, "Societe Generale");
    operations.importQifFile(12.50, file, "Societe Generale");

    periods.selectCells("2006/04");
    transactions.initContent()
      .add("19/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
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
      .add("19/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .check();
  }

  public void testBankDateWithEnglishFormat() throws Exception {
    checkBankDate("04/20/2006", "20/04/2006");
  }

  public void testBankDateWithEnglishFormatAndShortDate() throws Exception {
    checkBankDate("12/20/06", "20/12/2006");
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
    operations.importQifFile(12.50, file, "Societe Generale");
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

  private String createQifFile(String discriminant, String content) {
    String fileName = TestUtils.getFileName(this, discriminant + ".qif");
    Files.dumpStringToFile(fileName, content);
    return fileName;
  }

  public void testImportManyFileAndAskOnceForAmount() throws Exception {
    String file1 = createQifFile("1");
    String file2 = createQifFile("2");
    operations.importQifFiles(12.50, "Societe Generale", file1, file2);
  }
}
