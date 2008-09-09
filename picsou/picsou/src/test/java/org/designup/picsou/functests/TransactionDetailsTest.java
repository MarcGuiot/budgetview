package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class TransactionDetailsTest extends LoggedInFunctionalTestCase {

  public void testLabel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -15.10, "Quick")
      .addTransaction("2008/06/16", -27.50, "Quick")
      .addTransaction("2008/06/15", -15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkLabel("Quick");
    transactionDetails.checkLabelIsNotEditable();

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkLabel("Quick [2 operations]");
    transactionDetails.checkLabelIsNotEditable();

    transactions.getTable().selectRows(0, 2);
    transactionDetails.checkLabel("2 operations");
    transactionDetails.checkLabelIsNotEditable();
  }

  public void testDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -15.10, "Quick")
      .addTransaction("2008/06/18", -27.50, "Quick")
      .addTransaction("2008/06/15", -15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRowSpan(0, 2);
    transactionDetails.checkNoDate();

    transactions.getTable().clearSelection();
    transactionDetails.checkNoDate();
  }

  public void testAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -10.00, "Quick")
      .addTransaction("2008/06/18", -30.00, "Quick")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkAmount("Amount", "-10.00");
    transactionDetails.checkNoAmountStatistics();

    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkAmount("Total amount", "-40.00");
    transactionDetails.checkAmountStatistics("-30.00", "-10.00", "-20.00");

    transactions.getTable().clearSelection();
    transactionDetails.checkNoAmount();
    transactionDetails.checkNoAmountStatistics();
  }

  public void testDisplayWithNoSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/18", -10.00, "Quick", MasterCategory.FOOD)
      .addTransaction("2008/06/18", -30.00, "Quick", MasterCategory.FOOD)
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/05/13", 1000.00, "Wages", MasterCategory.INCOME)
      .load();

    timeline.selectAll();
    transactionDetails.checkNoSelectionLabels("4 operations", "1000.00", "-60.00", "940.00");

    timeline.selectMonth("2008/07");
    transactionDetails.checkNoSelectionLabels("1 operation", null, "-10.00", null);

    categories.select(MasterCategory.HOUSE);
    transactionDetails.checkNoSelectionLabels("No operation", null, null, null);

    timeline.selectAll();
    categories.select(MasterCategory.ALL);
    transactions.getTable().selectRow(0);
    transactionDetails.checkNoSelectionPanelHidden();

    categories.select(MasterCategory.HOUSE);
    transactionDetails.checkNoSelectionLabels("No operation", null, null, null);
  }

  public void testCategoriesWithoutSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -10.00, "Quick")
      .load();
    transactionDetails.checkCategoryNotVisible();
  }

  public void testDisplayedCategory() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", -10.00, "Quick")
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkNoCategory();

    transactions.getTable().selectRow(1);
    transactionDetails.checkCategory(MasterCategory.FOOD);
  }

  public void testSplitButtonInitiallyVisibleWithOneTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRow(0);
    transactionDetails.checkSplitVisible();
  }

  public void testSplitButtonInitiallyInvisibleWithTwoTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", -10.00, "Fouquet's", MasterCategory.EDUCATION)
      .load();
    transactionDetails.checkSplitNotVisible();

    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkSplitNotVisible();

    transactions.getTable().selectRow(0);
    transactionDetails.checkSplitVisible();

    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkSplitNotVisible();
  }

  public void testSplitCallAction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "Auchan", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRow(0);
    transactionDetails.split("10", "Auchan");
    transactions.initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00, MasterCategory.FOOD)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Auchan", "Auchan", -10.00)
      .check();
  }

  public void testOriginalLabelNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/16", 10.00, "PRELEVEMENT 123123 ED")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkOriginalLabelNotVisible();
    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testDisplayOriginalLabelOnlyIfDifferentFromUserLabel() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -100.00, "QUICK")
      .save();

    operations.importQifFiles(10, "Societe generale", fileName);

    transactions.initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00)
      .add("14/06/2008", TransactionType.PRELEVEMENT, "QUICK", "", -100.00)
      .check();

    transactions.getTable().selectRow(0);
    transactionDetails.checkOriginalLabel("PRELEVEMENT 123123 Auchan");

    transactions.getTable().selectRow(1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testTransactionTypeDisplay() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", -10.00, "PRELEVEMENT 123123 Monop")
      .addTransaction("2008/06/13", -40.00, "CHEQUE 123123")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactions.getTable().selectRow(0);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);
    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);
    transactions.getTable().selectRow(2);
    transactionDetails.checkType(TransactionType.CHECK);
    transactions.getTable().selectRowSpan(1, 2);
    transactionDetails.checkTypeNotVisible();
    transactions
      .initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00)
      .add("14/06/2008", TransactionType.PRELEVEMENT, "MONOP", "", -10.00)
      .add("13/06/2008", TransactionType.CHECK, "CHEQUE N. 123123", "", -40.00)
      .check();
  }

  public void testTransactionTypeIsNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "PRELEVEMENT 123123 Auchan")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkTypeNotVisible();
    transactions.getTable().selectRow(0);
  }

  public void testBankDateIsVisibleOnlyIfDifferentFromUserDate() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", -20.00, "CARTE 123123 12/06/08 AUCHAN1")
      .addTransaction("2008/06/10", -10.00, "CARTE 123123 10/06/08 AUCHAN2")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactions.initContent()
      .add("12/06/2008", TransactionType.CREDIT_CARD, "AUCHAN1", "", -20.00)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "AUCHAN2", "", -10.00)
      .check();

    transactionDetails.checkBankDateNotVisible();

    transactions.getTable().selectRow(0);
    transactionDetails.checkBankDate("15/06/2008");

    transactions.getTable().selectRowSpan(0, 1);
    transactionDetails.checkBankDateNotVisible();

    transactions.getTable().selectRows(1);
    transactionDetails.checkBankDateNotVisible();
  }
}
