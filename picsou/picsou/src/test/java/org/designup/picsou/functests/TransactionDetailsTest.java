package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class TransactionDetailsTest extends LoggedInFunctionalTestCase {
  public void testLabel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 15.10, "Quick")
      .addTransaction("2008/06/16", 27.50, "Quick")
      .addTransaction("2008/06/15", 15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkLabel("Quick");

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkLabel("Quick");
    transactionDetails.labelIsNotEditable();
    transactions.getTable().selectRows(0, 2);
    transactionDetails.checkLabel("...");
  }

  public void testDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 15.10, "Quick")
      .addTransaction("2008/06/18", 27.50, "Quick")
      .addTransaction("2008/06/15", 15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRows(0, 2);
    transactionDetails.checkNoDate();

    transactions.getTable().clearSelection();
    transactionDetails.checkNoDate();
  }

  public void testAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 10.00, "Quick")
      .addTransaction("2008/06/18", 30.00, "Quick")
      .addTransaction("2008/06/15", 20.00, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkAmount("Amount", "10.00");
    transactionDetails.checkNoAmountStatistics();

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkAmount("Total amount", "40.00");
    transactionDetails.checkAmountStatistics("10.00", "30.00", "20.00");

    transactions.getTable().clearSelection();
    transactionDetails.checkNoAmount();
    transactionDetails.checkNoAmountStatistics();
  }

  public void testCategoriesWthoutSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 10.00, "Quick")
      .load();
    transactionDetails.checkNoCategory();
  }

  public void testCategoriesWithSingleSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 10.00, "Quick")
      .addTransaction("2008/06/15", 20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", 10.00, "Fouquet's", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRow(0);
    transactionDetails.checkToCategorize();
    transactionDetails.categorizeWithLink(MasterCategory.FOOD);
    transactionDetails.checkCategory(MasterCategory.FOOD);
    transactions.getTable().selectRow(2);
    transactionDetails.categorizeWithLink(MasterCategory.EDUCATION);
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
    transactions.initContent()
      .add("18/06/2008", TransactionType.VIREMENT, "Quick", "", 10.00, MasterCategory.FOOD)
      .add("15/06/2008", TransactionType.VIREMENT, "McDo", "", 20.00, MasterCategory.FOOD)
      .add("14/06/2008", TransactionType.VIREMENT, "Fouquet's", "", 10.00, MasterCategory.EDUCATION)
      .check();
  }

  public void testCategoriesWithMultipleSimilarTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", 10.00, "Fouquet's", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkCategory(MasterCategory.FOOD);
    transactionDetails.categorizeWithLink(MasterCategory.EDUCATION);
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
    transactions.initContent()
      .add("15/06/2008", TransactionType.VIREMENT, "McDo", "", 20.00, MasterCategory.EDUCATION)
      .add("14/06/2008", TransactionType.VIREMENT, "Fouquet's", "", 10.00, MasterCategory.EDUCATION)
      .check();
  }

  public void testCategoriesWithMultipleDifferentTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", 10.00, "Fouquet's", MasterCategory.EDUCATION)
      .load();
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkManyCategories();
    transactionDetails.categorizeWithLink(MasterCategory.EDUCATION);
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
    transactions.initContent()
      .add("15/06/2008", TransactionType.VIREMENT, "McDo", "", 20.00, MasterCategory.EDUCATION)
      .add("14/06/2008", TransactionType.VIREMENT, "Fouquet's", "", 10.00, MasterCategory.EDUCATION)
      .check();
  }

  public void testSplitNotVisible() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/06/14", 10.00, "Fouquet's", MasterCategory.EDUCATION)
      .load();
    transactionDetails.checkSplitNotVisible();
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkSplitNotVisible();
  }

  public void testSplitVisible() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "McDo", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRow(0);
    transactionDetails.checkSplitVisible();
  }

  public void testSplitCallAction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "Auchan", MasterCategory.FOOD)
      .load();
    transactions.getTable().selectRow(0);
    transactionDetails.split("10", "Auchan");
    transactions.initContent()
      .add("15/06/2008", TransactionType.VIREMENT, "Auchan", "", 10.00, MasterCategory.FOOD)
      .add("15/06/2008", TransactionType.VIREMENT, "Auchan", "Auchan", 10.00)
      .check();
  }

  public void testOriginalLabelNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/16", 10.00, "PRELEVEMENT 123123 ED")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkOriginalLabelNotVisible();
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkOriginalLabelNotVisible();
  }

  public void testDisplayOriginalLabel() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactions.getTable().selectRow(0);
    transactionDetails.checkOriginalLabel("PRELEVEMENT 123123 Auchan");
    transactions.initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", 20.00)
      .check();
  }

  public void testTransactionTypeDisplay() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .addTransaction("2008/06/14", 10.00, "PRELEVEMENT 123123 Monop")
      .addTransaction("2008/06/13", 40.00, "CHEQUE 123123")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactions.getTable().selectRow(0);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkType(TransactionType.PRELEVEMENT);
    transactions.getTable().selectRow(2);
    transactionDetails.checkType(TransactionType.CHECK);
    transactions.getTable().selectRows(1, 2);
    transactionDetails.checkTypeNotVisible();
    transactions
      .initContent()
      .add("15/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", 20.00)
      .add("14/06/2008", TransactionType.PRELEVEMENT, "MONOP", "", 10.00)
      .add("13/06/2008", TransactionType.CHECK, "CHEQUE N. 123123", "", 40.00)
      .check();
  }

  public void testTransactionTypeIsNotVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "PRELEVEMENT 123123 Auchan")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkTypeNotVisible();
    transactions.getTable().selectRow(0);
  }

  public void testBankDateIsVisible() throws Exception {
    String fileName = QifBuilder.init(this)
      .addTransaction("2008/06/15", 20.00, "CARTE 123123 12/06/08 auchan")
      .addTransaction("2008/06/14", 20.00, "CARTE 123123 12/06/08 auchan")
      .save();
    operations.importQifFiles(10, "Societe generale", fileName);
    transactionDetails.checkBankDateNotVisible();
    transactions.getTable().selectRow(0);
    transactionDetails.checkBankDate("15/06/2008");
    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkBankDateNotVisible();

    transactions.initContent()
      .add("12/06/2008", TransactionType.CREDIT_CARD, "AUCHAN", "", 20.00)
      .add("12/06/2008", TransactionType.CREDIT_CARD, "AUCHAN", "", 20.00)
      .check();
  }
}
