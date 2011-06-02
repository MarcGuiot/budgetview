package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.TransactionType;

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

    categorization.selectTableRow(0)
      .checkLabel("WORLDCO/JUNE")
      .selectIncome()
      .checkDescriptionDisplayed()
      .checkNoSeriesMessage("You must create a series")
      .selectNewSeries("Salary", "My income")
      .checkNoSeriesMessageHidden();

    transactions.checkSeries("WorldCo/june", "Salary");

    categorization.checkSelectedTableRows(0);
    categorization.checkIncomeSeriesIsSelected("Salary");
    categorization.getIncome().checkSeriesTooltip("Salary", "My income");

    categorization.selectIncome().createSeries()
      .setName("Exceptional Income")
      .setAmount("0.0")
      .validate();
    categorization.selectIncome()
      .selectSeries("Exceptional Income")
      .checkSeriesNotSelected("Salary");
  }

  public void testStandardRecurringTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    categorization
      .selectTableRow(0)
      .checkLabel("FREE TELECOM")
      .selectRecurring()
      .checkDescriptionDisplayed()
      .checkContainsNoSeries()
      .checkNoSeriesMessage("You must create a series")
      .selectNewSeries("Internet", "WWW connection")
      .checkNoSeriesMessageHidden();

    transactions.checkSeries(0, "Internet");

    categorization.checkSelectedTableRows(0);
    categorization.checkRecurringSeriesIsSelected("Internet");
    categorization.getRecurring().checkSeriesTooltip("Internet", "WWW connection");
    categorization.selectRecurring()
      .selectNewSeries("Rental")
      .checkSeriesNotSelected("Internet");
  }

  public void testStandardEnvelopeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    categorization.selectTableRows(0)
      .checkLabel("AUCHAN C'EST BON")
      .selectVariable()
      .checkDescriptionDisplayed()
      .checkNoSeriesMessage("You must create a series")
      .selectNewSeries("Courant")
      .checkSeriesIsSelected("Courant")
      .checkNoSeriesMessageHidden();

    transactions.checkSeries(0, "Courant");
  }

  public void testSwitchingTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free")
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    categorization.selectTableRows(0, 1);
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
    });
    categorization.selectTableRow(0);
    categorization.checkLabel("AUCHAN");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectVariable().selectNewSeries("Groceries");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
    });

    categorization.selectTableRow(2);
    categorization.checkLabel("FREE");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring().selectNewSeries("Internet");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRow(1);
    categorization.checkLabel("FRANCE TELECOM");
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring()
      .checkNoSeriesSelected()
      .selectSeries("Internet");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Internet", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.selectTableRow(0);
    categorization.getVariable().checkSeriesIsSelected("Groceries");
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

    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "", "France Telecom", -59.90},
      {"30/06/2008", "", "Free", -29.90},
      {"15/06/2008", "", "Monops", -60.00},
    });

    categorization.selectTableRows(1, 2);
    categorization.selectRecurring().selectNewSeries("Comm");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "", "Auchan", -40.00},
      {"25/06/2008", "Comm", "France Telecom", -59.90},
      {"30/06/2008", "Comm", "Free", -29.90},
      {"15/06/2008", "", "Monops", -60.00},
    });

    categorization.selectTableRows(0, 3);
    categorization.selectVariable().selectNewSeries("Groceries");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Comm", "France Telecom", -59.90},
      {"30/06/2008", "Comm", "Free", -29.90},
      {"15/06/2008", "Groceries", "Monops", -60.00},
    });

    categorization.selectTableRows(1, 2);
    categorization.checkRecurringSeriesIsSelected("Comm");

    categorization.selectTableRows(0, 3);
    categorization.getVariable().checkSeriesIsSelected("Groceries");
  }

  public void testSelectingASeriesInABudgetAreaUnselectsPreviousSeriesInOtherBudgetAreas() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("France Telecom", "Telephone");
    categorization.checkRecurringSeriesIsSelected("Telephone");

    categorization.setNewVariable("France Telecom", "Phone");

    categorization.selectRecurring().checkSeriesNotSelected("Telephone");
    categorization.setRecurring("France Telecom", "Telephone");

    categorization.selectVariable();
    categorization.getVariable().checkSeriesNotSelected("Phone");
  }

  public void testRevertingTransactionsToUncategorized() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free")
      .addTransaction("2008/06/25", -59.90, "France Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("France Telecom", "Telephone");
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Telephone", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.checkTable(new Object[][]{
      {"15/06/2008", "Groceries", "Auchan", -40.00},
      {"25/06/2008", "Telephone", "France Telecom", -59.90},
      {"30/06/2008", "Internet", "Free", -29.90},
    });

    categorization.setUncategorized(0);
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
    categorization.selectRecurring().createSeries()
      .checkName("FREE TELECOM")
      .setName("Internet")
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
      .addTransaction("2008/06/30", -29.90, "Carouf")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/15", -40.00, "Auchan")
      .addTransaction("2008/03/17", -12.00, "MacDo")
      .load();

    views.selectCategorization();
    categorization.checkShowsAllTransactions();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Carouf", -29.90},
      {"17/03/2008", "", "MacDo", -12.00}
    });

    timeline.selectMonth("2008/06");
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Carouf", -29.90},
      {"17/03/2008", "", "MacDo", -12.00}
    });

    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "Carouf", -29.90}
    });

    categorization.showUncategorizedTransactionsForSelectedMonths();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "Carouf", -29.90},
    });

    timeline.selectMonths("2008/05", "2008/06");
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Carouf", -29.90},
    });

    timeline.selectMonths("2008/06");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "Carouf", -29.90},
    });

    categorization.showLastImportedFileOnly();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"17/03/2008", "", "MacDo", -12.00}
    });

    categorization.setNewVariable("MacDo", "Food");
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"17/03/2008", "Food", "MacDo", -12.00}
    });

    categorization.showUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"30/06/2008", "", "Carouf", -29.90}
    });

    categorization.showUncategorizedTransactionsForSelectedMonths();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "Carouf", -29.90}
    });

    categorization.setVariable("Carouf", "Food");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Food", "Carouf", -29.90}
    });

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "Auchan", -40.00},
      {"30/06/2008", "Food", "Carouf", -29.90},
      {"17/03/2008", "Food", "MacDo", -12.00}
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

    categorization.selectRecurring().selectNewSeries("Internet");

    categorization.doubleClickTableRow("Auchan 1111");
    categorization.checkSelectedTableRows(0, 1);
    categorization.selectVariable().selectNewSeries("Groceries", -170.0);

    views.selectData();
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Planned: Groceries", "", -170.00, "Groceries")
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet")
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet")
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan 1111", "", -90.00, "Groceries")
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Auchan 2222", "", -80.00, "Groceries")
      .add("24/04/2008", TransactionType.PRELEVEMENT, "Free Telecom 21/04", "", -29.90, "Internet")
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
    categorization.setNewRecurring(0, "Internet");

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet")
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

    categorization.setNewVariable(0, "Groceries", -170);

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });

    categorization.selectTableRows(2, 3);
    categorization.checkBudgetAreaSelectionPanelDisplayed();
    categorization.selectRecurring().selectNewSeries("Internet");

    categorization.checkTable(new Object[][]{
      {"14/05/2008", "", "Carouf", -80.00},
      {"25/05/2008", "Internet", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Internet", "Free Telecom 26/06", -29.90}
    });

    categorization.setVariable(0, "Groceries");

    categorization.checkTable(new Object[][]{
      {"14/05/2008", "Groceries", "Carouf", -80.00}});

    views.selectData();
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("26/06/2008", TransactionType.PLANNED, "Planned: Groceries", "", -170.00, "Groceries")
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet")
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet")
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "Groceries")
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Carouf", "", -80.00, "Groceries")
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
    categorization.selectVariable().selectNewSeries("Groceries");

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "Groceries", "Auchan", -90.00},
      {"14/05/2008", "Groceries", "Carouf", -80.00},
      {"25/05/2008", "", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "", "Free Telecom 26/06", -29.90}
    });
    categorization.checkSelectedTableRows(0, 1);
    categorization.getVariable().checkSeriesIsSelected("Groceries");

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

  public void testFiltersSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/30", -50.00, "Monoprix")
      .addTransaction("2008/06/30", -95.00, "Auchan")
      .addTransaction("2008/05/29", -29.00, "ED")
      .load();

    budgetView.variable.createSeries().setName("courantED")
      .setEndDate(200805)
      .selectAllMonths()
      .setAmount("100")
      .setEndDate(200805)
      .validate();
    budgetView.variable.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .setStartDate(200806)
      .setEndDate(200806)
      .validate();
    budgetView.variable.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .setStartDate(200806)
      .validate();

    categorization.selectTransactions("ED");
    categorization.selectVariable()
      .checkContainsSeries("courantED")
      .checkDoesNotContainSeries("courantAuchan", "courantMonoprix");

    categorization.selectTransactions("Auchan", "Monoprix");
    categorization.selectVariable()
      .checkContainsSeries("courantMonoprix")
      .checkDoesNotContainSeries("courantED", "courantAuchan");

    categorization.selectTransactions("Auchan", "ED");
    categorization.selectVariable()
      .checkDoesNotContainSeries("courantED", "courantAuchan", "courantMonoprix");

    categorization.selectTransactions("Auchan");
    categorization.selectVariable()
      .checkContainsSeries("courantAuchan")
      .checkContainsSeries("courantMonoprix")
      .checkDoesNotContainSeries("courantED");
  }

  public void testRemovingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/30", -50.00, "Monoprix")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("Monoprix");
    categorization.selectVariable().createSeries("series1");
    categorization.selectExtras().selectNewSeries("Occasional");

    views.selectData();
    transactions.checkSeries("Monoprix", "Occasional");

    views.selectCategorization();
    categorization.selectVariable()
      .editSeries("series1")
      .deleteCurrentSeries();
    categorization.getVariable()
      .checkDoesNotContainSeries("series1");
  }

  public void testAutomaticBudget() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectBudget();
    budgetView.recurring.createSeries().setName("Courant")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/20", -20, "Auchan")
      .addTransaction("2008/06/10", -10, "Auchan")
      .load();

    categorization.setRecurring("Auchan", "Courant");

    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -20, -20);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -10, -20);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -20);

    OfxBuilder
      .init(this)
      .addTransaction("2008/04/10", -100, "ATAC")
      .addTransaction("2008/05/10", -10, "ED")
      .load();

    categorization.setRecurring("ED", "Courant");
    categorization.setRecurring("ATAC", "Courant");

    timeline.selectMonth("2008/04");
    budgetView.recurring.checkSeries("Courant", -100, -100);
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -30, -100);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -10, -30);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -30);

    timeline.selectMonth("2008/05");
    categorization.setNewExtra("ED", "Occasional");

    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -20, -100);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -10, -20);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -20);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("19/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("19/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("19/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -10.00, "Courant")
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00, "Courant")
      .add("20/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -20.00, "Courant")
      .add("10/05/2008", TransactionType.PRELEVEMENT, "ED", "", -10.00, "Occasional")
      .add("10/04/2008", TransactionType.PRELEVEMENT, "ATAC", "", -100.00, "Courant")
      .check();
  }

  public void testAutomaticInCurrentMonth() throws Exception {

    views.selectBudget();
    budgetView.recurring.createSeries().setName("Courant")
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setRecurring("ED", "Courant");
    categorization.setRecurring("Auchan", "Courant");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -30, -30);

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "ATAC")
      .load();
    views.selectCategorization();
    categorization.setRecurring("ATAC", "Courant");
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -30, -10);
  }

  public void testAutomaticWithInactiveMonth() throws Exception {
    views.selectBudget();
    budgetView.recurring.createSeries().setName("Tel")
      .setCustom()
      .toggleMonth(1, 3, 5, 7, 9, 11)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/04/20", -10, "FT")
      .load();
    views.selectCategorization();
    categorization.setRecurring("FT", "Tel");
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeriesNotPresent("Tel");
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Tel", 0, -10);
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -10, "FT")
      .load();
    views.selectBudget();
    budgetView.recurring.checkSeries("Tel", -10, -10);
  }

  public void testInAutomaticBudgetOverrunInCurrentUpdateFuture() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("Auchan", "Courant");
    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", 0, -10);

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.setRecurring("ED", "Courant");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -20, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -20);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", 0, -20);

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.selectTransactions("ED");
    transactionDetails.split("5", "DVD");
    categorization.selectExtras().selectNewSeries("Leisures");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -15, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -15);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", 0, -15);

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.selectTableRows(categorization.getTable()
                                     .getRowIndex(CategorizationChecker.AMOUNT_COLUMN_INDEX, -15.0));
    transactionDetails.split("10", "CD");
    categorization.selectExtras().selectSeries("Leisures");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -10);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", 0, -10);
  }

  public void testInAutomaticUpdateImmediatelyPreviousFromCurrentImpactFutur() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();
    views.selectCategorization();
    categorization.setNewRecurring("Auchan", "Courant");
    categorization.setRecurring("ED", "Courant");

    categorization.selectTransactions("ED");
    transactionDetails.split("15", "DVD");
    categorization.selectRecurring().selectNewSeries("Leisures");

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -10);

    views.selectCategorization();
    timeline.selectMonth("2008/05");
    categorization.selectTransactions("Auchan");
    transactionDetails.split("9", "DVD");
    categorization.selectExtras().selectNewSeries("Leisures");

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -1, -1);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -5, -1);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -5);

    views.selectCategorization();
    timeline.selectMonth("2008/05");
    categorization.selectTableRows(
      categorization.getTable().getRowIndex(CategorizationChecker.LABEL_COLUMN_INDEX, "AUCHAN"));
    transactionDetails.openSplitDialog().deleteRow(1).validateAndClose();

    views.selectBudget();
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -10);
  }

  public void testAutomaticShouldNotTakeInAccountPreviousEmptyMonthWhenPositiveBudget() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    budgetView.income.createSeries("Revenue");
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 10, "revenue 2")
      .addTransaction("2008/06/20", 20, "revenue 1")
      .load();

    categorization.setIncome("revenue 1", "Revenue");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("19/08/2008", TransactionType.PLANNED, "Planned: Revenue", "", 20.00, "Revenue")
      .add("19/07/2008", TransactionType.PLANNED, "Planned: Revenue", "", 20.00, "Revenue")
      .add("20/06/2008", TransactionType.VIREMENT, "revenue 1", "", 20.00, "Revenue")
      .add("10/05/2008", TransactionType.VIREMENT, "revenue 2", "", 10.00)
      .check();
  }

  public void testAutomaticShouldNotTakeInAccountPreviousEmptyMonth() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();
    budgetView.recurring.createSeries("Courant");

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", -10, "Auchan")
      .addTransaction("2008/06/20", -20, "ED")
      .load();

    categorization.setRecurring("ED", "Courant");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("19/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("19/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courant")
      .add("10/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00)
      .check();
  }

  public void testInAutomaticNewMonthUpdateFuture() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/04/10", -10, "Auchan")
      .addTransaction("2008/05/10", -5, "ATAC")
      .load();

    categorization.setNewRecurring("Auchan", "Courant");
    categorization.setRecurring("ATAC", "Courant");

    timeline.selectMonth("2008/04");
    budgetView.recurring.checkSeries("Courant", -10, -10);
    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", 0, -10);

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -20.00, "Auchan")
      .load();

    timeline.selectMonth("2008/05");
    budgetView.recurring.checkSeries("Courant", -5, -10);
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Courant", -20, -5);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", 0, -20);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", 0, -20);
  }

  public void testCreatedSavingsTransactionsAreNotVisibleInCategorization() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings();

    categorization.createAccount()
      .setAccountName("Epargne")
      .selectBank("ING Direct")
      .checkIsSavings()
      .checkAccountTypeNotEditable()
      .setPosition(1000.0)
      .validate();

    categorization
      .selectSavings()
      .createSeries()
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .setName("Epargne")
      .validate();

    categorization.initContent()
      .add("10/08/2008", "Epargne", "Virement", -100)
      .check();
  }

  public void testSavingsHelpDisplayedWhenNoSavingsAccountAvailable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    views.selectCategorization();

    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .checkNoSeriesMessage("No savings account is declared")
      .clickSeriesMessageAccountCreationLink("create a savings account")
      .checkIsSavings()
      .checkUpdateModeIsEditable()
      .setAccountName("Epargne")
      .selectBank("CIC")
      .setPosition(0.00)
      .validate();

    views.selectBudget();
    budgetView.savings.editSeries("From account Epargne").deleteCurrentSeries();
    budgetView.savings.editSeries("To account Epargne").deleteCurrentSeries();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .checkNoSeriesMessage("There are no savings series");

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("My savings")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .validate();

    views.selectCategorization();
    categorization
      .selectSavings()
      .checkNoSeriesMessageHidden();

    views.selectBudget();
    budgetView.savings.editSeries("My savings")
      .deleteCurrentSeries();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .checkNoSeriesMessage("There are no savings series");

    views.selectHome();
    savingsAccounts.edit("Epargne").delete().validate();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .checkNoSeriesMessage("No savings account is declared");
  }

  public void testCreateSavingsAccountActionAvailableInSavingsBlock() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "ING")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .selectSavings()
      .createSavingsAccount()
      .checkIsSavings()
      .checkAccountTypeNotEditable()
      .setAccountName("Epargne ING")
      .selectBank("ING Direct")
      .setPosition(200.00)
      .validate();

    views.selectHome();
    savingsAccounts.edit("Epargne ING")
      .checkSelectedBank("ING Direct")
      .validate();
  }

  public void testCreateSeriesShouldNotCategorizeToTransactionIfNotValid() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .selectVariable().createSeries()
      .setName("Courses")
      .setEndDate(200805)
      .validate();

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("25/06/2008", TransactionType.PRELEVEMENT, "1_AUCHAN", "", -50.00)
      .add("15/05/2008", TransactionType.PRELEVEMENT, "2_AUCHAN", "", -40.00)
      .check();
  }

  public void testEditingSeriesUpdatesCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries("Courses");

    views.selectCategorization();

    timeline.selectMonth("2008/06");

    categorization
      .showSelectedMonthsOnly()
      .selectTableRow(0)
      .selectVariable()
      .editSeries("Courses")
      .setEndDate(200805)
      .validate();
    categorization.getVariable().checkDoesNotContainSeries("Courses");
  }

  public void testFilteringSeriesByValidMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/05/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Courses 2")
      .setCustom()
      .toggleMonth(6)
      .setStartDate(200805)
      .validate();

    budgetView.variable.createSeries()
      .setName("Courses 1")
      .setCustom()
      .toggleMonth(5)
      .validate();

    views.selectCategorization();

    categorization
      .selectTransactions("1_Auchan")
      .selectVariable()
      .checkNonActiveSeries("Courses 2")
      .checkActiveSeries("Courses 1")
      .selectSeries("Courses 1");

    categorization
      .selectTransactions("2_Auchan")
      .selectVariable()
      .checkNonActiveSeries("Courses 1")
      .checkActiveSeries("Courses 2");

    categorization
      .selectTransactions("1_Auchan", "2_Auchan")
      .checkSelectedTableRows(0, 1)
      .selectVariable()
      .checkActiveSeries("Courses 1")
      .checkActiveSeries("Courses 2");
  }

  public void testDoNotFilterValidMonthIfMonthIsUncheckedButWithAlreadyCategorizedOperations() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/06/15", -40.0, "2_Auchan")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Courses")
      .setCustom()
      .setStartDate(200804)
      .validate();

    views.selectCategorization();

    categorization
      .selectTransactions("1_Auchan")
      .selectVariable()
      .selectSeries("Courses");

    categorization.editSeries("Courses")
      .toggleMonth(6)
      .validate();

    categorization
      .selectTransactions("2_Auchan")
      .selectVariable()
      .selectSeries("Courses");
  }

  public void testDeleteTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "1_Auchan")
      .addTransaction("2008/06/15", -40.0, "2_Auchan")
      .load();

    views.selectCategorization();
    categorization.delete(0, 1)
      .checkMessageContains("Removing several operations")
      .cancel();
    categorization.delete("1_Auchan")
      .checkMessageContains("Removing one operation")
      .validate();
    categorization.initContent()
      .add("15/06/2008", "", "2_Auchan", -40.0)
      .check();
  }

  public void testInternalTransferSeriesHelp() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.00, "Transfer")
      .load();

    views.selectCategorization();
    categorization.selectTransaction("Transfer");
    categorization.selectOther()
      .selectInternalTransfers()
      .clickAndOpenSeriesEdition("create series")
      .checkName("Internal transfers")
      .checkSelectedProfile("Irregular")
      .checkAmount(0.00)
      .validate();

    categorization.checkTable(new Object[][]{
      {"25/06/2008", "Internal transfers", "Transfer", -50.00}
    });
  }

  public void testDoNotSwitchToUncategorized() throws Exception {
    operations.hideSignposts();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "Auchan")
      .addTransaction("2008/06/15", -40.0, "EDF")
      .addTransaction("2008/06/16", -30.0, "Monop")
      .load();

    views.selectCategorization();
    categorization.selectTransaction("Auchan")
      .checkToCategorize();
    categorization.setNewVariable("Auchan", "courses")
      .setNewRecurring("EDF", "electricite");
    categorization.selectTransaction("Monop")
      .checkRecurringPreSelected();

    categorization.selectTransaction("Auchan")
      .selectTransaction("Monop")
      .checkVariablePreSelected()
      .selectAllTransactions()
      .checkMultipleSeriesSelection();

    categorization.selectTransaction("Monop")
      .checkVariablePreSelected()
      .selectTransaction("Auchan")
      .selectUncategorized()
      .selectTransaction("Monop")
      .checkToCategorize();
  }

  public void testUseSignInCategorisation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/25", -50.0, "MSA")
      .addTransaction("2008/05/15", 200.0, "MSA")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectIncome()
      .createSeries("Alloc");
    categorization.selectTableRow(1)
      .selectVariable()
      .createSeries("Remboursement");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "MSA")
      .addTransaction("2008/06/15", 200.0, "MSA")
      .load();

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("25/06/2008", TransactionType.PRELEVEMENT, "MSA", "", -50.00, "Remboursement")
      .add("15/06/2008", TransactionType.VIREMENT, "MSA", "", 200.00, "Alloc")
      .add("25/05/2008", TransactionType.PRELEVEMENT, "MSA", "", -50.00, "Remboursement")
      .add("15/05/2008", TransactionType.VIREMENT, "MSA", "", 200.00, "Alloc")
      .check();
  }

  public void testNotAutocategorizationOutOfBeginEndDate() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/04/25", -50.0, "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectRecurring()
      .createSeries().setName("courses")
      .setStartDate(200804)
      .setEndDate(200804)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/25", -50.0, "Auchan")
      .load();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("25/05/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00)
      .add("25/04/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "courses")
      .check();
  }

  public void testColorChangeOnCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .checkLabel("AUCHAN C'EST BON")
      .selectVariable()
      .selectNewSeries("Courant")
      .checkSeriesIsSelected("Courant");
    categorization.checkYellowBgLabel(0, 1);
    categorization.selectTableRow(1)
      .checkNormalBgColor(0, 1)
      .selectTableRow(0);
  }

  public void testHidingBudgetAreaDescriptions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectVariable()
      .checkDescriptionShown()
      .checkShowDescriptionButtonHidden()
      .hideDescription()
      .checkDescriptionHidden();

    categorization.selectIncome()
      .checkDescriptionHidden()
      .showDescription()
      .checkDescriptionShown();

    categorization.selectExtras()
      .checkDescriptionShown()
      .hideDescription();

    categorization.selectSavings()
      .checkDescriptionHidden()
      .showDescription();

    categorization.selectVariable()
      .checkDescriptionShown();
  }

  public void testSavingsCategorizationMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000001", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -100.00, "SAVINGS 1.1")
      .addTransaction("2008/06/30", -100.00, "SAVINGS 1.2")
      .load();
    mainAccounts.edit("Account n. 000001")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000002", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -200.00, "SAVINGS 2")
      .load();
    mainAccounts.edit("Account n. 000002")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000003", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -200.00, "OTHER")
      .load();

    categorization.selectTransaction("OTHER");
    categorization.selectSavings().checkMessageHidden();

    categorization.selectTransaction("SAVINGS 1.1");
    categorization.checkSavingsPreSelected()
      .getSavings()
      .checkMessage("This operation is part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 1.2");
    categorization.checkSavingsPreSelected()
      .getSavings()
      .checkMessage("These operations are part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 1.2");
    categorization.checkSavingsPreSelected()
      .getSavings()
      .checkMessage("These operations are part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 2");
    categorization.checkSavingsPreSelected()
      .getSavings()
      .checkMessage("These operations are part of several savings accounts.");

    categorization.selectTransaction("SAVINGS 1.1");

    categorization.checkSavingsPreSelected()
      .checkAllButSavingBudgetAreaAreDisabled()
      .getSavings()
      .checkMessage("This operation is part of a savings account. Edit this account.")
      .clickMessageToEditAccount("Edit this account.")
      .setAsMain()
      .validate();

    categorization.checkSavingsPreSelected();
    categorization.checkAllBudgetAreasAreEnabled();

    categorization.selectVariable().selectNewSeries("Misc");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "OTHER", -200.0},
      {"30/06/2008", "Misc", "SAVINGS 1.1", -100.0},
      {"30/06/2008", "", "SAVINGS 1.2", -100.0},
      {"30/06/2008", "", "SAVINGS 2", -200.0}
    });
  }
}
