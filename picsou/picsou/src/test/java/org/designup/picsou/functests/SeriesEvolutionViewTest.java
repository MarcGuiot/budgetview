package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(8).validate();
  }

  public void testStandardDisplay() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "00000123", -125.0, "2008/07/12")
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
      .add("Main account", "", "-125.00", "45.00", "165.00", "335.00", "555.00", "425.00", "545.00")
      .add("Savings account", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "40.00", "", "", "", "", "", "")
      .add("Income", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Salary", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Recurring", "", "80.00", "30.00", "80.00", "30.00", "80.00", "30.00", "80.00")
      .add("Energy", "", "50.00", "", "50.00", "", "50.00", "", "50.00")
      .add("Internet", "", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00")
      .add("Envelopes", "", "20.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00")
      .add("Special", "", "", "", "", "", "+100.00", "300.00", "")
      .add("Christmas", "", "", "", "", "", "", "300.00", "")
      .add("Lottery", "", "", "", "", "", "+100.00", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesEvolution.checkForeground("To categorize", "Jul 08", "red");

    seriesEvolution.checkForeground("Main account", "Jul 08", "darkRed");
    seriesEvolution.checkForeground("Main account", "Aug 08", "darkGrey");

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
    fail("tbd");
  }

  public void testNoData() throws Exception {
    timeline.checkSelection("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkTableIsEmpty(
      "Balance", "Main account", "Savings account", "To categorize",
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
    String[] expanded = {"Balance", "Main account", "Savings account", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring", "Internet",
                         "Envelopes", "Groceries",
                         "Special", "Savings"};

    String[] collapsed = {"Balance", "Main account", "Savings account", "To categorize",
                          "Income", "Recurring", "Envelopes", "Special", "Savings"};

    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.collapse();
    seriesEvolution.checkRowLabels(collapsed);

    seriesEvolution.expand();
    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.doubleClickOnRow("Income");
    seriesEvolution.checkRowLabels("Balance", "Main account", "Savings account", "To categorize",
                                   "Income",
                                   "Recurring", "Internet",
                                   "Envelopes", "Groceries",
                                   "Special",
                                   "Savings");

    seriesEvolution.expand();
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
      .switchToManual()
      .selectMonth(200809)
      .setAmount("150")
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
      "\tMain account\t\t-200.00\t-400.00\t-600.00\t-800.00\t-1000.00\t-1200.00\t-1400.00\n" +
      "\tSavings account\t\t\t\t\t\t\t\t\n" +
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
