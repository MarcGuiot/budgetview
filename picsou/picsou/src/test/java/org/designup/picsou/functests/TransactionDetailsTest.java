package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

public class TransactionDetailsTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    views.selectCategorization();
  }

  public void testLabel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .addTransaction("2008/06/18", -15.10, "Burger King")
      .addTransaction("2008/06/15", -15.50, "McDo")
      .load();

    categorization.selectTableRow(2);
    transactionDetails.checkLabel("MCDO");
    transactionDetails.checkLabelIsNotEditable();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkLabel("BURGER KING [2 operations]");
    transactionDetails.checkLabelIsNotEditable();

    categorization.selectTableRows(0, 2);
    transactionDetails.checkLabel("2 operations");
    transactionDetails.checkLabelIsNotEditable();
  }

  public void testAnonymizedLabelDisplayedForMultiSelections() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .addTransaction("2008/06/16", -27.50, "Cheque 1")
      .addTransaction("2008/06/18", -15.10, "Cheque 2")
      .addTransaction("2008/06/18", -15.10, "McDo 1")
      .addTransaction("2008/06/18", -15.10, "McDo 2")
      .load();

    categorization.selectTableRows(1, 2);
    transactionDetails.checkLabel("2 operations");

    categorization.selectTableRows(3, 4);
    transactionDetails.checkLabel("MCDO [2 operations]");
  }

  public void testDisplayWithNoSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/18", -10.00, "Burger King")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .addTransaction("2008/05/13", 1000.00, "Wages")
      .load();

    timeline.selectAll();
    categorization.checkNoSelectedTableRows();
    transactionDetails.checkMessage("No operation selected");
  }

  public void testDisplayWithNoData() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/15", -10.00, "Burger King")
      .addTransaction("2008/06/15", -10.00, "McDo")
      .load();

    timeline.selectMonth("2008/07");
    categorization.showSelectedMonthsOnly();
    categorization.checkNoSelectedTableRows();
    transactionDetails.checkNoDataMessage();
  }

  public void testDisplayAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("0001", 0.0, "2008/07/31")
      .addTransaction("2008/08/18", -10.00, "Burger King")
      .addBankAccount("0002", 0.0, "2008/07/31")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();

    categorization.checkNoSelectedTableRows();
    transactionDetails.checkNoAccountDisplayed();

    categorization.selectTransaction("Burger King");
    transactionDetails.checkAccount("Account n. 0001");
    
    categorization.selectTransaction("McDo");
    transactionDetails.checkAccount("Account n. 0002");

    categorization.selectAllTransactions();
    transactionDetails.checkAccount("Multiple accounts");
  }

  public void testSplitButtonInitiallyVisibleWithOneTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();
    categorization.selectTableRow(0);
    transactionDetails.checkSplitVisible();
  }

  public void testSplitButtonLabelChanges() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();
    categorization.selectTableRow(0);
    transactionDetails.checkSplitButtonLabel("Split...");

    transactionDetails.openSplitDialog()
      .enterAmount("5")
      .validateAndClose();
    transactionDetails.checkSplitButtonLabel("View split...");
  }

  public void testSplitButtonInitiallyInvisibleWithTwoTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo")
      .addTransaction("2008/06/14", -10.00, "Fouquet's")
      .load();

    transactionDetails.checkSplitNotVisible();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkSplitNotVisible();

    categorization.selectTableRow(0);
    transactionDetails.checkSplitVisible();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkSplitNotVisible();
  }

  public void testOriginalLabelNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/16", 10.00, "PRELEVEMENT 123123 ED")
      .save();
//    System.out.println("TransactionDetailsTest.testOriginalLabelNotVisible " + fileName);
//    openApplication();
    operations.importQifFiles(SOCIETE_GENERALE, fileName);
    transactionDetails.checkOriginalLabelNotVisible();
    categorization.selectTableRows(0, 1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testDisplayOriginalLabelOnlyIfDifferentFromUserLabel() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -100.00, "Burger King")
      .save();

    operations.importQifFiles(SOCIETE_GENERALE, fileName);

    views.selectCategorization();
    categorization.initContent()
      .add("15/06/2008", "", "AUCHAN", -20.00)
      .add("14/06/2008", "", "Burger King", -100.00)
      .check();

    views.selectCategorization();
    categorization.selectTableRow(0);
    transactionDetails.checkOriginalLabel("PRELEVEMENT 123123 AUCHAN");
    categorization.selectTableRow(1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testTransactionTypeDisplay() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -10.00, "PRELEVEMENT 123123 Monop")
      .addTransaction("2008/06/13", -40.00, "CHEQUE 123123")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, fileName);

    categorization.initContent()
      .add("15/06/2008", "", "AUCHAN", -20.00)
      .add("13/06/2008", "", "CHEQUE N°123123", -40.00)
      .add("14/06/2008", "", "MONOP", -10.00)
      .check();

    categorization.selectTableRow(0);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);

    categorization.selectTableRows(0, 2);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);

    categorization.selectTableRow(1);
    transactionDetails.checkType(TransactionType.CHECK);

    categorization.selectTableRows(1, 2);
    transactionDetails.checkTypeNotVisible();
  }

  public void testTransactionTypeIsNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, fileName);

    categorization.getTable().clearSelection();
    transactionDetails.checkTypeNotVisible();
  }

  public void testBankDateIsVisibleOnlyIfDifferentFromUserDate() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "CARTE 123123 12/06/08 AUCHAN1")
      .addTransaction("2008/06/10", -10.00, "CARTE 123123 10/06/08 AUCHAN2")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, fileName);

    categorization.initContent()
      .add("12/06/2008", "", "AUCHAN1", -20.00)
      .add("10/06/2008", "", "AUCHAN2", -10.00)
      .check();

    transactionDetails.checkDetailsNotVisible();

    categorization.selectTableRow(0);
    transactionDetails.checkBankDate("15/06/2008");

    categorization.selectTableRows(0, 1);
    transactionDetails.checkBankDateNotVisible();

    categorization.selectTableRows(1);
    transactionDetails.checkBankDateNotVisible();
  }

  public void testEditingNotes() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -10.50, "Burger King")
      .addTransaction("2008/06/18", -10.50, "McDo")
      .addTransaction("2008/06/18", -10.10, "Quick")
      .load();

    transactionDetails.checkNotesFieldNotVisible();

    categorization.selectTableRow(0);
    transactionDetails.checkNote("");
    transactionDetails.setNote("burger");
    transactionDetails.checkNote("burger");

    categorization.selectTableRow(1);
    transactionDetails.checkNote("");
    transactionDetails.setNote("mac");
    transactionDetails.checkNote("mac");

    categorization.selectTableRow(0);
    transactionDetails.checkNote("burger");

    categorization.selectTableRows(0, 1);
    transactionDetails.checkNote("");
    transactionDetails.setNote("common");

    categorization.selectTableRow(0);
    transactionDetails.checkNote("common");

    categorization.selectTableRow(1);
    transactionDetails.checkNote("common");
  }
}
