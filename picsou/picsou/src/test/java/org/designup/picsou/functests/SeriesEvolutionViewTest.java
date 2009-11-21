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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();
    budgetView.recurring.editSeries("Energy").setTwoMonths().validate();

    timeline.selectMonth("2008/11");
    budgetView.specials.createSeries()
      .setName("Lottery")
      .selectPositiveAmounts()
      .setAmount(100.00)
      .validate();

    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Christmas")
      .setAmount(300.00)
      .validate();

    timeline.selectMonth("2008/07");
    budgetView.envelopes.editSeries("Groceries")
      .switchToManual()
      .setAmount(20)
      .validate();

    timeline.selectMonth("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );

    seriesEvolution.initContent()
      .add("Balance", "", "80.00", "170.00", "120.00", "170.00", "220.00", "-130.00", "120.00")
      .add("Main accounts", "", "-125.00", "45.00", "165.00", "335.00", "555.00", "425.00", "545.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "40.00", "", "", "", "", "", "")
      .add("Income", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Salary", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Recurring", "", "80.00", "30.00", "80.00", "30.00", "80.00", "30.00", "80.00")
      .add("Energy", "", "50.00", "", "50.00", "", "50.00", "", "50.00")
      .add("Internet", "", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00")
      .add("Envelopes", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Special", "", "", "", "", "", "+100.00", "300.00", "")
      .add("Christmas", "", "", "", "", "", "", "300.00", "")
      .add("Lottery", "", "", "", "", "", "+100.00", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.checkForeground("To categorize", "Jul 08", "red");

    seriesEvolution.checkForeground("Main accounts", "Jul 08", "darkRed");
    seriesEvolution.checkForeground("Main accounts", "Aug 08", "darkGrey");

    seriesEvolution.checkForeground("Groceries", "Jul 08", "red");
    seriesEvolution.checkForeground("Groceries", "Aug 08", "0022BB");

    seriesEvolution.checkForeground("Envelopes", "Jul 08", "AA0000"); // should be darkRed
    seriesEvolution.checkForeground("Envelopes", "Aug 08", "darkGrey");

    seriesEvolution.checkForeground("Recurring", "Jul 08", "darkGrey");
    seriesEvolution.checkForeground("Recurring", "Aug 08", "darkGrey");

    seriesEvolution.checkForeground("Energy", "Jul 08", "0022BB");
    seriesEvolution.checkForeground("Energy", "Aug 08", "0022BB");
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/06");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Balance", "200.00", "190.00", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00")
      .add("Main accounts", "480.00", "670.00", "870.00", "1070.00", "1270.00", "1470.00", "1670.00", "1870.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "650.00", "670.00", "680.00", "680.00", "680.00", "680.00", "680.00", "680.00")
      .add("John's", "300.00", "310.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "350.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Groceries", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/04");

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Balance", "200.00", "190.00", "190.00", "190.00", "190.00", "190.00", "190.00", "190.00")
      .add("Main accounts", "810.00", "1000.00", "1190.00", "1380.00", "1570.00", "1760.00", "1950.00", "2140.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "650.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "300.00", "310.00", "310.00", "310.00", "310.00", "310.00", "310.00", "310.00")
      .add("Mary's", "350.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00", "360.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Groceries", "450.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00", "480.00")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testNoData() throws Exception {
    timeline.checkSelection("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkTableIsEmpty(
      "Balance", "Main accounts", "Savings accounts", "To categorize",
      "Income", "Recurring", "Envelopes", "Special", "Savings");
  }

  public void testColumnNamesAreUpdatedOnMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectBudget();
    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Christmas")
      .setAmount(200.00)
      .validate();

    views.selectEvolution();

    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "", "", "", "200.00", ""
    );

    timeline.selectMonth("2008/10");
    seriesEvolution.checkColumnNames(
      "", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09", "Mar 09", "Apr 09"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "200.00", "", "", "", ""
    );

    timeline.selectMonths("2008/09", "2008/10", "2008/11");
    seriesEvolution.checkColumnNames(
      "", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09", "Mar 09"
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
      .add("Balance", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("Main accounts", "", "1000.00", "1670.00", "2340.00", "3010.00", "3680.00", "4350.00", "5020.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Jul 08")
      .checkPositiveAmountsSelected()
      .setAmount(400.00)
      .checkPropagationEnabled()
      .validate();

    seriesEvolution.initContent()
      .add("Balance", "", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00")
      .add("Main accounts", "", "1080.00", "1830.00", "2580.00", "3330.00", "4080.00", "4830.00", "5580.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00", "750.00")
      .add("John's", "", "400.00", "400.00", "400.00", "400.00", "400.00", "400.00", "400.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Sep 08")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .setPropagationDisabled()
      .validate();

    seriesEvolution.initContent()
      .add("Balance", "", "750.00", "750.00", "850.00", "750.00", "750.00", "750.00", "750.00")
      .add("Main accounts", "", "1080.00", "1830.00", "2680.00", "3430.00", "4180.00", "4930.00", "5680.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "750.00", "750.00", "850.00", "750.00", "750.00", "750.00", "750.00")
      .add("John's", "", "400.00", "400.00", "500.00", "400.00", "400.00", "400.00", "400.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Sep 08")
      .checkPositiveAmountsSelected()
      .checkAmount("500.00")
      .checkPeriodicity("Every month")
      .editSeries()
      .setTwoMonths()
      .setEndDate(200812)
      .validate();

// On devrait n'avoir qu'un mois sur deux dans SeriesEvolution apres le validate, mais ils restent tous
//    openApplication();
    fail();
//
    seriesEvolution.initContent()
      .dump();

    seriesEvolution.editSeries("John's", "Sep 08")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPeriodicity("Every two months until dec 08")
      .validate();
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
      .add("Balance", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("Main accounts", "", "1000.00", "1670.00", "2340.00", "3010.00", "3680.00", "4350.00", "5020.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00", "670.00")
      .add("John's", "", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00", "320.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.editSeries("John's", "Jul 08")
      .editSeries()
      .deleteCurrentSeriesWithConfirmation();

    seriesEvolution.initContent()
      .add("Balance", "", "670.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Main accounts", "", "1000.00", "1350.00", "1700.00", "2050.00", "2400.00", "2750.00", "3100.00")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "320.00", "", "", "", "", "", "")
      .add("Income", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Mary's", "", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00", "350.00")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Envelopes", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testNothingIsShownAfterTheLastMonth() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Groceries");

    views.selectEvolution();
    timeline.selectMonths("2008/08");
    seriesEvolution.checkColumnNames(
      "", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09"
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewIncome("GlobalCorp", "Salary 2");

    views.selectEvolution();
    String[] expanded = {"Balance", "Main accounts", "Savings accounts", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring", "Internet",
                         "Envelopes", "Groceries",
                         "Special", "Savings"};

    String[] collapsed = {"Balance", "Main accounts", "Savings accounts", "To categorize",
                          "Income", "Recurring", "Envelopes", "Special", "Savings"};

    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.collapseAll();
    seriesEvolution.checkRowLabels(collapsed);

    seriesEvolution.expandAll();
    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.doubleClickOnRow("Income");
    seriesEvolution.checkRowLabels("Balance", "Main accounts", "Savings accounts", "To categorize",
                                   "Income",
                                   "Recurring", "Internet",
                                   "Envelopes", "Groceries",
                                   "Special",
                                   "Savings");

    seriesEvolution.expandAll();
    seriesEvolution.checkRowLabels(expanded);
  }

  public void testEditingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Groceries");

    views.selectEvolution();
    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkRow(
      "Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00"
    );

    seriesEvolution.editSeries("Groceries", "Sep 08")
      .setAmount(150.00)
      .setPropagationDisabled()
      .validate();

    seriesEvolution.editSeries("Groceries", "Sep 08")
      .editSeries()
      .setEndDate(200810)
      .validate();

    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
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
      .switchToManual()
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
      .switchToManual()
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
      .switchToManual()
      .selectAllMonths()
      .setAmount(200)
      .validate();

    views.selectEvolution();
    seriesEvolution.checkClipboardExport(
      "\t\tJune 08\tJul 08\tAug 08\tSep 08\tOct 08\tNov 08\tDec 08\tJan 09\n" +
      "\tBalance\t\t3416.00\t-200.00\t-200.00\t-200.00\t-200.00\t-200.00\t-200.00\n" +
      "\tMain accounts\t\t-200.00\t-400.00\t-600.00\t-800.00\t-1000.00\t-1200.00\t-1400.00\n" +
      "\tSavings accounts\t\t\t\t\t\t\t\t\n" +
      "\tTo categorize\t\t3864.00\t\t\t\t\t\t\n" +
      "\tIncome\t\t\t\t\t\t\t\t\n" +
      "\tRecurring\t\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\n" +
      "\tTaxes\t\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\t200.00\n" +
      "\tEnvelopes\t\t\t\t\t\t\t\t\n" +
      "\tSpecial\t\t\t\t\t\t\t\t\n" +
      "\tSavings\t\t\t\t\t\t\t\t\n"
    );
  }
}
