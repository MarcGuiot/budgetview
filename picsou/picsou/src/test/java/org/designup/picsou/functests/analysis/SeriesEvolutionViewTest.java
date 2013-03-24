package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

import java.awt.*;

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

    categorization.setNewVariable("Auchan", "Groceries", -100.00);
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    budgetView.recurring.editSeries("Energy").setRepeatEveryTwoMonths().validate();

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
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesAnalysis
      .checkTableHidden()
      .checkToggleLabel("Show details")
      .toggleTable()
      .checkToggleLabel("Hide details")
      .checkTableShown();

    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.unselectAll();
    seriesAnalysis.checkForeground("To categorize", "Jul 2008", "red");

    seriesAnalysis.checkForeground("Main accounts", "Jul 2008", "darkRed");
    seriesAnalysis.checkForeground("Main accounts", "Aug 2008", "darkGrey");

    seriesAnalysis.checkForeground("Groceries", "Jul 2008", "red");
    seriesAnalysis.checkForeground("Groceries", "Aug 2008", "0022BB");

    seriesAnalysis.checkForeground("Variable", "Jul 2008", "AA0000"); // should be darkRed
    seriesAnalysis.checkForeground("Variable", "Aug 2008", "darkGrey");

    seriesAnalysis.checkForeground("Recurring", "Jul 2008", "darkGrey");
    seriesAnalysis.checkForeground("Recurring", "Aug 2008", "darkGrey");

    seriesAnalysis.checkForeground("Energy", "Jul 2008", "0022BB");
    seriesAnalysis.checkForeground("Energy", "Aug 2008", "0022BB");
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

    categorization.setNewVariable("Auchan", "Groceries", -480.);
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/06");

    seriesAnalysis.toggleTable();
    seriesAnalysis.initContent()
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

    categorization.setNewVariable("Auchan", "Groceries", -480.);
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/04");

    seriesAnalysis.toggleTable();
    seriesAnalysis.initContent()
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
      .check();
  }

  public void testNoData() throws Exception {
    timeline.checkSelection("2008/07");
    seriesAnalysis.toggleTable();
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.checkTableIsEmpty(
      "Main accounts", "Balance", "Savings accounts", "To categorize",
      "Income", "Recurring", "Variable", "Extras", "Savings");
  }

  public void testColumnNamesAreUpdatedOnMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    timeline.selectMonth("2008/12");
    budgetView.extras.createSeries()
      .setName("Christmas")
      .setAmount(200.00)
      .validate();

    seriesAnalysis.toggleTable();

    timeline.selectMonth("2008/07");
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.checkRow(
      "Christmas", "", "", "", "", "", "", "200.00", ""
    );

    timeline.selectMonth("2008/10");
    seriesAnalysis.checkColumnNames(
      "", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009", "Apr 2009"
    );
    seriesAnalysis.checkRow(
      "Christmas", "", "", "", "200.00", "", "", "", ""
    );

    timeline.selectMonths("2008/09", "2008/10", "2008/11");
    seriesAnalysis.checkColumnNames(
      "", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009"
    );
    seriesAnalysis.checkRow(
      "Christmas", "", "", "", "", "200.00", "", "", ""
    );
  }

  public void testEditingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/07");

    seriesAnalysis.toggleTable();
    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.editSeries("John's", "Jul 2008")
      .checkPositiveAmountsSelected()
      .setAmount(400.00)
      .setPropagationEnabled()
      .validate();

    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPropagationDisabled()
      .validate();

    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .checkAmount(500.00)
      .checkPeriodicity("Every month")
      .editSeries()
      .setRepeatEveryTwoMonths()
      .setEndDate(200812)
      .validate();

    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPeriodicity("Every two months until december 2008")
      .validate();

    seriesAnalysis.initContent()
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
      .check();
  }

  public void testNavigatingFromTheBudgetView() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/07");

    budgetView.income.gotoAnalysis("John's");

    views.checkAnalysisSelected();
    seriesAnalysis.checkBreadcrumb("Overall budget > Income > John's");

    seriesAnalysis.checkBudgetStackShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00)
      .checkValue("John's", 320.00, true);

    seriesAnalysis.toggleTable();
    seriesAnalysis.checkSelected("John's");

    seriesAnalysis.collapseAll();

    views.selectBudget();
    budgetView.income.gotoAnalysis("Mary's");

    seriesAnalysis.checkSelected("Mary's");
    seriesAnalysis.checkBreadcrumb("Overall budget > Income > Mary's");

    seriesAnalysis.checkBudgetStackShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00, true)
      .checkValue("John's", 320.00);

    seriesAnalysis.checkExpanded("Income", true);
    seriesAnalysis.checkSelected("Mary's");
  }

  public void testDeletingTheShownSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/07/30")
      .addTransaction("2008/07/01", 320.00, "WorldCo")
      .addTransaction("2008/07/15", 350.00, "Big Inc.")
      .load();

    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2008/07");

    seriesAnalysis.toggleTable();
    seriesAnalysis.initContent()
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
      .check();

    seriesAnalysis.editSeries("John's").deleteCurrentSeriesWithConfirmation();

    seriesAnalysis.initContent()
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
      .check();
  }

  public void testNothingIsShownAfterTheLastMonth() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -100.);

    timeline.selectMonths("2008/08");
    seriesAnalysis.toggleTable();
    seriesAnalysis.checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );
    seriesAnalysis.checkRow(
      "Groceries", "100.00", "100.00", "100.00", "100.00", "", "", "", ""
    );
  }

  public void testExpandAndCollapse() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/12", -50.00, "Boucherie")
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    budgetView.variable.createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Meat")
      .addSubSeries("Vegetables")
      .validate();

    categorization.setVariable("Auchan", "Groceries", "Vegetables");
    categorization.setVariable("Boucherie", "Groceries", "Meat");
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewIncome("GlobalCorp", "Salary 2");

    views.selectAnalysis();
    seriesAnalysis.toggleTable();

    String[] expanded = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring",
                         "Variable", "Groceries", "Meat", "Vegetables",
                         "Extras", "Savings"};

    String[] collapsed = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                          "Income", "Recurring", "Variable", "Extras", "Savings"};

    seriesAnalysis.checkRowLabels(expanded);

    seriesAnalysis.checkExpansionEnabled("Main accounts", false);
    seriesAnalysis.checkExpansionEnabled("Balance", false);
    seriesAnalysis.checkExpansionEnabled("Savings accounts", false);
    seriesAnalysis.checkExpansionEnabled("To categorize", false);
    seriesAnalysis.checkExpansionEnabled("Income", true);
    seriesAnalysis.checkExpansionEnabled("Recurring", false);
    seriesAnalysis.checkExpansionEnabled("Groceries", true);
    seriesAnalysis.checkExpansionEnabled("Extras", false);

    seriesAnalysis.collapseAll();
    seriesAnalysis.checkRowLabels(collapsed);
    seriesAnalysis.checkExpanded("Income", false);

    seriesAnalysis.expandAll();
    seriesAnalysis.checkRowLabels(expanded);
    seriesAnalysis.checkExpanded("Income", true);
    seriesAnalysis.checkExpanded("Groceries", true);

    categorization.setNewRecurring("Free Telecom", "Internet");
    seriesAnalysis.checkExpansionEnabled("Recurring", true);
    seriesAnalysis.checkExpanded("Recurring", true);

    seriesAnalysis.checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                  "Income", "Salary", "Salary 2",
                                  "Recurring", "Internet",
                                  "Variable", "Groceries", "Meat", "Vegetables",
                                  "Extras",
                                  "Savings");

    seriesAnalysis.doubleClickOnRow("Income");
    seriesAnalysis.checkExpanded("Income", false);
    seriesAnalysis.checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                  "Income",
                                  "Recurring", "Internet",
                                  "Variable", "Groceries", "Meat", "Vegetables",
                                  "Extras",
                                  "Savings");

    seriesAnalysis.doubleClickOnRow("Groceries");
    seriesAnalysis.checkExpanded("Groceries", false);
    seriesAnalysis.checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                  "Income",
                                  "Recurring", "Internet",
                                  "Variable", "Groceries",
                                  "Extras",
                                  "Savings");

    seriesAnalysis.expandAll();
    seriesAnalysis.checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                  "Income", "Salary", "Salary 2",
                                  "Recurring", "Internet",
                                  "Variable", "Groceries", "Meat", "Vegetables",
                                  "Extras",
                                  "Savings");

    // Expansion disabled for budget areas without series
    categorization.selectTransactions("GlobalCorp", "WorldCo").setUncategorized();
    seriesAnalysis.checkExpansionEnabled("Income", true);
    budgetView.income.editSeries("Salary").deleteCurrentSeries();
    budgetView.income.editSeries("Salary 2").selectAllMonths().setAmount(0.00).validate();
    seriesAnalysis.checkExpansionEnabled("Income", false);
  }

  public void testClickingTheExpandCollapseButtonSelectsTheCorrespondingRow() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/12", -50.00, "Boucherie")
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    budgetView.variable.createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Meat")
      .addSubSeries("Vegetables")
      .validate();

    categorization.setVariable("Auchan", "Groceries", "Vegetables");
    categorization.setVariable("Boucherie", "Groceries", "Meat");
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewIncome("GlobalCorp", "Salary 2");

    views.selectAnalysis();
    seriesAnalysis.toggleTable();
    seriesAnalysis
      .toggleExpansion("Groceries")
      .checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                      "Income", "Salary", "Salary 2",
                      "Recurring",
                      "Variable", "Groceries",
                      "Extras",
                      "Savings")
      .checkSelected("Groceries");
    
    seriesAnalysis
      .select("Salary")
      .checkSelected("Salary")
      .toggleExpansion("Groceries")
      .checkSelected("Groceries");
  }

  public void testEditingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -100.);

    timeline.selectMonth("2008/07");
    seriesAnalysis.toggleTable();
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.checkRow(
      "Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00"
    );

    seriesAnalysis.editSeries("Groceries", "Sep 2008")
      .setAmount(150.00)
      .checkPropagationDisabled()
      .validate();

    seriesAnalysis.editSeries("Groceries")
      .setEndDate(200810)
      .validate();

    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.checkRow(
      "Groceries", "", "100.00", "100.00", "150.00", "100.00", "", "", ""
    );
  }

  public void testOnlySeriesWithDataForTheDisplayedPeriodAreShown() throws Exception {
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .validate();

    seriesAnalysis.toggleTable();
    seriesAnalysis.checkNoTableRowWithLabel("Taxes");

    budgetView.recurring.editSeries("Taxes")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    seriesAnalysis.checkRow(
      "Taxes", "", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00"
    );

    budgetView.recurring.editSeries("Taxes")
      .setEndDate(200808)
      .validate();

    timeline.selectMonth("2008/10");
    seriesAnalysis.checkNoTableRowWithLabel("Taxes");

    timeline.selectMonth("2008/08");
    seriesAnalysis.checkRow(
      "Taxes", "200.00", "200.00", "", "", "", "", "", ""
    );
  }

  public void testSeriesWithDataOnlyInObservedAreShown() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Impots")
      .load();

    budgetView.recurring.createSeries()
      .setName("Taxes")
      .selectAllMonths()
      .setAmount(0)
      .validate();

    categorization.setRecurring("Impots", "Taxes");

    seriesAnalysis.toggleTable();
    seriesAnalysis.checkRow("Taxes", "", "100.00", "", "", "", "", "", "");
  }

  public void testClipboardExport() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    budgetView.recurring.createSeries()
      .setName("Taxes")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    mainWindow.getAwtComponent().setSize(new Dimension(1024, 760));

    seriesAnalysis.toggleTable();
    seriesAnalysis.checkSelectionClipboardExport(
      new int[]{4,5,6},
      ""
    );

    seriesAnalysis.checkTableClipboardExport(
      ""
    );
  }

  public void testMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    timeline.checkSelection("2008/07");
    seriesAnalysis.toggleTable();
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesAnalysis.checkPreviousMonthSelectionDisabled();
    seriesAnalysis.selectNextMonth();
    timeline.checkSelection("2008/08");
    seriesAnalysis.checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );

    seriesAnalysis.selectPreviousMonth();
    timeline.checkSelection("2008/07");
    seriesAnalysis.checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    timeline.selectMonth("2009/01");
    seriesAnalysis.checkNextMonthSelectionDisabled();
    seriesAnalysis.selectPreviousMonth();
    timeline.checkSelection("2008/12");
    seriesAnalysis.checkColumnNames(
      "", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009", "Apr 2009", "May 2009", "June 2009"
    );

    timeline.selectMonths("2008/07", "2009/01");
    seriesAnalysis.checkPreviousMonthSelectionDisabled();
    seriesAnalysis.checkNextMonthSelectionDisabled();

    timeline.selectMonths("2008/11", "2008/12");
    seriesAnalysis.selectNextMonth();
    timeline.checkSelection("2009/01");

    timeline.selectMonths("2008/11", "2008/12");
    seriesAnalysis.selectPreviousMonth();
    timeline.checkSelection("2008/10");
  }

  public void testMirrorSavingsSeriesAreNotShown() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Virement")
      .load();

    savingsAccounts.createNewAccount()
      .setName("Livret")
      .selectBank("ING Direct")
      .setAccountNumber("333")
      .setPosition(10.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("333", 20, "2008/07/12")
      .addTransaction("2008/07/12", +95.00, "Virt livret")
      .loadInAccount("Livret");

    categorization.setSavings("Virement", "To account Livret");
    categorization.setSavings("Virt livret", "To account Livret");

    seriesAnalysis.toggleTable();
    seriesAnalysis.expandAll();
    seriesAnalysis.checkRowLabels(
      "Main accounts", "Balance", "Savings accounts", "To categorize", "Income", "Recurring", "Variable", "Extras",
      "Savings", "To account Livret", "To account Livret"
    );
  }

  public void testBreadcrumbs() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .addTransaction("2008/07/05", -30.00, "Free Telecom")
      .addTransaction("2008/07/05", -50.00, "EDF")
      .addTransaction("2008/07/01", 300.00, "WorldCo")
      .addTransaction("2008/07/11", -40.00, "SomethingElse")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -100.00);
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    seriesAnalysis.clearSelection();
    seriesAnalysis.checkBreadcrumb("Overall budget / summary");

    seriesAnalysis.toggleTable();
    seriesAnalysis.select("Energy");
    seriesAnalysis.checkBreadcrumb("Overall budget > Recurring > Energy");

    seriesAnalysis.clickBreadcrumb("Recurring");
    seriesAnalysis.checkSelected("Recurring");
    seriesAnalysis.checkBreadcrumb("Overall budget > Recurring");

    seriesAnalysis.clickBreadcrumb("Overall budget");
    seriesAnalysis.checkSelected("Balance");

    seriesAnalysis.select("Salary");
    seriesAnalysis.checkBreadcrumb("Overall budget > Income > Salary");

    // Unselect
    seriesAnalysis.clearSelection();
    seriesAnalysis.checkBreadcrumb("Overall budget / summary");

    // Series renaming
    seriesAnalysis.select("Internet");
    seriesAnalysis.checkBreadcrumb("Overall budget > Recurring > Internet");
    seriesAnalysis.editSeries("Internet")
      .setName("Net")
      .validate();
    seriesAnalysis.checkBreadcrumb("Overall budget > Recurring > Net");

    // Change of budget area
    seriesAnalysis.editSeries("Net")
      .changeBudgetArea("Variable")
      .validate();
    seriesAnalysis.checkBreadcrumb("Overall budget / summary");

    // Series deletion
    seriesAnalysis.select("Net");
    seriesAnalysis.checkBreadcrumb("Overall budget > Variable > Net");
    seriesAnalysis.editSeries("Net")
      .deleteCurrentSeriesWithConfirmation();
    seriesAnalysis.checkNoSelection();
    seriesAnalysis.checkBreadcrumb("Overall budget / summary");
  }

  public void testPopupMenus() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/18", -100.00, "Virt vers ING")
      .addTransaction("2009/06/20", -30.00, "Free")
      .addTransaction("2009/06/20", -50.00, "Orange")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/06/20", 50.00, "Unknown1")
      .addTransaction("2009/06/20", -100.00, "Unknown2")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -140.00, "Auchan")
      .addTransaction("2009/07/01", 320.00, "WorldCo")
      .addTransaction("2009/07/20", -30.00, "Free")
      .addTransaction("2009/07/20", -60.00, "Orange")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000234", 2000.00, "2009/07/30")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/07/15", 350.00, "Big Inc.")
      .addTransaction("2009/07/20", -20.00, "Unknown3")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000456", 5000.00, "2009/07/30")
      .addTransaction("2009/06/10", -200.00, "Virt sur ING")
      .load();
    mainAccounts
      .edit("Account n. 00000456")
      .setAsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .validate();
    categorization.setNewSavings("Virt sur ING", "Virt vers livret", "ING", "Main accounts");

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -450.00);
    categorization.setNewSavings("Virt vers ING", "Virt de livret", "Main accounts", "ING");

    seriesAnalysis.toggleTable();

    // ---- Balance ----

    views.selectAnalysis();
    timeline.selectMonth(200906);
    seriesAnalysis.checkRightClickOptions("Balance",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("Balance", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/06/2009", "Groceries", "AUCHAN", -250.00)
      .add("15/06/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("20/06/2009", "Internet", "FREE", -30.00)
      .add("20/06/2009", "Mobile", "ORANGE", -50.00)
      .add("20/06/2009", "", "UNKNOWN1", 50.00)
      .add("20/06/2009", "", "UNKNOWN2", -100.00)
      .add("10/06/2009", "Virt vers livret", "VIRT SUR ING", -200.00)
      .add("18/06/2009", "Virt de livret", "VIRT VERS ING", -100.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();
    categorization.checkCustomFilterVisible(false);

    mainAccounts.select("Account n. 00000123");
    transactions.setSearchText("a");
    views.selectAnalysis();
    seriesAnalysis.rightClickAndSelect("Balance", "Show transactions in Accounts view");
    mainAccounts.checkNoAccountsSelected();
    savingsAccounts.checkNoAccountsSelected();
    transactions.checkSearchTextIsEmpty();
    transactions.initContent()
      .add("20/06/2009", TransactionType.PRELEVEMENT, "UNKNOWN2", "", -100.00)
      .add("20/06/2009", TransactionType.VIREMENT, "UNKNOWN1", "", 50.00)
      .add("20/06/2009", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00, "Mobile")
      .add("20/06/2009", TransactionType.PRELEVEMENT, "FREE", "", -30.00, "Internet")
      .add("18/06/2009", TransactionType.PRELEVEMENT, "VIRT VERS ING", "", -100.00, "Virt de livret")
      .add("15/06/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("15/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "VIRT SUR ING", "", -200.00, "Virt vers livret")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -250.00, "Groceries")
      .add("01/06/2009", TransactionType.VIREMENT, "WORLDCO", "", 300.00, "John's")
      .check();
    transactions.checkClearFilterButtonHidden();

    // ---- Main accounts ----

    views.selectAnalysis();
    seriesAnalysis.select("Main accounts");
    seriesAnalysis.checkRightClickOptions("Main accounts",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("Main accounts", "Show transactions in Accounts view");
    mainAccounts.checkSelectedAccounts("Account n. 00000234", "Account n. 00000123");
    savingsAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("20/06/2009", TransactionType.PRELEVEMENT, "UNKNOWN2", "", -100.00)
      .add("20/06/2009", TransactionType.VIREMENT, "UNKNOWN1", "", 50.00)
      .add("20/06/2009", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00, "Mobile")
      .add("20/06/2009", TransactionType.PRELEVEMENT, "FREE", "", -30.00, "Internet")
      .add("18/06/2009", TransactionType.PRELEVEMENT, "VIRT VERS ING", "", -100.00, "Virt de livret")
      .add("15/06/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("15/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -250.00, "Groceries")
      .add("01/06/2009", TransactionType.VIREMENT, "WORLDCO", "", 300.00, "John's")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    // ---- Savings accounts ----

    views.selectAnalysis();
    seriesAnalysis.select("Savings accounts");
    seriesAnalysis.checkRightClickOptions("Main accounts",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("Savings accounts", "Show transactions in Accounts view");
    savingsAccounts.checkSelectedAccounts("ING");
    mainAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("10/06/2009", TransactionType.PRELEVEMENT, "VIRT SUR ING", "", -200.00, "Virt vers livret")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    // ---- BudgetArea ----

    views.selectAnalysis();
    timeline.selectMonth(200907);
    seriesAnalysis.checkRightClickOptions("Income",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("Income", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("15/07/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/07/2009", "John's", "WORLDCO", 320.00)
      .check();
    categorization.checkCustomFilterVisible(true);
    categorization.clearCustomFilter();

    views.selectAnalysis();
    seriesAnalysis.rightClickAndSelect("Income", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/07/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("01/07/2009", TransactionType.VIREMENT, "WORLDCO", "", 320.00, "John's")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    // ---- Series ----

    views.selectAnalysis();
    seriesAnalysis.select("Variable");
    seriesAnalysis.checkRightClickOptions("Groceries",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Edit",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("Groceries", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/07/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/07/2009", "Groceries", "AUCHAN", -140.00)
      .check();
    categorization.checkCustomFilterVisible(true);

    views.selectAnalysis();
    seriesAnalysis.rightClickAndSelect("Groceries", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/07/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -140.00, "Groceries")
      .add("10/07/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    views.selectAnalysis();
    seriesAnalysis.rightClickAndEditSeries("Groceries", "Edit")
      .checkName("Groceries")
      .checkAmount(450.00)
      .validate();

    // ---- Multi-series ----

    views.selectAnalysis();
    seriesAnalysis.select("Recurring");
    String[] series = {"Mobile", "Internet"};
    seriesAnalysis.checkRightClickOptions(series,
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect(series, "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("20/07/2009", "Internet", "FREE", -30.00)
      .add("20/07/2009", "Mobile", "ORANGE", -60.00)
      .check();
    categorization.checkCustomFilterVisible(true);
    categorization.clearCustomFilter();

    views.selectAnalysis();
    seriesAnalysis.rightClickAndSelect(series, "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("20/07/2009", TransactionType.PRELEVEMENT, "ORANGE", "", -60.00, "Mobile")
      .add("20/07/2009", TransactionType.PRELEVEMENT, "FREE", "", -30.00, "Internet")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    // ---- Uncategorized budget area ----

    timeline.selectMonths(200906, 200907);
    views.selectAnalysis();
    seriesAnalysis.select("To categorize");
    seriesAnalysis.checkRightClickOptions("To categorize",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.rightClickAndSelect("To categorize", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.initContent()
      .add("20/06/2009", "", "UNKNOWN1", 50.00)
      .add("20/06/2009", "", "UNKNOWN2", -100.00)
      .add("20/07/2009", "", "UNKNOWN3", -20.00)
      .check();
    categorization.checkCustomFilterVisible(false);
    categorization.clearCustomFilter();

    views.selectAnalysis();
    seriesAnalysis.rightClickAndSelect("To categorize", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("20/07/2009", TransactionType.PRELEVEMENT, "UNKNOWN3", "", -20.00)
      .add("20/06/2009", TransactionType.PRELEVEMENT, "UNKNOWN2", "", -100.00)
      .add("20/06/2009", TransactionType.VIREMENT, "UNKNOWN1", "", 50.00)
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();
  }
}
