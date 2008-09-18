package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
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
    transactionDetails.checkLabel("McDo");
    transactionDetails.checkLabelIsNotEditable();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkLabel("Burger King [2 operations]");
    transactionDetails.checkLabelIsNotEditable();

    categorization.selectTableRows(0, 2);
    transactionDetails.checkLabel("2 operations");
    transactionDetails.checkLabelIsNotEditable();
  }

  public void testDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -27.50, "Burger King")
      .addTransaction("2008/06/18", -15.10, "Burger King")
      .addTransaction("2008/06/15", -15.50, "McDo")
      .load();

    categorization.selectTableRow(0);
    transactionDetails.checkDate("18/06/2008");

    categorization.selectTableRows(0, 1);
    transactionDetails.checkDate("18/06/2008");

    categorization.selectTableRows(0, 1, 2);
    transactionDetails.checkNoDate();

    transactions.getTable().clearSelection();
    transactionDetails.checkNoDate();
  }

  public void testAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -10.00, "Burger King")
      .addTransaction("2008/06/18", -30.00, "Burger King")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();

    categorization.initContent()
      .add("18/06/2008", TransactionType.PRELEVEMENT, "Burger King", "", -10.00)
      .add("18/06/2008", TransactionType.PRELEVEMENT, "Burger King", "", -30.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "McDo", "", -20.00)
      .check();

    categorization.selectTableRow(2);
    transactionDetails.checkAmount("Amount", "-20.00");
    transactionDetails.checkNoAmountStatistics();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkAmount("Total amount", "-40.00");
    transactionDetails.checkAmountStatistics("-30.00", "-10.00", "-20.00");

    transactions.getTable().clearSelection();
    transactionDetails.checkNoAmount();
    transactionDetails.checkNoAmountStatistics();
  }

  public void testDisplayWithNoSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/18", -10.00, "Burger King")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .addTransaction("2008/05/13", 1000.00, "Wages")
      .load();

    timeline.selectAll();
    categorization.checkNoSelectedTableRows();
    transactionDetails.checkNoSelectionLabels("3 operations", "1000.00", "-30.00", "970.00");

    timeline.selectMonth("2008/08");
    transactionDetails.checkNoSelectionLabels("1 operation", null, "-10.00", null);

    timeline.selectMonth("2008/07");
    transactionDetails.checkNoSelectionPanelHidden();

    timeline.selectAll();
    transactionDetails.checkNoSelectionLabels("3 operations", "1000.00", "-30.00", "970.00");
  }

  public void testSplitButtonInitiallyVisibleWithOneTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .load();
    categorization.selectTableRow(0);
    transactionDetails.checkSplitVisible();
  }

  public void testSplitButtonInitiallyInvisibleWithTwoTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", -10.00, "Fouquet's", MasterCategory.EDUCATION)
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
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkOriginalLabelNotVisible();
    categorization.selectTableRows(0, 1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testDisplayOriginalLabelOnlyIfDifferentFromUserLabel() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -100.00, "Burger King")
      .save();

    operations.importQifFiles(10, "Societe generale", fileName);

    categorization.initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00)
      .add("14/06/2008", TransactionType.PRELEVEMENT, "Burger King", "", -100.00)
      .check();

    transactions.getTable().selectRow(0);
    transactionDetails.checkOriginalLabel("PRELEVEMENT 123123 Auchan");

    categorization.selectTableRow(1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testTransactionTypeDisplay() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -10.00, "PRELEVEMENT 123123 Monop")
      .addTransaction("2008/06/13", -40.00, "CHEQUE 123123")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);

    categorization.initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00)
      .add("13/06/2008", TransactionType.CHECK, "CHEQUE N. 123123", "", -40.00)
      .add("14/06/2008", TransactionType.PRELEVEMENT, "MONOP", "", -10.00)
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
    operations.importQifFiles(10, "Societe generale", fileName);

    categorization.getTable().clearSelection();
    transactionDetails.checkTypeNotVisible();
  }

  public void testBankDateIsVisibleOnlyIfDifferentFromUserDate() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "CARTE 123123 12/06/08 AUCHAN1")
      .addTransaction("2008/06/10", -10.00, "CARTE 123123 10/06/08 AUCHAN2")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);

    categorization.initContent()
      .add("12/06/2008", TransactionType.CREDIT_CARD, "AUCHAN1", "", -20.00)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "AUCHAN2", "", -10.00)
      .check();

    transactionDetails.checkBankDateNotVisible();

    categorization.selectTableRow(0);
    transactionDetails.checkBankDate("15/06/2008");

    categorization.selectTableRows(0, 1);
    transactionDetails.checkBankDateNotVisible();

    categorization.selectTableRows(1);
    transactionDetails.checkBankDateNotVisible();
  }
}
