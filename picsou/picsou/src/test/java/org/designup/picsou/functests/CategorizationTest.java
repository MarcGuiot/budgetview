package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;

public class CategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/06/30");
    super.setUp();
  }

  public void testStandardIncomeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .checkLabel("WORLDCO/JUNE")
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
      .switchToManual()
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
      .checkLabel("FREE TELECOM")
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
    categorization.checkLabel("FOUQUET'S");
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
      .addTransaction("2008/06/30", -29.90, "Free")
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0, 1);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
    });
    categorization.selectTableRow(0);
    categorization.checkLabel("AUCHAN");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
    });

    categorization.selectTableRow(2);
    categorization.checkLabel("FREE");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    categorization.checkRecurringSeriesIsSelected("Internet");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRow(1);
    categorization.checkLabel("FRANCE TELECOM");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring();
    categorization.checkNoRecurringSeriesSelected();
    categorization.selectRecurringSeries("Internet", MasterCategory.TELECOMS, false);
    categorization.checkRecurringSeriesIsSelected("Internet");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Internet", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRow(0);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    categorization.selectTableRow(1);
    categorization.checkRecurringSeriesIsSelected("Internet");
  }

  public void testAssigningSeveralTransactionsAtOnce() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free")
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .addTransaction("2008/06/15", -60, "Monops")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
      {"15/06/2008", "", "Monops", -60.00},
    });

    categorization.selectTableRows(1, 2);
    categorization.selectRecurring();
    categorization.selectRecurringSeries("Comm", MasterCategory.TELECOMS, true);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "Comm", "France Telecom", -59.90},
      {"30/06/2008", "Comm", "Free", -29.90},
      {"15/06/2008", "", "Monops", -60.00},
    });

    categorization.selectTableRows(0, 3);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Comm", "France Telecom", -59.90},
      {"30/06/2008", "Comm", "Free", -29.90},
      {"15/06/2008", "Groceries", "Monops", -60.00},
    });

    categorization.selectTableRows(1, 2);
    categorization.checkRecurringSeriesIsSelected("Comm");

    categorization.selectTableRows(0, 3);
    categorization.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
  }

  public void testSelectingASeriesInABudgetAreaUnselectsPreviousSeriesInOtherBudgetAreas() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .load();

    views.selectCategorization();
    categorization.setRecurring("France Telecom", "Telephone", MasterCategory.TELECOMS, true);
    categorization.checkRecurringSeriesIsSelected("Telephone");

    categorization.selectEnvelopes();
    categorization.setEnvelope("France Telecom", "Phone", MasterCategory.TELECOMS, true);

    categorization.selectRecurring();
    categorization.checkRecurringSeriesIsNotSelected("Telephone");
    categorization.setRecurring("France Telecom", "Telephone", MasterCategory.TELECOMS, false);

    categorization.selectEnvelopes();
    categorization.checkEnvelopeSeriesNotSelected("Phone", MasterCategory.TELECOMS);
  }

  public void testRevertingTransactionsToUncategorized() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free")
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setRecurring("Free", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("France Telecom", "Telephone", MasterCategory.TELECOMS, true);
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Telephone", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRow(0);
    categorization.selectUncategorized();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Telephone", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.setUncategorized();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "Telephone", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRows(1, 2);
    categorization.setUncategorized();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
    });
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
    categorization.createRecurringSeries()
      .checkName("FREE TELECOM")
      .setName("Internet")
      .setCategories(MasterCategory.TELECOMS)
      .validate();
    categorization.checkBudgetAreaIsSelected(BudgetArea.RECURRING);
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

  public void testTransactionFilteringMode() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/30", -29.90, "Carouf")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/15", -40.00, "Auchan")
      .addTransaction("2008/06/17", -12.00, "MacDo")
      .load();

    views.selectCategorization();

    categorization.checkShowsAllTransactions();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"30/07/2008", "", "Carouf", -29.90},
      {"17/06/2008", "", "MacDo", -12.00}
    });

    timeline.selectMonth("2008/07");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"30/07/2008", "", "Carouf", -29.90},
      {"17/06/2008", "", "MacDo", -12.00}
    });

    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"30/07/2008", "", "Carouf", -29.90}
    });

    categorization.showLastImportedFileOnly();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"17/06/2008", "", "MacDo", -12.00}
    });

    categorization.setOccasional("MacDo", MasterCategory.FOOD);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"17/06/2008", "Groceries", "MacDo", -12.00}
    });

    categorization.showUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"30/07/2008", "", "Carouf", -29.90}
    });
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
      {"15/06/2008", "", "CHEQUE N°1111", -90.00},
      {"14/06/2008", "", "CHEQUE N°2222", -80.00},
      {"24/06/2008", "", "Free Telecom 21/04", -29.90},
      {"25/06/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.getTable().doubleClick(0, 0);
    categorization.checkSelectedTableRows(0);

    categorization.getTable().doubleClick(2, 0);
    categorization.checkSelectedTableRows(2, 3, 4);
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

    categorization.doubleClickTableRow(0);
    categorization.checkLabel("AUCHAN [2 operations]");

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
    timeline.selectAll();
    transactions.initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Planned: Groceries", "", -170.00, "Groceries", MasterCategory.FOOD)
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
    timeline.selectMonth("2008/06");
    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.doubleClickTableRow(0);
    transactionDetails.checkLabel("FREE TELECOM 26/06");
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
    categorization.showUncategorizedTransactionsOnly();

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
    timeline.selectAll();
    transactions
      .initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Planned: Groceries", "", -170.00, "Groceries", MasterCategory.FOOD)
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

    categorization.showUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkNoSelectedTableRows();

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "Groceries", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkNoSelectedTableRows();
  }

  public void testEditingSingleCategorySeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", 1000, "Salaire")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);

    categorization.selectIncome()
      .checkNoIncomeSeriesDisplayed()
      .checkEditIncomeSeriesDisabled();

    categorization
      .createIncomeSeries()
      .setName("Salary")
      .setCategory(MasterCategory.INCOME)
      .createSeries()
      .setName("Other salary")
      .setCategory(MasterCategory.INCOME)
      .validate();

    categorization.checkContainsIncomeSeries("Salary");
    categorization.selectIncome().selectIncomeSeries("Salary", false);
    categorization.editSeries("Salary", true).checkSeriesSelected("Salary").cancel();

    categorization.selectIncomeSeries("Other salary", false);
    categorization.editSeries("Other salary", true).checkSeriesSelected("Other salary").cancel();
  }

  public void testEditingMultiCategoriesSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", 1000, "FNAC")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);

    categorization.selectEnvelopes();
    categorization
      .createEnvelopeSeries()
      .checkName(Lang.get("seriesEdition.newSeries"))
      .setName("Music")
      .setCategory(MasterCategory.LEISURES)
      .validate();

    categorization.checkContainsEnvelope("Music", MasterCategory.LEISURES);
    categorization.editSeries(true).checkSeriesSelected("Music").cancel();

    categorization.editSeries("Music", false)
      .checkName("Music")
      .setName("CDs")
      .validate();

    categorization.checkContainsEnvelope("CDs", MasterCategory.LEISURES);
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
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.envelopes.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .setCategory(MasterCategory.FOOD)
      .validate();
    budgetView.envelopes.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .switchToManual()
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
      .deleteSelectedSeries()
      .validate();
  }

  public void testAutomaticBudget() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/20", -20, "Auchan")
      .addTransaction("2008/06/10", -10, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -20, -20);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -10, -20);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -20);

    OfxBuilder
      .init(this)
      .addTransaction("2008/04/10", -100, "ATAC")
      .addTransaction("2008/05/10", -10, "ED")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    categorization.setEnvelope("ATAC", "Courant", MasterCategory.FOOD, false);

    views.selectBudget();
    timeline.selectMonth("2008/04");
    budgetView.envelopes.checkSeries("Courant", -100, -100);
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -30, -100);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -10, -30);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -30);

    views.selectCategorization();
    timeline.selectMonth("2008/05");
    categorization.setOccasional("ED", MasterCategory.FOOD);

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -20, -100);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -10, -20);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -20);
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Occasional", "", -10.00, "Occasional")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Occasional", "", -10.00, "Occasional")
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("10/06/2008", TransactionType.PLANNED, "Planned: Occasional", "", -10.00, "Occasional")
      .add("10/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -10.00, "Courant", MasterCategory.FOOD)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00, "Courant", MasterCategory.FOOD)
      .add("20/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("10/05/2008", TransactionType.PRELEVEMENT, "ED", "", -10.00, "Occasional", MasterCategory.FOOD)
      .add("10/04/2008", TransactionType.PRELEVEMENT, "ATAC", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();
  }

  public void testAutomaticInCurrentMonth() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -30, -30);

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "ATAC")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("ATAC", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -30, -10);
  }

  public void testAutomaticWithInactiveMonth() throws Exception {
    views.selectBudget();
    budgetView.recurring.createSeries().setName("Tel")
      .setCustom()
      .toggleMonth(1, 3, 5, 7, 9, 11)
      .setCategory(MasterCategory.TELECOMS)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/04/20", -10, "FT")
      .load();
    views.selectCategorization();
    categorization.setRecurring("FT", "Tel", MasterCategory.TELECOMS, false);
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeriesNotPresent("Tel");
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Tel", 0, -10);
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -10, "FT")
      .load();
    budgetView.recurring.checkSeries("Tel", -10, -10);
  }

  public void testInAutomaticBudgetOverrunInCurrentUpdateFuture() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -20, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -20);
    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", 0, -20);

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.selectTableRows("ED");
    transactionDetails.split("5", "DVD");
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -15, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -15);
    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", 0, -15);

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.selectTableRows(categorization.getTable()
      .getRowIndex(CategorizationChecker.AMOUNT_COLUMN_INDEX, -15.0));
    transactionDetails.split("10", "CD");
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
  }

  public void testInAutomaticUpdateImmediatelyPreviousFromCurrentImpactFutur() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    categorization.selectTableRows("ED");
    transactionDetails.split("15", "DVD");
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -10);

    views.selectCategorization();
    timeline.selectMonth("2008/05");
    categorization.selectTableRows("Auchan");
    transactionDetails.split("9", "DVD");
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -1, -1);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -5, -1);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -5);

    views.selectCategorization();
    timeline.selectMonth("2008/05");
    categorization.selectTableRows(
      categorization.getTable().getRowIndex(CategorizationChecker.LABEL_COLUMN_INDEX, "AUCHAN"));
    transactionDetails.openSplitDialog().deleteRow(1).validate();

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
  }

  public void testAutomaticShouldNotTakeInAccountPreviousEmptyMonthWhenPositifBudget() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.income.createSeries().setName("Revenue")
      .setCategory(MasterCategory.INCOME)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 10, "revenue 2")
      .addTransaction("2008/06/20", 20, "revenue 1")
      .load();
    views.selectCategorization();
    categorization.setIncome("revenue 1", "Revenue", false);
    views.selectData();
    timeline.selectAll();
    transactions
      .initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Revenue", "", 20.00, "Revenue", MasterCategory.INCOME)
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Revenue", "", 20.00, "Revenue", MasterCategory.INCOME)
      .add("20/06/2008", TransactionType.VIREMENT, "revenue 1", "", 20.00, "Revenue", MasterCategory.INCOME)
      .add("10/05/2008", TransactionType.VIREMENT, "revenue 2", "", 10.00)
      .check();
  }

  public void testAutomaticShouldNotTakeInAccountPreviousEmptyMonth() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    views.selectData();
    timeline.selectAll();
    transactions
      .initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("10/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00)
      .check();
  }

  public void testInAutomaticNewMonthUpdateFuture() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/04/10", -10, "Auchan")
      .addTransaction("2008/05/10", -5, "ATAC")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);
    categorization.setEnvelope("ATAC", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();
    timeline.selectMonth("2008/04");
    budgetView.envelopes.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", 0, -10);
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -20, "Auchan")
      .load();
    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("Courant", -20, -5);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", 0, -20);
    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", 0, -20);
  }

  public void testAutoCategoriseWithFirstSelectedCategory() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectEnvelopes()
      .createEnvelopeSeries()
      .setName("Divers")
      .setCategories(MasterCategory.EDUCATION, MasterCategory.EQUIPMENT)
      .validate();

    views.selectData();
    transactions.checkSeries("Auchan", "Divers");
    transactions.checkCategory("Auchan", MasterCategory.EDUCATION);
  }

  public void testHelpForUncategorizedBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.clickHelpLink("categorization")
      .checkTitle("Categorization")
      .close();
  }

  public void testCreatedSavingTransactionAreNotVisibleInCategorization() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne")
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .validate();

    categorization.initContent()
      .add("10/08/2008", "Epargne", "Virement", -100)
      .check();
  }

  public void testCanNotChangeCategoryAlreadyAssociatedToTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -59.90, "Auchan")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.selectTableRows("Auchan");
    SeriesEditionDialogChecker seriesChecker = categorization.editSeries(false);
    seriesChecker
      .openCategory()
      .checkNotUncheckable(getCategoryName(MasterCategory.FOOD))
      .cancel();
    seriesChecker.validate();
  }

  public void testCreateSerieShouldNotCategorizeToTransactionIfNotValide() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .selectEnvelopes()
      .createEnvelopeSeries()
      .setName("Courses")
      .setEndDate(200805)
      .setCategories(MasterCategory.FOOD)
      .validate();

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("25/06/2008", TransactionType.PRELEVEMENT, "1_AUCHAN", "", -50.00)
      .add("15/05/2008", TransactionType.PRELEVEMENT, "2_AUCHAN", "", -40.00)
      .check();
  }

  public void testEditSerieUpdateInCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courses")
      .setCategories(MasterCategory.FOOD)
      .validate();

    views.selectCategorization();

    timeline.selectMonth("2008/06");

    categorization
      .showSelectedMonthsOnly()
      .selectTableRow(0)
      .selectEnvelopes()
      .editSeries("Courses", false)
      .setEndDate(200805)
      .validate();
    categorization.checkNotContainsEnvelope("Courses");

  }

  public void testFilteringSerieByValidMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courses 2")
      .setCategories(MasterCategory.FOOD)
      .setCustom()
      .toggleMonth(6)
      .setStartDate(200805)
      .validate();

    budgetView.envelopes.createSeries()
      .setName("Courses 1")
      .setCategories(MasterCategory.FOOD)
      .setCustom()
      .toggleMonth(5)
      .validate();

    views.selectCategorization();
    
    categorization
      .selectTableRows("1_Auchan")
      .selectEnvelopes()
      .checkNotContainsEnvelope("Courses 2")
      .selectEnvelopeSeries("Courses 1", MasterCategory.FOOD, false);

    categorization
      .selectTableRows("2_Auchan")
      .selectEnvelopes()
      .checkNotContainsEnvelope("Courses 1")
      .selectEnvelopeSeries("Courses 2", MasterCategory.FOOD, false);
  }

  public void testDoNotFilterValidMonthIfMonthIsUncheckedButWithAlreadyCategorizedOperations() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/06/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courses")
      .setCategories(MasterCategory.FOOD)
      .setCustom()
      .setStartDate(200804)
      .validate();

    views.selectCategorization();

    categorization
      .selectTableRows("1_Auchan")
      .selectEnvelopes()
      .selectEnvelopeSeries("Courses", MasterCategory.FOOD, false);

    categorization.editSeries("Courses", false)
      .toggleMonth(6)
      .validate();

    categorization
      .selectTableRows("2_Auchan")
      .selectEnvelopes()
      .selectEnvelopeSeries("Courses", MasterCategory.FOOD, false);
  }

  public void testDeleteTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/06/15", -40.0, "2_Auchan")
      .load();

    views.selectCategorization();
    categorization.delete("1_Auchan")
      .validate();
    categorization.initContent()
      .add("15/06/2008", "2_Auchan", -40.0)
      .check();
  }
}
