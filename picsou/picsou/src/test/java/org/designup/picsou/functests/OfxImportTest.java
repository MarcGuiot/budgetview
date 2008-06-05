package org.designup.picsou.functests;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobFieldMatcher;
import org.crossbowlabs.globs.utils.Files;
import org.crossbowlabs.globs.utils.TestUtils;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.importer.ofx.OfxWriter;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.io.FileWriter;

public class OfxImportTest extends LoggedInFunctionalTestCase {

  public void testManagesCategoriesInOfxFiles() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Dr Lecter", MasterCategory.HEALTH)
      .addTransaction("2006/01/11", -2.2, "MiamMiam")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MiamMiam", "", -2.2, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -1.1, MasterCategory.HEALTH)
      .check();
  }

  public void testManagesSubcategoriesInOfxFiles() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "apero")
      .addTransaction("2006/01/10", -1.1, "Dr Lecter", MasterCategory.HEALTH)
      .addTransaction("2006/01/11", -2.2, "MiamMiam", "apero")
      .load();

    categories.select(MasterCategory.ALL);

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MiamMiam", "", -2.2, "Apero")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -1.1, MasterCategory.HEALTH)
      .check();
  }

  public void testCreatesSubcategoriesFromOfxFiles() throws Exception {
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addCategory(MasterCategory.TRANSPORTS, "Oil")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu", "Apero", "Oil")
      .load();

    categories.assertCategoryExists("Apero");
    categories.assertCategoryExists("Oil");

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero, Oil")
      .check();
  }

  public void testManagesMultipleSubcategoriesInOfxFiles() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.TRANSPORTS, "Oil");

    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addCategory(MasterCategory.TRANSPORTS, "Oil")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu", "Apero", "Oil")
      .load();

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero, Oil")
      .check();
  }

  public void testImportingTheSameFileTwiceDoesNotDuplicateTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -2.2, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testImportingASecondFileWithNewerTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "Tx 1")
      .addTransaction("2006/01/12", -2.2, "Tx 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -3.3, "Tx 3")
      .addTransaction("2006/01/14", -4.4, "Tx 4")
      .load();

    transactions
      .initContent()
      .add("14/01/2006", TransactionType.PRELEVEMENT, "Tx 4", "", -4.4, MasterCategory.NONE)
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "", -3.3, MasterCategory.NONE)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -2.2, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.NONE)
      .check();
  }

  public void testImportingASecondFileWithOlderTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2005/12/25", -10.0, "Tx 0", MasterCategory.HOUSE)
      .load();

    periods.selectCells(0, 1);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -2.2, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .add("25/12/2005", TransactionType.PRELEVEMENT, "Tx 0", "", -10.0, MasterCategory.HOUSE)
      .check();
  }

  public void testImportingOverlappingFilesDoesNotDuplicateTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1")
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .addTransaction("2006/01/12", -3.3, "Tx 3")
      .load();

    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "", -3.3, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -2.2, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.NONE)
      .check();
  }

  public void testImportingFilesWithDuplicatesBeforeAndAfter() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -3.3, "Tx 3")
      .addTransaction("2006/01/14", -4.4, "Tx 4")
      .addTransaction("2006/01/15", -5.5, "Tx 5")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/16", -6.6, "Tx 6")
      .addTransaction("2006/01/15", -5.5, "Tx 5")
      .addTransaction("2006/01/13", -3.3, "Tx 3")
      .addTransaction("2006/01/12", -2.2, "Tx 2")
      .load();

    transactions
      .initContent()
      .add("16/01/2006", TransactionType.PRELEVEMENT, "Tx 6", "", -6.6, MasterCategory.NONE)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Tx 5", "", -5.5, MasterCategory.NONE)
      .add("14/01/2006", TransactionType.PRELEVEMENT, "Tx 4", "", -4.4, MasterCategory.NONE)
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "", -3.3, MasterCategory.NONE)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -2.2, MasterCategory.NONE)
      .check();
  }

  public void testManagesUnknownBanksWithADefaultOne() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(1234, 56, "acc1", 1.0, "2006/01/10")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();
  }

  public void testUsingMasterCategoryNames() throws Exception {
    String fileName = TestUtils.getFileName(this, "_setup.ofx");
    OfxWriter writer = new OfxWriter(new FileWriter(fileName));
    writer.writeHeader();
    writer.writeBankMsgHeader(12345, 12345, "1111");
    writer.startTransaction("20060524000000", -99.0, 1, "blah")
      .add("category", MasterCategory.FOOD.getName())
      .end();
    writer.writeBankMsgFooter(123.56, "2006052(000000");
    writer.writeFooter();
    operations.importOfxFile(fileName);

    transactions
      .initContent()
      .add("24/05/2006", TransactionType.PRELEVEMENT, "blah", "", -99.00, MasterCategory.FOOD)
      .check();
  }

  public void testManagesSplitOfTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/01/15", -10.0, "Auchan", "RAS", MasterCategory.FOOD)
      .splitTransaction("2006/01/15", "Auchan", -10, "DVD", MasterCategory.LEISURES.getName())
      .load();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "RAS", -10, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -10, MasterCategory.LEISURES)
      .check();

    Glob parentTransaction = repository.getAll(Transaction.TYPE, new GlobFieldMatcher(Transaction.NOTE, "RAS")).get(0);
    Glob splitTransaction = repository.getAll(Transaction.TYPE, new GlobFieldMatcher(Transaction.NOTE, "DVD")).get(0);
    assertTrue(parentTransaction.get(Transaction.SPLIT, false));
    assertEquals(parentTransaction.get(Transaction.ID),
                 splitTransaction.get(Transaction.SPLIT_SOURCE));
  }

  public void testImportingTheSameFileTwiceOnSplittedDoesNotDuplicateTransactions() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -1, "info", MasterCategory.BEAUTY.getName())
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.2, "Tx 2")
      .load();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.0, MasterCategory.BEAUTY)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testImportingTheSameSplitedFileTwiceDoesNotDuplicateTransactions() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -1, "info", MasterCategory.BEAUTY.getName())
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -1, "info", MasterCategory.BEAUTY.getName())
      .load();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.0, MasterCategory.BEAUTY)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testImportingTheSameSplittedFileOnDifferentSplitKeepTheActualSplit() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -1.5, "info", MasterCategory.BEAUTY.getName())
      .splitTransaction("2006/01/11", "Tx 2", -1.5, "info2", MasterCategory.CLOTHING.getName())
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -3, "info", MasterCategory.BEAUTY.getName())
      .load();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.5, MasterCategory.BEAUTY)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info2", -1.5, MasterCategory.CLOTHING)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testImportingSplitOnOtherSplitImportUnknownSplitTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -1.2, "Tx 2")
      .splitTransaction("2006/01/11", "Tx 2", -1.5, "info", MasterCategory.BEAUTY.getName())
      .splitTransaction("2006/01/11", "Tx 2", -1.5, "info2", MasterCategory.CLOTHING.getName())
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/14", -1.2, "Tx 3")
      .splitTransaction("2006/01/14", "Tx 3", -1, "info", MasterCategory.BEAUTY.getName())
      .load();

    transactions
      .initContent()
      .add("14/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "", -1.2, MasterCategory.NONE)
      .add("14/01/2006", TransactionType.PRELEVEMENT, "Tx 3", "info", -1.0, MasterCategory.BEAUTY)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "", -1.2, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info", -1.5, MasterCategory.BEAUTY)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Tx 2", "info2", -1.5, MasterCategory.CLOTHING)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testImportWithTwoSubCategory() throws Exception {
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "sub1")
      .addCategory(MasterCategory.FOOD, "sub2")
      .addTransaction("2006/01/10", -1.1, "Tx 1", "sub1", "sub2")
      .load();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, "sub1", "sub2")
      .check();

  }

  public void testImportWithTwoSubCategoryAndOneCategory() throws Exception {
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "sub1")
      .addCategory(MasterCategory.FOOD, "sub2")
      .addTransaction("2006/01/10", -1.1, "Tx 1", MasterCategory.FOOD.getName(), "sub1", "sub2")
      .load();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Tx 1", "", -1.1, "Alimentation/Courses", "sub1", "sub2")
      .check();

  }

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

  public void testInvalidContent() throws Exception {
    checkInvalidFileImport("Hello world!");
  }

  private void checkInvalidFileImport(String fileContent) {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -45.0, "Dr Lecter")
      .load();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -45.0)
      .check();

    final String fileName = TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(fileName, fileContent);
    WindowInterceptor
      .init(new Trigger() {
        public void run() throws Exception {
          operations.importOfxFile(fileName);
        }
      })
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          assertTrue(window.containsLabel("Le contenu du fichier " + fileName + " est invalide"));
          return window.getButton("OK").triggerClick();
        }
      })
      .run();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -45.0)
      .check();
  }

  public void testInvalidFileExtension() throws Exception {
    WindowInterceptor
      .init(new Trigger() {
        public void run() throws Exception {
          operations.importOfxFile("file.dat");
        }
      })
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          assertTrue(window.containsLabel("seules les extensions OFX et QIF sont acceptées"));
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
  }
}
