package com.budgetview.functests.importexport;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;

public class ExportTest extends LoggedInFunctionalTestCase {

  @Test
  public void testOfxExport() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", -25.00, "15/06/2008")
      .addTransaction("2008/06/10", -100.0, "FNAC")
      .addBankAccount(30006, 1024, "12345678b", 12.0, "15/06/2008")
      .addTransaction("2008/06/05", -256.0, "Monop's")
      .addTransaction("2008/06/13", "2008/06/15", 1.0, "Carouf")
      .loadUnknown("Other");

    views.selectCategorization();
    categorization.setNewVariable("MONOP'S", "Groceries");

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
      "            <FITID>PICSOU103\n" +
      "            <NAME>FNAC\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>-25.00\n" +
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
      "            <FITID>PICSOU114\n" +
      "            <NAME>MONOP'S\n" +
      "          </STMTTRN>\n" +
      "          <STMTTRN>\n" +
      "            <TRNTYPE>CREDIT\n" +
      "            <DTPOSTED>20080615\n" +
      "            <DTUSER>20080613\n" +
      "            <TRNAMT>1.00\n" +
      "            <FITID>PICSOU115\n" +
      "            <NAME>CAROUF\n" +
      "          </STMTTRN>\n" +
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>12.00\n" +
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

  @Test
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
    operations.importQifFile(fileName, SOCIETE_GENERALE, 0.00);

    views.selectData();
    transactions.initContent()
      .add("28/03/2007", TransactionType.VIREMENT, "VIREMENT LOGITEL AVRIL", "", 12345.67)
      .add("27/03/2007", TransactionType.CREDIT_CARD,
           "MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX", "", -30.58)
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N°0416063", "", -45.0)
      .check();

    String ofxFileName = TestUtils.getFileName(this, ".ofx");
    operations.exportOfxFile(ofxFileName);

    operations.importOfxOnAccount(ofxFileName, "Main account");

    transactions.initContent()
      .add("28/03/2007", TransactionType.VIREMENT, "VIREMENT LOGITEL AVRIL", "", 12345.67)
      .add("27/03/2007", TransactionType.CREDIT_CARD,
           "MONOPRIX CARTE 24371925 PAIEMENT CB 2303 SCEAUX", "", -30.58)
      .add("26/03/2007", TransactionType.CHECK, "CHEQUE N°0416063", "", -45.0)
      .check();
  }

  @Test
  public void testTSVExport() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", -25.00, "15/06/2008")
      .addTransaction("2008/06/10", -100.0, "FNAC")
      .addBankAccount(-1, 1024, "12345678b", 12.00, "15/06/2008")
      .addTransaction("2008/06/05", -256.00, "Monop's")
      .addTransaction("2008/06/08", "2008/06/15", 1.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("MONOP'S", "Groceries");

    categorization.selectTransaction("CAROUF");
    transactionDetails.shift();

    String fileName = TestUtils.getFileName(this, "txt");

    operations.openExport()
      .selectTsv()
      .validate(fileName);

    assertEquals("Operation date\tBank date\tBudget date\tLabel\tAmount\tCompte\tBudget area\tSeries\n" +
                 "2008/06/10\t2008/06/10\t2008/06/10\tFNAC\t-100.00\tAccount n. 000123\tTo categorize\t\n" +
                 "2008/06/08\t2008/06/15\t2008/05/31\tCAROUF\t1.00\tAccount n. 12345678b\tTo categorize\t\n" +
                 "2008/06/05\t2008/06/05\t2008/06/05\tMONOP'S\t-256.00\tAccount n. 12345678b\tVariable\tGroceries\n",
                 Files.loadFileToString(fileName));
  }

  @Test
  public void testAsksForConfirmationWhenTheSelectedFileExists() throws Exception {
    String fileName = TestUtils.getFileName(this, ".ofx");

    Files.dumpStringToFile(fileName, "Blah");

    operations.openExport()
      .selectOfx()
      .validateAndCancelReplace(fileName);

    assertEquals("Blah", Files.loadFileToString(fileName).trim());

    operations.openExport()
      .selectOfx()
      .validateAndConfirmReplace(fileName);

    assertTrue(Files.loadFileToString(fileName).startsWith("<OFX>"));
  }

}
