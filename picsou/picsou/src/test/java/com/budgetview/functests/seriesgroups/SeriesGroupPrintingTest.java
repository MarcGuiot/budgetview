package com.budgetview.functests.seriesgroups;

import com.budgetview.functests.checkers.printing.BudgetReportChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class SeriesGroupPrintingTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/01");
    super.setUp();
    getOperations().openPreferences().setFutureMonthsCount(6).validate();
    addOns.activateGroups();
  }

  public void testPrinting() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/10", -50.00, "Monoprix")
      .addTransaction("2014/01/12", 1000.00, "WorldCo")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/10", -60.00, "EDF")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewIncome("WORLDCO", "Income");
    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setNewRecurring("EDF", "Electricity");

    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201401);
    operations.openPrint()
      .checkOptions("January 2014", "Year 2014")
      .selectCurrentMonth()
      .print();

    BudgetReportChecker january2014Report = printer.getBudgetReport();
    january2014Report.getOverviewPage().checkTitle("Overview for january 2014");
    january2014Report.getOverviewPage().getOverviewStack()
      .getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    january2014Report.getOverviewPage().getOverviewStack()
      .getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 60.00)
      .checkValue("Variable", 500.00);

    january2014Report.getOverviewPage().getExpensesStack()
      .getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 300.00)
      .checkValue("Leisures", 200.00)
      .checkValue("Electricity", 60.00);

    january2014Report.getOverviewPage().getHistoChart()
      .checkColumnCount(8)
      .checkDiffColumn(0, "D", "2013", 0.00, 120.00)
      .checkDiffColumn(1, "J", "2014", 1000.00, 560.00, true)
      .checkDiffColumn(2, "F", "2014", 1000.00, 560.00)
      .checkDiffColumn(3, "M", "2014", 1000.00, 560.00)
      .checkDiffColumn(4, "A", "2014", 1000.00, 560.00)
      .checkDiffColumn(5, "M", "2014", 1000.00, 560.00)
      .checkDiffColumn(6, "J", "2014", 1000.00, 560.00)
      .checkDiffColumn(7, "J", "2014", 1000.00, 560.00);

    january2014Report.initGaugesPage(1)
      .checkTitle("Budget for january 2014")
      .checkBlockCount(15)
      .checkBudget(0, "Income", "1000.00", "1000.00")
      .checkSeries(1, "Income", "1000.00", "1000.00")
      .checkSeparator(2)
      .checkBudget(3, "Recurring", "60.00", "60.00")
      .checkSeries(4, "Electricity", "60.00", "60.00")
      .checkSeparator(5)
      .checkBudget(6, "Variable", "180.00", "500.00")
      .checkSeries(7, "Groceries", "80.00", "300.00")
      .checkSeries(8, "Food", "80.00", "200.00")
      .checkSeries(9, "Home", "0.00", "100.00")
      .checkSeries(10, "Leisures", "100.00", "200.00")
      .checkSeparator(11)
      .checkBudget(12, "Transfers", "0.00", "0.00")
      .checkSeparator(13)
      .checkBudget(14, "Extras", "0.00", "0.00");

    january2014Report.initTablePage(4)
      .add("Series","Total","Dec 13","Jan 14","Feb 14","Mar 14","Apr 14","May 14","June 14","Jul 14")
      .add("Groceries","2220.00","120.00","300.00","300.00","300.00","300.00","300.00","300.00","300.00")
      .add("Food","1470.00","70.00","200.00","200.00","200.00","200.00","200.00","200.00","200.00")
      .add("Home","750.00","50.00","100.00","100.00","100.00","100.00","100.00","100.00","100.00")
      .add("Leisures","1400.00","","200.00","200.00","200.00","200.00","200.00","200.00","200.00")
      .check();
  }

  public void testCollapsedGroupsAreCollapsedInThePrintedReport() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/10", -50.00, "Monoprix")
      .addTransaction("2014/01/12", 1000.00, "WorldCo")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/10", -60.00, "EDF")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewIncome("WORLDCO", "Income");
    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setNewRecurring("EDF", "Electricity");

    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201401);
    operations.openPrint()
      .checkOptions("January 2014", "Year 2014")
      .selectCurrentMonth()
      .print();

    budgetView.variable.collapseGroup("Groceries");

    BudgetReportChecker january2014Report = printer.getBudgetReport();
    january2014Report.initGaugesPage(1)
      .checkTitle("Budget for january 2014")
      .checkBlockCount(13)
      .checkBudget(0, "Income", "1000.00", "1000.00")
      .checkSeries(1, "Income", "1000.00", "1000.00")
      .checkSeparator(2)
      .checkBudget(3, "Recurring", "60.00", "60.00")
      .checkSeries(4, "Electricity", "60.00", "60.00")
      .checkSeparator(5)
      .checkBudget(6, "Variable", "180.00", "500.00")
      .checkSeries(7, "Groceries", "80.00", "300.00")
      .checkSeries(8, "Leisures", "100.00", "200.00")
      .checkSeparator(9)
      .checkBudget(10, "Transfers", "0.00", "0.00")
      .checkSeparator(11)
      .checkBudget(12, "Extras", "0.00", "0.00");

    january2014Report.initTablePage(4)
      .add("Series","Total","Dec 13","Jan 14","Feb 14","Mar 14","Apr 14","May 14","June 14","Jul 14")
      .add("Groceries","2220.00","120.00","300.00","300.00","300.00","300.00","300.00","300.00","300.00")
      .add("Food","1470.00","70.00","200.00","200.00","200.00","200.00","200.00","200.00","200.00")
      .add("Home","750.00","50.00","100.00","100.00","100.00","100.00","100.00","100.00","100.00")
      .add("Leisures","1400.00","","200.00","200.00","200.00","200.00","200.00","200.00","200.00")
      .check();
  }
}
