package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OfxExportTest extends LoggedInFunctionalTestCase {

  public void testExport() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.2, "Menu K", MasterCategory.FOOD)
      .load();
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.2, MasterCategory.FOOD)
      .check();

    views.selectCategorization();
    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("-1")
      .enterNote("COCA")
      .ok();

    categorization.setEnvelope("Menu K", "Groceries", MasterCategory.FOOD, true);

    String fileName = TestUtils.getFileName(this, ".ofx");
    operations.exportFile(fileName);
    String exportedContent = Files.loadFileToString(fileName);

    assertTrue(exportedContent.contains(
      "<BANKTRANLIST>\n" +
      "          <DTSTART>20060131000000\n" +
      "          <DTEND>20060203000000\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20060110\n" +
      "            <DTUSER>20060110\n" +
      "            <TRNAMT>-0.2\n" +
      "            <FITID>PICSOU0\n" +
      "            <NAME>Menu K\n" +
      "            <CATEGORY>2\n" +
      "          </STMTTRN>\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20060110\n" +
      "            <DTUSER>20060110\n" +
      "            <TRNAMT>-1.0\n" +
      "            <FITID>PICSOU33\n" +
      "            <NAME>Menu K\n" +
      "            <CATEGORY>2\n" +
      "            <NOTE>COCA\n" +
      "            <PARENT>PICSOU0\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>"
    ));
  }

  public void testAsksForConfirmationWhenTheSelectedFileExists() throws Exception {
    String fileName = TestUtils.getFileName(this, ".ofx");

    Files.dumpStringToFile(fileName, "Blah");

    WindowInterceptor
      .init(operations.getExportTrigger())
      .process(FileChooserHandler.init().select(fileName))
      .processWithButtonClick("Confirmation", "No")
      .run();

    assertEquals("Blah", Files.loadFileToString(fileName).trim());

    WindowInterceptor
      .init(operations.getExportTrigger())
      .process(FileChooserHandler.init().select(fileName))
      .processWithButtonClick("Confirmation", "Yes")
      .run();

    assertTrue(Files.loadFileToString(fileName).startsWith("<OFX>"));
  }

  public void testExportAndReImportCheck() throws Exception {
    String content =
      "!Type:Bank\n" +
      "D28/03/07\n" +
      "T12,345.67\n" +
      "MVIR.LOGITEL AVRIL\n" +
      "^\n" +
      "D26/03/07\n" +
      "T-45.00\n" +
      "MCHEQUE 0416063\n" +
      "^\n" +
      "D27/03/07\n" +
      "T-30.58\n" +
      "MFAC.FRANCE 4561409787231717 27/03/07 MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX\n" +
      "^\n";

    String fileName = TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(fileName, content);
    operations.importQifFile(fileName, SOCIETE_GENERALE);

    transactions.initContent()
      .add("28/03/2007", TransactionType.VIREMENT, "AVRIL", "", 12345.67)
      .add("27/03/2007", TransactionType.CREDIT_CARD,
           "MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX", "", -30.58)
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N. 0416063", "", -45.0)
      .check();

    String ofxFileName = TestUtils.getFileName(this, ".ofx");
    operations.exportFile(ofxFileName);

    operations.importOfxFile(ofxFileName);

    transactions.initContent()
      .add("28/03/2007", TransactionType.VIREMENT, "AVRIL", "", 12345.67)
      .add("27/03/2007", TransactionType.CREDIT_CARD,
           "MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX", "", -30.58)
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N. 0416063", "", -45.0)
      .check();
  }
}
