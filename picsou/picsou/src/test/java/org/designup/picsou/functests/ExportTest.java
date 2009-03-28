package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class ExportTest extends LoggedInFunctionalTestCase {

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
