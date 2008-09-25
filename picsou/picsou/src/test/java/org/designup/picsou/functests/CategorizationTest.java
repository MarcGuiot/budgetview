package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class CategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    super.setUp();
  }

  public void testStandardIncomeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .checkLabel("WorldCo/june")
      .selectIncome()
      .checkNoSeriesMessage("You must create a series")
      .selectIncomeSeries("Salary", true)
      .checkNoSeriesMessageHidden();

    views.selectData();
    transactions.checkSeries("WorldCo/june", "Salary");
    transactions.checkCategory("WorldCo/june", MasterCategory.INCOME);

    views.selectCategorization();
    categorization.checkSelectedTableRows(0);
    categorization.checkIncomeSeriesIsSelected("Salary");

    categorization.createIncomeSeries()
      .setName("Exceptional Income")
      .setCategory(MasterCategory.INCOME)
      .setAmount("0.0")
      .validate();
    categorization.selectIncomeSeries("Exceptional Income", false);
    categorization.checkIncomeSeriesIsNotSelected("Salary");
  }

  public void testStandardRecurringTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization
      .selectTableRow(0)
      .checkLabel("Free Telecom")
      .selectRecurring()
      .checkContainsNoRecurringSeries()
      .checkNoSeriesMessage("You must create a series")
      .selectRecurringSeries("Internet", MasterCategory.TELECOMS, true)
      .checkNoSeriesMessageHidden();

    views.selectData();
    transactions.checkSeries(0, "Internet");
    transactions.checkCategory(0, MasterCategory.TELECOMS);

    views.selectCategorization();
    categorization.checkSelectedTableRows(0);
    categorization.checkRecurringSeriesIsSelected("Internet");
    categorization.selectNewRecurringSeries("Rental", MasterCategory.HOUSE, true);
    categorization.checkRecurringSeriesIsNotSelected("Internet");
  }

  public void testStandardEnvelopeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .checkLabel("AUCHAN C'EST BON")
      .selectEnvelopes()
      .checkNoSeriesMessage("You must create a series")
      .selectEnvelopeSeries("Courant", MasterCategory.FOOD, true)
      .checkEnvelopeSeriesIsSelected("Courant", MasterCategory.FOOD)
      .checkNoSeriesMessageHidden();

    views.selectData();
    transactions.checkSeries(0, "Courant");
    transactions.checkCategory(0, MasterCategory.FOOD);
  }

  public void testStandardOccasionalTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "Fouquet's")
      .load();

    views.selectData();
    categories.createSubCategory(MasterCategory.FOOD, "Saucisson");
    categories.select(MasterCategory.ALL);

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("Fouquet's");
    categorization.selectOccasional();
    categorization.checkContainsOccasional(MasterCategory.EQUIPMENT,
                                           MasterCategory.CLOTHING,
                                           MasterCategory.BEAUTY,
                                           MasterCategory.EDUCATION);
    categorization.checkContainsOccasional(MasterCategory.FOOD, "Saucisson");
    categorization.selectOccasionalSeries(MasterCategory.FOOD);

    views.selectData();
    transactions.checkSeries(0, "Occasional");
    transactions.checkCategory(0, MasterCategory.FOOD);

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkOccasionalSeries(MasterCategory.FOOD);
    categorization.selectOccasionalSeries(MasterCategory.FOOD, "Saucisson");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Saucisson", "Fouquet's", -199.90},
    });

    views.selectData();
    transactions.checkSeries(0, "Occasional");
    transactions.checkCategory(0, "Saucisson");
  }

  public void testSwitchingTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0, 1);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Free Telecom", -29.90},
    });
    categorization.selectTableRows(0);
    categorization.checkLabel("Auchan");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"30/06/2008", "", "Free Telecom", -29.90},
    });

    categorization.selectTableRows(1);
    categorization.checkLabel("Free Telecom");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    categorization.checkRecurringSeriesIsSelected("Internet");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"30/06/2008", "Internet", "Free Telecom", -29.90},
    });

    categorization.selectTableRows(0);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    categorization.selectTableRow(1);
    categorization.checkRecurringSeriesIsSelected("Internet");
  }

  public void testUnassignedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "LDLC")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("LDLC");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
  }

  public void testSelectingRecurringSelectsBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    categorization.checkBudgetAreaIsSelected(BudgetArea.RECURRING_EXPENSES);
  }

  public void testNoSelection() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Carouf")
      .addTransaction("2008/06/15", -40.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Carouf", -29.90},
    });

    categorization.unselectAllTransactions();
    categorization.checkNoSelectionPanelDisplayed();

    categorization.selectTableRows(0);
    categorization.checkBudgetAreaSelectionPanelDisplayed();
  }

  public void testAutoSelectSimilarTransactionsByDoubleClick() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/06/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/06/24", -29.90, "Free Telecom 21/04")
      .addTransaction("2008/06/15", -90.0, "Cheque 1111")
      .addTransaction("2008/06/14", -80.0, "Cheque 2222")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "CHEQUE N. 1111", -90.00},
      {"14/06/2008", "", "CHEQUE N. 2222", -80.00},
      {"24/06/2008", "", "Free Telecom 21/04", -29.90},
      {"25/06/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.disableAutoHide();

    categorization.getTable().doubleClick(0, 0);
    categorization.checkSelectedTableRows(0);

    categorization.getTable().doubleClick(2, 0);
    categorization.checkSelectedTableRows(2, 3, 4);
  }

  public void testSort() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/27", -29.90, "Free Telecom 27/05")
      .addTransaction("2008/05/15", -90.0, "Auchan 1111")
      .addTransaction("2008/05/14", -80.0, "Auchan 2222")
      .load();

    views.selectCategorization();
    categorization.getTable().getHeader().click(0);
    categorization.checkTable(new Object[][]{
      {"26/06/2008", "", "Free Telecom 26/06", -29.90},
      {"27/05/2008", "", "Free Telecom 27/05", -29.90},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"15/05/2008", "", "Auchan 1111", -90.00},
      {"14/05/2008", "", "Auchan 2222", -80.00},
    });
    categorization.getTable().getHeader().click(0);
    categorization.checkTable(new Object[][]{
      {"14/05/2008", "", "Auchan 2222", -80.00},
      {"15/05/2008", "", "Auchan 1111", -90.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"27/05/2008", "", "Free Telecom 27/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90},
    });
    categorization.getTable().getHeader().click(0);
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan 1111", -90.00},
      {"14/05/2008", "", "Auchan 2222", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90},
      {"27/05/2008", "", "Free Telecom 27/05", -29.90},
    });
  }

  public void testAutomaticSelectionOfSimilarTransactionsMode() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/04/24", -29.90, "Free Telecom 21/04")
      .addTransaction("2008/05/15", -90.0, "Auchan 1111")
      .addTransaction("2008/05/14", -80.0, "Auchan 2222")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan 1111", -90.00},
      {"14/05/2008", "", "Auchan 2222", -80.00},
      {"24/04/2008", "", "Free Telecom 21/04", -29.90},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.disableAutoHide();
    categorization.disableAutoSelectNext();

    categorization.doubleClickTableRow(0);
    categorization.checkLabel(2);

    categorization.checkSelectedTableRows(0, 1);
    categorization.doubleClickTableRow(3);
    categorization.checkSelectedTableRows(2, 3, 4);

    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.doubleClickTableRow("Auchan 1111");
    categorization.checkSelectedTableRows(0, 1);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    views.selectData();
    transactions.initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Groceries", "", -170.00, "Groceries", MasterCategory.FOOD)
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan 1111", "", -90.00, "Groceries", MasterCategory.FOOD)
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Auchan 2222", "", -80.00, "Groceries", MasterCategory.FOOD)
      .add("24/04/2008", TransactionType.PRELEVEMENT, "Free Telecom 21/04", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .check();
  }

  public void testAutomaticSelectionOfSimilarTransactionsAreOnlySelectedInTheVisibleScope() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/04/24", -29.90, "Free Telecom 21/04")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"24/04/2008", "", "Free Telecom 21/04", -29.90},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    timeline.selectMonth("2008/06");
    categorization.doubleClickTableRow(0);
    transactionDetails.checkLabel("Free Telecom 26/06");
    categorization.setRecurring(0, "Internet", MasterCategory.TELECOMS, true);

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90)
      .add("24/04/2008", TransactionType.PRELEVEMENT, "Free Telecom 21/04", "", -29.90)
      .check();
  }

  public void testAutomaticSelectionExcludesChecks() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -12.90, "Cheque 12345")
      .addTransaction("2008/05/25", -34.90, "Cheque 23456")
      .addTransaction("2008/04/24", -56.90, "Cheque 34556")
      .load();

    views.selectCategorization();
    categorization.doubleClickTableRow(0);
    categorization.checkSelectedTableRows(0);
  }

  public void testAutoHideCategorizedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkAutoHideEnabled(false);
    categorization.enableAutoHide();

    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    categorization.checkTable(new Object[][]{
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.selectTableRows(1, 2);
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.checkTable(new Object[][]{
      {"14/05/2008", "", "Carouf", -80.00},
    });

    categorization.selectTableRows(0);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, false);

    categorization.checkTableIsEmpty();

    views.selectData();
    transactions
      .initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Groceries", "", -90.00, "Groceries", MasterCategory.FOOD)
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "Groceries", MasterCategory.FOOD)
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Carouf", "", -80.00, "Groceries", MasterCategory.FOOD)
      .check();
  }

  public void testAutoHideActivation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.disableAutoHide();

    categorization.selectTableRows(0, 1);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "Groceries", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkSelectedTableRows(0, 1);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);

    categorization.enableAutoHide();
    categorization.checkTable(new Object[][]{
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkNoSelectedTableRows();

    categorization.disableAutoHide();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "Groceries", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkNoSelectedTableRows();
  }

  public void testAutoSelectNext() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkAutoSelectNextEnabled(false);
    categorization.enableAutoSelectNext();
    categorization.disableAutoHide();

    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    categorization.checkSelectedTableRows(1);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, false);

    categorization.checkSelectedTableRows(2);
    categorization.selectTableRows(2, 3);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "Groceries", "Carouf", -80.00},
      {"25/05/2008", "Internet", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Internet", "Free Telecom 26/06", -29.90}
    });
    categorization.checkNoTransactionSelected();

    views.selectData();
    transactions
      .initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Groceries", "", -90.00, "Groceries", MasterCategory.FOOD)
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "Groceries", MasterCategory.FOOD)
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Carouf", "", -80.00, "Groceries", MasterCategory.FOOD)
      .check();
  }

  public void testAutoSelectNextDisabled() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.disableAutoSelectNext();
    categorization.disableAutoHide();

    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    categorization.checkSelectedTableRows(0);
  }

  public void testAutoSelectNextSkipsCategorizedItems() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();
    categorization.enableAutoSelectNext();
    categorization.disableAutoHide();

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.doubleClickTableRow(1);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    categorization.checkSelectedTableRow(2);

    categorization.doubleClickTableRow(0);
    categorization.checkSelectedTableRows(0);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.checkSelectedTableRows(2);
  }

  public void testAutoSelectNextUnselectsAllAfterLastCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -50, "France Telecom")
      .addTransaction("2008/06/30", -60, "SFR 3G")
      .load();

    timeline.selectAll();
    views.selectCategorization();

    categorization.enableAutoSelectNext();
    categorization.disableAutoHide();

    categorization.checkTable(new Object[][]{
      {"28/06/2008", "", "France Telecom", -50.00},
      {"30/06/2008", "", "SFR 3G", -60.00},
    });

    categorization.selectTableRows(0, 1);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.checkNoSelectedTableRows();
    categorization.checkNoSelectionPanelDisplayed();
  }

  public void testAutoSelectWraps() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .load();

    views.selectCategorization();
    categorization.enableAutoSelectNext();
    categorization.disableAutoHide();

    categorization.doubleClickTableRow(2);
    categorization.checkSelectedTableRows(2, 3);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    categorization.checkSelectedTableRows(0);
  }

  public void testSeriesList() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.disableAutoHide();

    categorization.selectIncome()
      .checkNoIncomeSeriesDisplayed()
      .checkEditIncomeSeriesDisabled();

    categorization
      .createIncomeSeries()
      .setName("Salary")
      .setCategory(MasterCategory.INCOME)
      .validate();

    categorization.checkContainsIncomeSeries("Salary");
  }

  public void testEditingOccasionalCategories() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.editOccasionalCategories()
      .selectMaster(MasterCategory.FOOD)
      .createSubCategory("Apero")
      .validate();

    categorization.checkContainsOccasional(MasterCategory.FOOD, "Apero");

    categorization.editOccasionalCategories()
      .selectMaster(MasterCategory.FOOD)
      .selectSub("Apero")
      .deleteSubCategory()
      .validate();

    categorization.checkDoesNotContainOccasional(MasterCategory.FOOD, "Apero");
  }

  public void testFiltersSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/30", -50.00, "Monoprix")
      .addTransaction("2008/06/30", -95.00, "Auchan")
      .addTransaction("2008/05/29", -29.00, "ED")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries().setName("courantED")
      .setEndDate(200805)
      .setCategory(MasterCategory.FOOD)
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.envelopes.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .setCategory(MasterCategory.FOOD)
      .validate();
    budgetView.envelopes.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .setCategory(MasterCategory.FOOD)
      .validate();
    views.selectCategorization();
    categorization.selectTableRows("ED");
    categorization.selectEnvelopes()
      .checkContainsEnvelope("courantED", MasterCategory.FOOD)
      .checkNotContainsEnvelope("courantAuchan")
      .checkNotContainsEnvelope("courantMonoprix");

    categorization.selectTableRows("Auchan", "Monoprix");
    categorization.selectEnvelopes()
      .checkContainsEnvelope("courantMonoprix", MasterCategory.FOOD)
      .checkNotContainsEnvelope("courantED")
      .checkNotContainsEnvelope("courantAuchan");

    categorization.selectTableRows("Auchan", "ED");
    categorization.selectEnvelopes()
      .checkNotContainsEnvelope("courantED", "courantAuchan", "courantMonoprix");

    categorization.selectTableRows("Auchan");
    categorization.selectEnvelopes()
      .checkContainsEnvelope("courantAuchan", MasterCategory.FOOD)
      .checkContainsEnvelope("courantMonoprix", MasterCategory.FOOD)
      .checkNotContainsEnvelope("courantED");

    categorization.setEnvelope("Monoprix", "courantMonoprix", MasterCategory.FOOD, false);
    categorization.setEnvelope("Auchan", "courantAuchan", MasterCategory.FOOD, false);
    categorization.setEnvelope("ED", "courantED", MasterCategory.FOOD, false);
  }

  public void testRemovingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/30", -50.00, "Monoprix")
      .load();
    views.selectCategorization();

    categorization.selectTableRows("Monoprix");
    categorization
      .selectEnvelopes()
      .createEnvelopeSeries()
      .setName("series1")
      .setCategory(MasterCategory.FOOD)
      .validate();
    categorization
      .selectOccasionalSeries(MasterCategory.TELECOMS);

    views.selectData();
    transactions.checkSeries("Monoprix", "Occasional");
    views.selectCategorization();
    categorization.selectEnvelopes()
      .editSeries(false)
      .selectSeries("series1")
      .deleteSeries()
      .validate();
  }
}
