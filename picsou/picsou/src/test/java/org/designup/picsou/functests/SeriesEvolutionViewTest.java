package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
  }

  public void testStandardDisplay() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .addTransaction("2008/07/05", -30.00, "Free Telecom")
      .addTransaction("2008/07/05", -50.00, "EDF")
      .addTransaction("2008/07/01", 300.00, "WorldCo")
      .addTransaction("2008/07/11", -40.00, "SomethingElse")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -100.);
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();
    budgetView.recurring.editSeries("Energy").setTwoMonths().validate();

    timeline.selectMonth("2008/11");
    budgetView.extras.createSeries()
      .setName("Lottery")
      .selectPositiveAmounts()
      .setAmount(100.00)
      .validate();

    timeline.selectMonth("2008/12");
    budgetView.extras.createSeries()
      .setName("Christmas")
      .setAmount(300.00)
      .validate();

    timeline.selectMonth("2008/07");
    budgetView.variable.editSeries("Groceries")
      .setAmount(20)
      .validate();

    timeline.selectMonth("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesEvolution.initContent()
      .add("Main accounts", "", "-125.00", "45.00", "165.00", "335.00", "555.00", "425.00", "545.00")
      .add("Balance", "", "80.00", "170.00", "120.00", "170.00", "220.00", "-130.00", "120.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "40.00", "", "", "", "", "", "")
      .add("Income", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Salary", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Recurring", "", "80.00", "30.00", "80.00", "30.00", "80.00", "30.00", "80.00")
      .add("Energy", "", "50.00", "", "50.00", "", "50.00", "", "50.00")
      .add("Internet", "", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00")
      .add("Variable", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Extras", "", "", "", "", "", "+100.00", "300.00", "")
      .add("Christmas", "", "", "", "", "", "", "300.00", "")
      .add("Lottery", "", "", "", "", "", "+100.00", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.checkForeground("To categorize", "Jul 2008", "red");

    seriesEvolution.checkForeground("Main accounts", "Jul 2008", "darkRed");
    seriesEvolution.checkForeground("Main accounts", "Aug 2008", "darkGrey");

    seriesEvolution.checkForeground("Groceries", "Jul 2008", "red");
    seriesEvolution.checkForeground("Groceries", "Aug 2008", "0022BB");

    seriesEvolution.checkForeground("Variable", "Jul 2008", "AA0000"); // should be darkRed
    seriesEvolution.checkForeground("Variable", "Aug 2008", "darkGrey");

    seriesEvolution.checkForeground("Recurring", "Jul 2008", "darkGrey");
    seriesEvolution.checkForeground("Recurring", "Aug 2008", "darkGrey");

    seriesEvolution.checkForeground("Energy", "Jul 2008", "0022BB");
    seriesEvolution.checkForeground("Energy", "Aug 2008", "0022BB");
  }

  public void testShowsActualAmountsInThePast() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/05/10", -250.00, "Auchan")
      .addTransaction("2008/05/15", -200.00, "Auchan")
      .addTransaction("2008/05/01", 300.00, "WorldCo")
      .addTransaction("2008/05/15", 350.00, "Big Inc.")
      .addTransaction("2008/06/10", -280.00, "Auchan")
      .addTransaction("2008/06/15", -200.00, "Auchan")
      .addTransaction("2008/06/01", 310.00, "WorldCo")
      .addTransaction("2008/06/15", 360.00, "Big Inc.")
      .addTransaction("2008/07/10", -200.00, "Auchan")
      .addTransaction("2008/07/15", -140.00, "Auchan")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -480.);
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/06");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Main accounts", "480.00", "670.00", "870.00", "1070.00", "1270.00", "1470.00", "1670.00", "1870.00")
      .add("Balance", "200.00", "190.00", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "650.00", "670.00", "680.00", "680.00", "680.00", "680.00", "680.00", "680.00")
      .add("John's", "300.00", "310.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "350.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Groceries", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testTakesLastMonthWithTransactionsAsCurrentMonth() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/03/10", -250.00, "Auchan")
      .addTransaction("2008/03/15", -200.00, "Auchan")
      .addTransaction("2008/03/01", 300.00, "WorldCo")
      .addTransaction("2008/03/15", 350.00, "Big Inc.")
      .addTransaction("2008/04/10", -280.00, "Auchan")
      .addTransaction("2008/04/15", -200.00, "Auchan")
      .addTransaction("2008/04/01", 310.00, "WorldCo")
      .addTransaction("2008/04/15", 360.00, "Big Inc.")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -480.);
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/04");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Main accounts", "810.00", "1000.00", "1190.00", "1380.00", "1570.00", "1760.00", "1950.00", "2140.00")
      .add("Balance", "200.00", "190.00", "190.00", "190.00", "190.00", "190.00", "190.00", "190.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "650.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "300.00", "310.00", "310.00", "310.00", "310.00", "310.00", "310.00", "310.00")
      .add("Mary's", "350.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Groceries", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testNoData() throws Exception {
    timeline.checkSelection("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesEvolution.checkTableIsEmpty(
      "Main accounts", "Balance", "Savings accounts", "To categorize",
      "Income", "Recurring", "Variable", "Extras", "Savings", "Other");
  }

  public void testColumnNamesAreUpdatedOnMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectBudget();
    timeline.selectMonth("2008/12");
    budgetView.extras.createSeries()
      .setName("Christmas")
      .setAmount(200.00)
      .validate();

    views.selectEvolution();

    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "", "", "", "200.00", ""
    );

    timeline.selectMonth("2008/10");
    seriesEvolution.checkColumnNames(
      "", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009", "Apr 2009"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "200.00", "", "", "", ""
    );

    timeline.selectMonths("2008/09", "2008/10", "2008/11");
    seriesEvolution.checkColumnNames(
      "", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "", "200.00", "", "", ""
    );
  }

  public void testEditingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/07");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Main accounts", "", "1000.00", "1670.00", "2340.00", "3010.00", "3680.00", "4350.00", "5020.00")
      .add("Balance", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Jul 2008")
      .checkPositiveAmountsSelected()
      .setAmount(400.00)
      .setPropagationEnabled()
      .validate();

    seriesEvolution.initContent()
      .add("Main accounts", "", "1080.00", "1830.00", "2580.00", "3330.00", "4080.00", "4830.00", "5580.00")
      .add("Balance", "", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00")
      .add("John's", "", "400.00", "400.00", "400.00", "400.00", "400.00", "400.00", "400.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPropagationDisabled()
      .validate();

    seriesEvolution.initContent()
      .add("Main accounts", "", "1080.00", "1830.00", "2680.00", "3430.00", "4180.00", "4930.00", "5680.00")
      .add("Balance", "", "750.00", "750.00", "850.00", "750.00", "750.00", "750.00", "750.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "750.00", "850.00", "750.00", "750.00", "750.00", "750.00")
      .add("John's", "", "400.00", "400.00", "500.00", "400.00", "400.00", "400.00", "400.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .checkAmount(500.00)
      .checkPeriodicity("Every month")
      .editSeries()
      .setTwoMonths()
      .setEndDate(200812)
      .validate();

    seriesEvolution.initContent()
      .add("Main accounts", "", "1080.00", "1430.00", "2280.00", "2630.00", "3380.00", "3730.00", "4080.00")
      .add("Balance", "", "750.00", "350.00", "850.00", "350.00", "750.00", "350.00", "350.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "350.00", "850.00", "350.00", "750.00", "350.00", "350.00")
      .add("John's", "", "400.00", "", "500.00", "", "400.00", "", "")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPeriodicity("Every two months until december 2008")
      .validate();

    seriesEvolution.initContent()
      .add("Main accounts", "", "1080.00", "1430.00", "2280.00", "2630.00", "3380.00", "3730.00", "4080.00")
      .add("Balance", "", "750.00", "350.00", "850.00", "350.00", "750.00", "350.00", "350.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "350.00", "850.00", "350.00", "750.00", "350.00", "350.00")
      .add("John's", "", "400.00", "", "500.00", "", "400.00", "", "")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testDeletingTheShownSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/07");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Main accounts", "", "1000.00", "1670.00", "2340.00", "3010.00", "3680.00", "4350.00", "5020.00")
      .add("Balance", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's").deleteCurrentSeriesWithConfirmation();

    seriesEvolution.initContent()
      .add("Main accounts", "", "1000.00", "1350.00", "1700.00", "2050.00", "2400.00", "2750.00", "3100.00")
      .add("Balance", "", "670.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "320.00", "", "", "", "", "", "")
      .add("Income", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .add("Other", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testNothingIsShownAfterTheLastMonth() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -100.);

    views.selectEvolution();
    timeline.selectMonths("2008/08");
    seriesEvolution.checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );
    seriesEvolution.checkRow(
      "Groceries", "100.00", "100.00", "100.00", "100.00", "", "", "", ""
    );
  }

  public void testExpandAndCollapse() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewIncome("GlobalCorp", "Salary 2");

    views.selectEvolution();
    String[] expanded = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring", "Internet",
                         "Variable", "Groceries",
                         "Extras", "Savings", "Other"};

    String[] collapsed = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                          "Income", "Recurring", "Variable", "Extras", "Savings", "Other"};

    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.collapseAll();
    seriesEvolution.checkRowLabels(collapsed);

    seriesEvolution.expandAll();
    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.doubleClickOnRow("Income");
    seriesEvolution.checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                   "Income",
                                   "Recurring", "Internet",
                                   "Variable", "Groceries",
                                   "Extras",
                                   "Savings",
                                   "Other");

    seriesEvolution.expandAll();
    seriesEvolution.checkRowLabels(expanded);
  }

  public void testEditingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -100.);

    views.selectEvolution();
    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesEvolution.checkRow(
      "Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00"
    );

    seriesEvolution.editSeries("Groceries", "Sep 2008")
      .setAmount(150.00)
      .checkPropagationDisabled()
      .validate();

    seriesEvolution.editSeries("Groceries")
      .setEndDate(200810)
      .validate();

    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesEvolution.checkRow(
      "Groceries", "", "100.00", "100.00", "150.00", "100.00", "", "", ""
    );
  }

  public void testOnlySeriesWithDataForTheDisplayedPeriodAreShown() throws Exception {
    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .validate();

    views.selectEvolution();
    seriesEvolution.checkSeriesNotShown("Taxes");

    views.selectBudget();
    budgetView.recurring.editSeries("Taxes")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    views.selectEvolution();
    seriesEvolution.checkRow(
      "Taxes", "", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00"
    );

    views.selectBudget();
    budgetView.recurring.editSeries("Taxes")
      .setEndDate(200808)
      .validate();

    timeline.selectMonth("2008/10");
    views.selectEvolution();
    seriesEvolution.checkSeriesNotShown("Taxes");

    timeline.selectMonth("2008/08");
    seriesEvolution.checkRow(
      "Taxes", "200.00", "200.00", "", "", "", "", "", ""
    );
  }

  public void testSeriesWithDataOnlyInObservedAreShown() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Impots")
      .load();
    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .selectAllMonths()
      .setAmount(0)
      .validate();
    views.selectCategorization();
    categorization.setRecurring("Impots", "Taxes");

    views.selectEvolution();
    seriesEvolution.checkRow("Taxes", "", "100.00", "", "", "", "", "", "");
  }

  public void testClipboardExport() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    views.selectEvolution();
    seriesEvolution.checkClipboardExport(
      "\t\tJune 2008\tJul 2008\tAug 2008\tSep 2008\tOct 2008\tNov 2008\tDec 2008\tJan 2009\tFeb 2009\tMar 2009\tApr 2009\tMay 2009\n" +
      "\tMain accounts\t\t-200.00\t-400.00\t-600.00\t-800.00\t-1000.00\t-1200.00\t-1400.00\t\t\t\t\n" +
      "\tBalance\t\t3416.00\t-200.00\t-200.00\t-200.00\t-200.00\t-200.00\t-200.00\t\t\t\t\n" +
      "\tSavings accounts\t\t\t\t\t\t\t\t\t\t\t\t\n" +
      "\tTo categorize\t\t3864.00\t\t\t\t\t\t\t\t\t\t\n" +
      "\tIncome\t\t\t\t\t\t\t\t\t\t\t\t\n" +
      "\tRecurring\t\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t\t\t\t\n" +
      "\tTaxes\t\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t\t\t\t\n" +
      "\tVariable\t\t\t\t\t\t\t\t\t\t\t\t\n" +
      "\tExtras\t\t\t\t\t\t\t\t\t\t\t\t\n" +
      "\tSavings\t\t\t\t\t\t\t\t\t\t\t\t\n" +
      "\tOther\t\t\t\t\t\t\t\t\t\t\t\t\n"
    );
  }

  public void testMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectEvolution();
    timeline.checkSelection("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesEvolution.checkPreviousMonthSelectionDisabled();
    seriesEvolution.selectNextMonth();
    timeline.checkSelection("2008/08");
    seriesEvolution.checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );

    seriesEvolution.selectPreviousMonth();
    timeline.checkSelection("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    timeline.selectMonth("2009/01");
    seriesEvolution.checkNextMonthSelectionDisabled();
    seriesEvolution.selectPreviousMonth();
    timeline.checkSelection("2008/12");
    seriesEvolution.checkColumnNames(
      "", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009", "Apr 2009", "May 2009", "June 2009"
    );

    timeline.selectMonths("2008/07", "2009/01");
    seriesEvolution.checkPreviousMonthSelectionDisabled();
    seriesEvolution.checkNextMonthSelectionDisabled();

    timeline.selectMonths("2008/11", "2008/12");
    seriesEvolution.selectNextMonth();
    timeline.checkSelection("2009/01");

    timeline.selectMonths("2008/11", "2008/12");
    seriesEvolution.selectPreviousMonth();
    timeline.checkSelection("2008/10");
  }

  public void testMirrorSavingsSeriesAreNotShown() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Virement")
      .load();

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setAccountNumber("333")
      .setPosition(10.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("333", 20, "2008/07/12")
      .addTransaction("2008/07/12", +95.00, "Virt livret")
      .load();

    views.selectCategorization();
    categorization.setSavings("Virement", "To account Livret");
    categorization.setSavings("Virt livret", "To account Livret");

    views.selectEvolution();
    seriesEvolution.expandAll();
    seriesEvolution.checkRowLabels(
      "Main accounts", "Balance", "Savings accounts", "To categorize", "Income", "Recurring", "Variable", "Extras",
      "Savings", "To account Livret", "To account Livret", "Other"
    );
  }
}
