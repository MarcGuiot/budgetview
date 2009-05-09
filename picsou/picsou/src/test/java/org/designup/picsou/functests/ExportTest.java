package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class ExportTest extends LoggedInFunctionalTestCase {

  public void testOfxExport() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", -25.00, "15/06/2008")
      .addTransaction("2008/06/10", -100.0, "FNAC")
      .addBankAccount(30006, 1024, "12345678b", 12.0, "15/06/2008")
      .addTransaction("2008/06/05", -256.0, "Monop's")
      .addTransaction("2008/06/13", "2008/06/15", 1.0, "Carouf")
      .load();

    views.selectCategorization();
    categorization
      .selectTransaction("MONOP'S")
      .selectEnvelopes()
      .selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    String fileName = TestUtils.getFileName(this, "ofx");
    operations.exportOfxFile(fileName);

    assertEquals(
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
      "          <BANKID>30006\n" +
      "          <BRANCHID>\n" +
      "          <ACCTID>000123\n" +
      "          <ACCTTYPE>CHECKING\n" +
      "        </BANKACCTFROM>\n" +
      "        <BANKTRANLIST>\n" +
      "          <DTSTART>20060131000000\n" +
      "          <DTEND>20060203000000\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20080610\n" +
      "            <DTUSER>20080610\n" +
      "            <TRNAMT>-100.00\n" +
      "            <FITID>PICSOU6\n" +
      "            <NAME>FNAC\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>-125.00\n" +
      "          <DTASOF>20080610000000\n" +
      "        </LEDGERBAL>\n" +
      "        <AVAILBAL>\n" +
      "          <BALAMT>0.0\n" +
      "          <DTASOF>20060704000000\n" +
      "        </AVAILBAL>\n" +
      "      </CCSTMTRS>\n" +
      "    </CCSTMTTRNRS>\n" +
      "  </BANKMSGSRSV1>\n" +
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
      "          <BANKID>30006\n" +
      "          <BRANCHID>\n" +
      "          <ACCTID>12345678b\n" +
      "          <ACCTTYPE>CHECKING\n" +
      "        </BANKACCTFROM>\n" +
      "        <BANKTRANLIST>\n" +
      "          <DTSTART>20060131000000\n" +
      "          <DTEND>20060203000000\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20080605\n" +
      "            <DTUSER>20080605\n" +
      "            <TRNAMT>-256.00\n" +
      "            <FITID>PICSOU7\n" +
      "            <NAME>MONOP'S\n" +
      "          </STMTTRN>\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>DEBIT\n" +
      "            <DTPOSTED>20080615\n" +
      "            <DTUSER>20080613\n" +
      "            <TRNAMT>1.00\n" +
      "            <FITID>PICSOU8\n" +
      "            <NAME>CAROUF\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>-243.00\n" +
      "          <DTASOF>20080615000000\n" +
      "        </LEDGERBAL>\n" +
      "        <AVAILBAL>\n" +
      "          <BALAMT>0.0\n" +
      "          <DTASOF>20060704000000\n" +
      "        </AVAILBAL>\n" +
      "      </CCSTMTRS>\n" +
      "    </CCSTMTTRNRS>\n" +
      "  </BANKMSGSRSV1>\n" +
      "</OFX>\n",
      Files.loadFileToString(fileName));
  }

  public void testOFXExportAndReImport() throws Exception {
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
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N°0416063", "", -45.0)
      .check();

    String ofxFileName = TestUtils.getFileName(this, ".ofx");
    operations.exportOfxFile(ofxFileName);

    operations.importOfxFile(ofxFileName);

    transactions.initContent()
      .add("28/03/2007", TransactionType.VIREMENT, "AVRIL", "", 12345.67)
      .add("27/03/2007", TransactionType.CREDIT_CARD,
           "MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX", "", -30.58)
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N°0416063", "", -45.0)
      .check();
  }

  public void testTSVExport() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", -25.00, "15/06/2008")
      .addTransaction("2008/06/10", -100.0, "FNAC")
      .addBankAccount(30006, 1024, "12345678b", 12.0, "15/06/2008")
      .addTransaction("2008/06/05", -256.0, "Monop's")
      .addTransaction("2008/06/13", "2008/06/15", 1.0, "Carouf")
      .load();

    views.selectCategorization();
    categorization
      .selectTransaction("MONOP'S")
      .selectEnvelopes()
      .selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    String fileName = TestUtils.getFileName(this, "txt");
    WindowInterceptor
      .init(operations.getExportTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getRadioButton("TSV").click();
          return window.getButton("OK").triggerClick();
        }
      })
      .process(FileChooserHandler.init().select(fileName))
      .run();

    assertEquals("Date\tBank date\tLabel\tAmount\tBudget area\tSeries\n" +
                 "13/06/2008\t15/06/2008\tCAROUF\t1.00\tTo categorize\t\n" +
                 "10/06/2008\t10/06/2008\tFNAC\t-100.00\tTo categorize\t\n" +
                 "05/06/2008\t05/06/2008\tMONOP'S\t-256.00\tEnvelopes\tGroceries\n",
                 Files.loadFileToString(fileName));
  }

  public void testAsksForConfirmationWhenTheSelectedFileExists() throws Exception {
    String fileName = TestUtils.getFileName(this, ".ofx");

    Files.dumpStringToFile(fileName, "Blah");

    WindowInterceptor
      .init(operations.getExportTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getRadioButton("OFX").click();
          return window.getButton("OK").triggerClick();
        }
      })
      .process(FileChooserHandler.init().select(fileName))
      .processWithButtonClick("Confirmation", "No")
      .run();

    assertEquals("Blah", Files.loadFileToString(fileName).trim());

    WindowInterceptor
      .init(operations.getExportTrigger())
      .processWithButtonClick("OK")
      .process(FileChooserHandler.init().select(fileName))
      .processWithButtonClick("Confirmation", "Yes")
      .run();

    assertTrue(Files.loadFileToString(fileName).startsWith("<OFX>"));
  }

}
