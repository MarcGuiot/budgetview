package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.checkers.analysis.TableAnalysisChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

import java.awt.*;

public class SeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    addOns.activateAnalysis();
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
      .setTargetAccount("Account n. 00000123")
      .selectPositiveAmounts()
      .setAmount(100.00)
      .validate();

    timeline.selectMonth("2008/12");
    budgetView.extras.createSeries()
      .setName("Christmas")
      .setTargetAccount("Account n. 00000123")
      .setAmount(300.00)
      .validate();

    timeline.selectMonth("2008/07");
    budgetView.variable.editSeries("Groceries")
      .setAmount(20)
      .validate();

    timeline.selectMonth("2008/07");
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesAnalysis.checkTableShown();
    seriesAnalysis.budget();
    seriesAnalysis.checkBudgetShown();
    seriesAnalysis.table();
    seriesAnalysis.checkTableShown();
    seriesAnalysis.budget();
    seriesAnalysis.checkBudgetShown();

    TableAnalysisChecker table = seriesAnalysis.table();
    table.initContent()
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

    table.unselectAll();
    table.checkForeground("To categorize", "Jul 2008", "red");

    table.checkForeground("Main accounts", "Jul 2008", "darkRed");
    table.checkForeground("Main accounts", "Aug 2008", "darkGrey");

    table.checkForeground("Groceries", "Jul 2008", "red");
    table.checkForeground("Groceries", "Aug 2008", "0022BB");

    table.checkForeground("Variable", "Jul 2008", "AA0000"); // should be darkRed
    table.checkForeground("Variable", "Aug 2008", "darkGrey");

    table.checkForeground("Recurring", "Jul 2008", "darkGrey");
    table.checkForeground("Recurring", "Aug 2008", "darkGrey");

    table.checkForeground("Energy", "Jul 2008", "0022BB");
    table.checkForeground("Energy", "Aug 2008", "0022BB");
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

    seriesAnalysis.budget();
    seriesAnalysis.table().initContent()
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

    seriesAnalysis.budget();
    seriesAnalysis.table().initContent()
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
    seriesAnalysis.budget();
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.table().checkEmpty(
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

    seriesAnalysis.budget();

    timeline.selectMonth("2008/07");
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.table().checkRow(
      "Christmas", "", "", "", "", "", "", "200.00", ""
    );

    timeline.selectMonth("2008/10");
    seriesAnalysis.table().checkColumnNames(
      "", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009", "Apr 2009"
    );
    seriesAnalysis.table().checkRow(
      "Christmas", "", "", "", "200.00", "", "", "", ""
    );

    timeline.selectMonths("2008/09", "2008/10", "2008/11");
    seriesAnalysis.table().checkColumnNames(
      "", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009", "Mar 2009"
    );
    seriesAnalysis.table().checkRow(
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

    seriesAnalysis.budget();
    seriesAnalysis.table().initContent()
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

    seriesAnalysis.table().editSeries("John's", "Jul 2008")
      .checkPositiveAmountsSelected()
      .setAmount(400.00)
      .setPropagationEnabled()
      .validate();

    seriesAnalysis.table().initContent()
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

    seriesAnalysis.table().editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPropagationDisabled()
      .validate();

    seriesAnalysis.table().initContent()
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

    seriesAnalysis.table().editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .checkAmount(500.00)
      .checkPeriodicity("Every month")
      .editSeries()
      .setRepeatEveryTwoMonths()
      .setEndDate(200812)
      .validate();

    seriesAnalysis.table().initContent()
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

    seriesAnalysis.table().editSeries("John's", "Sep 2008")
      .checkPositiveAmountsSelected()
      .setAmount(500.00)
      .checkPeriodicity("Every two months until december 2008")
      .validate();

    seriesAnalysis.table().initContent()
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
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Income > John's");

    seriesAnalysis.budget().checkBudgetAndSeriesStacksShown();
    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00)
      .checkValue("John's", 320.00, true);

    seriesAnalysis.budget();
    seriesAnalysis.table().checkSelected("John's");

    seriesAnalysis.table().collapseAll();

    views.selectBudget();
    budgetView.income.gotoAnalysis("Mary's");

    seriesAnalysis.table().checkSelected("Mary's");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Income > Mary's");

    seriesAnalysis.budget().checkBudgetAndSeriesStacksShown();
    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00, true)
      .checkValue("John's", 320.00);

    seriesAnalysis.table().checkExpanded("Income", true);
    seriesAnalysis.table().checkSelected("Mary's");
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

    seriesAnalysis.budget();
    seriesAnalysis.table().initContent()
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

    seriesAnalysis.table().editSeries("John's").deleteCurrentSeriesWithConfirmation();

    seriesAnalysis.table().initContent()
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
    seriesAnalysis.budget();
    seriesAnalysis.table().checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );
    seriesAnalysis.table().checkRow(
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
    seriesAnalysis.budget();

    String[] expanded = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring",
                         "Variable", "Groceries", "Meat", "Vegetables",
                         "Extras", "Savings"};

    String[] collapsed = {"Main accounts", "Balance", "Savings accounts", "To categorize",
                          "Income", "Recurring", "Variable", "Extras", "Savings"};

    TableAnalysisChecker table = seriesAnalysis.table();
    table.checkRowLabels(expanded);
    table.checkExpansionEnabled("Main accounts", false);
    table.checkExpansionEnabled("Balance", false);
    table.checkExpansionEnabled("Savings accounts", false);
    table.checkExpansionEnabled("To categorize", false);
    table.checkExpansionEnabled("Income", true);
    table.checkExpansionEnabled("Recurring", false);
    table.checkExpansionEnabled("Groceries", true);
    table.checkExpansionEnabled("Extras", false);
    table.collapseAll();
    table.checkRowLabels(collapsed);
    table.checkExpanded("Income", false);
    table.expandAll();
    table.checkRowLabels(expanded);
    table.checkExpanded("Income", true);
    table.checkExpanded("Groceries", true);

    categorization.setNewRecurring("Free Telecom", "Internet");
    seriesAnalysis.table().checkExpansionEnabled("Recurring", true);
    seriesAnalysis.table().checkExpanded("Recurring", true);

    seriesAnalysis.table().checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                          "Income", "Salary", "Salary 2",
                                          "Recurring", "Internet",
                                          "Variable", "Groceries", "Meat", "Vegetables",
                                          "Extras",
                                          "Savings");

    seriesAnalysis.table().doubleClickOnRow("Income");
    seriesAnalysis.table().checkExpanded("Income", false);
    seriesAnalysis.table().checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                          "Income",
                                          "Recurring", "Internet",
                                          "Variable", "Groceries", "Meat", "Vegetables",
                                          "Extras",
                                          "Savings");

    seriesAnalysis.table().doubleClickOnRow("Groceries");
    seriesAnalysis.table().checkExpanded("Groceries", false);
    seriesAnalysis.table().checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                          "Income",
                                          "Recurring", "Internet",
                                          "Variable", "Groceries",
                                          "Extras",
                                          "Savings");

    seriesAnalysis.table().expandAll();
    seriesAnalysis.table().checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                                          "Income", "Salary", "Salary 2",
                                          "Recurring", "Internet",
                                          "Variable", "Groceries", "Meat", "Vegetables",
                                          "Extras",
                                          "Savings");

    // Expansion disabled for budget areas without series
    categorization.selectTransactions("GlobalCorp", "WorldCo").setUncategorized();
    seriesAnalysis.table().checkExpansionEnabled("Income", true);
    budgetView.income.editSeries("Salary").deleteCurrentSeries();
    budgetView.income.editSeries("Salary 2").selectAllMonths().setAmount(0.00).validate();
    seriesAnalysis.table().checkExpansionEnabled("Income", false);
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
    seriesAnalysis.budget();
    seriesAnalysis.table()
      .toggleExpansion("Groceries")
      .checkRowLabels("Main accounts", "Balance", "Savings accounts", "To categorize",
                      "Income", "Salary", "Salary 2",
                      "Recurring",
                      "Variable", "Groceries",
                      "Extras",
                      "Savings")
      .checkSelected("Groceries");

    seriesAnalysis.table()
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
    seriesAnalysis.budget();
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.table().checkRow(
      "Groceries", "", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00", "100.00"
    );

    seriesAnalysis.table().editSeries("Groceries", "Sep 2008")
      .setAmount(150.00)
      .checkPropagationDisabled()
      .validate();

    seriesAnalysis.table().editSeries("Groceries")
      .setEndDate(200810)
      .validate();

    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );
    seriesAnalysis.table().checkRow(
      "Groceries", "", "100.00", "100.00", "150.00", "100.00", "", "", ""
    );
  }

  public void testOnlySeriesWithDataForTheDisplayedPeriodAreShown() throws Exception {
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .validate();

    seriesAnalysis.budget();
    seriesAnalysis.table().checkNoTableRowWithLabel("Taxes");

    budgetView.recurring.editSeries("Taxes")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    seriesAnalysis.table().checkRow(
      "Taxes", "", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00", "200.00"
    );

    budgetView.recurring.editSeries("Taxes")
      .setEndDate(200808)
      .validate();

    timeline.selectMonth("2008/10");
    seriesAnalysis.table().checkNoTableRowWithLabel("Taxes");

    timeline.selectMonth("2008/08");
    seriesAnalysis.table().checkRow(
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

    seriesAnalysis.budget();
    seriesAnalysis.table().checkRow("Taxes", "", "100.00", "", "", "", "", "", "");
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

    seriesAnalysis.budget();
    seriesAnalysis.table().checkSelectionClipboardExport(
      new int[]{4, 5, 6},
      ""
    );

    seriesAnalysis.table().checkTableClipboardExport(
      ""
    );
  }

  public void testMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    timeline.checkSelection("2008/07");
    seriesAnalysis.budget();
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    seriesAnalysis.checkPreviousMonthSelectionDisabled();
    seriesAnalysis.selectNextMonth();
    timeline.checkSelection("2008/08");
    seriesAnalysis.table().checkColumnNames(
      "", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009", "Feb 2009"
    );

    seriesAnalysis.selectPreviousMonth();
    timeline.checkSelection("2008/07");
    seriesAnalysis.table().checkColumnNames(
      "", "June 2008", "Jul 2008", "Aug 2008", "Sep 2008", "Oct 2008", "Nov 2008", "Dec 2008", "Jan 2009"
    );

    timeline.selectMonth("2009/01");
    seriesAnalysis.checkNextMonthSelectionDisabled();
    seriesAnalysis.selectPreviousMonth();
    timeline.checkSelection("2008/12");
    seriesAnalysis.table().checkColumnNames(
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

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setAccountNumber("333")
      .setPosition(10.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("333", 20, "2008/07/12")
      .addTransaction("2008/07/12", +95.00, "Virt livret")
      .loadInAccount("Livret");

    budgetView.savings.createSavingSeries("To account Livret", "Account n. 00001123", "Livret");
    categorization.setSavings("Virement", "To account Livret");
    categorization.setSavings("Virt livret", "To account Livret");

    seriesAnalysis.budget();
    seriesAnalysis.table().expandAll();
    seriesAnalysis.table().initContent()
      .add("Main accounts", "", "", "", "", "", "", "", "")
      .add("Balance", "", "-95.00", "", "", "", "", "", "")
      .add("Savings accounts", "", "105.00", "105.00", "105.00", "105.00", "105.00", "105.00", "105.00")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "95.00", "", "", "", "", "", "")
//      .add("To account Livret", "", "95.00", "", "", "", "", "", "")
      .check();
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

    seriesAnalysis.table().clearSelection();
    seriesAnalysis.budget().checkBreadcrumb("Overall budget / summary");

    seriesAnalysis.budget();
    seriesAnalysis.table().select("Energy");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Recurring > Energy");

    seriesAnalysis.budget().clickBreadcrumb("Recurring");
    seriesAnalysis.table().checkSelected("Recurring");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Recurring");

    seriesAnalysis.budget().clickBreadcrumb("Overall budget");
    seriesAnalysis.table().checkSelected("Balance");

    seriesAnalysis.table().select("Salary");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Income > Salary");

    // Unselect
    seriesAnalysis.table().clearSelection();
    seriesAnalysis.budget().checkBreadcrumb("Overall budget / summary");

    // Series renaming
    seriesAnalysis.table().select("Internet");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Recurring > Internet");
    seriesAnalysis.table().editSeries("Internet")
      .setName("Net")
      .validate();
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Recurring > Net");

    // Change of budget area
    seriesAnalysis.table().editSeries("Net")
      .changeBudgetArea("Variable")
      .validate();
    seriesAnalysis.budget().checkBreadcrumb("Overall budget / summary");

    // Series deletion
    seriesAnalysis.table().select("Net");
    seriesAnalysis.budget().checkBreadcrumb("Overall budget > Variable > Net");
    seriesAnalysis.table().editSeries("Net")
      .deleteCurrentSeriesWithConfirmation();
    seriesAnalysis.table().checkNoSelection();
    seriesAnalysis.budget().checkBreadcrumb("Overall budget / summary");
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
    categorization.setNewSavings("Virt sur ING", "Virt vers livret", "ING", "Account n. 00000123");

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -450.00);
    categorization.setNewSavings("Virt vers ING", "Virt de livret", "Account n. 00000123", "ING");

    seriesAnalysis.budget();

    // ---- Balance ----

    views.selectAnalysis();
    timeline.selectMonth(200906);
    seriesAnalysis.table().checkRightClickOptions("Balance",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("Balance", "Show transactions in Categorization view");
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
    seriesAnalysis.table().rightClickAndSelect("Balance", "Show transactions in Accounts view");
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
    transactions.checkNoFilterMessageShown();

    // ---- Main accounts ----

    views.selectAnalysis();
    seriesAnalysis.table().select("Main accounts");
    seriesAnalysis.table().checkRightClickOptions("Main accounts",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("Main accounts", "Show transactions in Accounts view");
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
    transactions.checkFilterMessage("2 accounts");
    transactions.clearCurrentFilter();

    // ---- Savings accounts ----

    views.selectAnalysis();
    seriesAnalysis.table().select("Savings accounts");
    seriesAnalysis.table().checkRightClickOptions("Main accounts",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("Savings accounts", "Show transactions in Accounts view");
    savingsAccounts.checkSelectedAccounts("ING");
    mainAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("10/06/2009", TransactionType.PRELEVEMENT, "VIRT SUR ING", "", -200.00, "Virt vers livret")
      .check();
    transactions.checkFilterMessage("Account: ING");
    transactions.clearCurrentFilter();

    // ---- BudgetArea ----

    views.selectAnalysis();
    timeline.selectMonth(200907);
    seriesAnalysis.table().checkRightClickOptions("Income",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("Income", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("15/07/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/07/2009", "John's", "WORLDCO", 320.00)
      .check();
    categorization.checkCustomFilterVisible(true);
    categorization.clearCustomFilter();

    views.selectAnalysis();
    seriesAnalysis.table().rightClickAndSelect("Income", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/07/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("01/07/2009", TransactionType.VIREMENT, "WORLDCO", "", 320.00, "John's")
      .check();
    transactions.checkFilterMessage("2 transactions");
    transactions.clearCurrentFilter();

    // ---- Series ----

    views.selectAnalysis();
    seriesAnalysis.table().select("Variable");
    seriesAnalysis.table().checkRightClickOptions("Groceries",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Edit",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("Groceries", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/07/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/07/2009", "Groceries", "AUCHAN", -140.00)
      .check();
    categorization.checkCustomFilterVisible(true);

    views.selectAnalysis();
    seriesAnalysis.table().rightClickAndSelect("Groceries", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/07/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -140.00, "Groceries")
      .add("10/07/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .check();
    transactions.checkFilterMessage("Envelope");
    transactions.clearCurrentFilter();

    seriesAnalysis.table().rightClickAndEditSeries("Groceries", "Edit")
      .checkName("Groceries")
      .checkAmount(450.00)
      .validate();

    // ---- Multi-series ----

    seriesAnalysis.table().select("Recurring");
    String[] series = {"Mobile", "Internet"};
    seriesAnalysis.table().checkRightClickOptions(series,
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect(series, "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("20/07/2009", "Internet", "FREE", -30.00)
      .add("20/07/2009", "Mobile", "ORANGE", -60.00)
      .check();
    categorization.checkCustomFilterVisible(true);
    categorization.clearCustomFilter();

    views.selectAnalysis();
    seriesAnalysis.table().rightClickAndSelect(series, "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("20/07/2009", TransactionType.PRELEVEMENT, "ORANGE", "", -60.00, "Mobile")
      .add("20/07/2009", TransactionType.PRELEVEMENT, "FREE", "", -30.00, "Internet")
      .check();
    transactions.checkFilterMessage("2 envelopes");
    transactions.clearCurrentFilter();

    // ---- Uncategorized budget area ----

    timeline.selectMonths(200906, 200907);
    views.selectAnalysis();
    seriesAnalysis.table().select("To categorize");
    seriesAnalysis.table().checkRightClickOptions("To categorize",
                                          "Show transactions in Categorization view",
                                          "Show transactions in Accounts view",
                                          "Copy");
    seriesAnalysis.table().rightClickAndSelect("To categorize", "Show transactions in Categorization view");
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
    seriesAnalysis.table().rightClickAndSelect("To categorize", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("20/07/2009", TransactionType.PRELEVEMENT, "UNKNOWN3", "", -20.00)
      .add("20/06/2009", TransactionType.PRELEVEMENT, "UNKNOWN2", "", -100.00)
      .add("20/06/2009", TransactionType.VIREMENT, "UNKNOWN1", "", 50.00)
      .check();
    transactions.checkFilterMessage("3 transactions");
    transactions.clearCurrentFilter();
  }
}
