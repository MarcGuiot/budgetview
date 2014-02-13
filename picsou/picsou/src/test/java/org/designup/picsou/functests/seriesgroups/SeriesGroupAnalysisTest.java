package org.designup.picsou.functests.seriesgroups;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesGroupAnalysisTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2014/01");
    super.setUp();
    getOperations().openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testATreeNodeIsAddedInTheAnalysisTableForEachGroup() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/10", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/10", -60.00, "EDF")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setNewRecurring("EDF", "Electricity");

    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    // ------ Analysis table -----

    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-320.00", "-880.00", "-1440.00", "", "", "", "")
      .add("Balance", "-120.00", "-590.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "500.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "300.00", "300.00", "300.00", "", "", "", "")
      .add("Food", "70.00", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesAnalysis.toggleExpansion("Groceries");
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-320.00", "-880.00", "-1440.00", "", "", "", "")
      .add("Balance", "-120.00", "-590.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "500.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "300.00", "300.00", "300.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();
    seriesAnalysis.toggleExpansion("Groceries");

    // ------ Add series to group -----

    timeline.selectMonth(201401);
    budgetView.variable.addToGroup("Leisures", "Groceries");
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-320.00", "-880.00", "-1440.00", "", "", "", "")
      .add("Balance", "-120.00", "-590.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "500.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "500.00", "500.00", "500.00", "", "", "", "")
      .add("Food", "70.00", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    // ------ Change amount -----

    views.selectBudget();
    budgetView.variable.editSeries("Food")
      .setAmount(250.00)
      .validate();
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-370.00", "-930.00", "-1490.00", "", "", "", "")
      .add("Balance", "-120.00", "-640.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Food", "70.00", "250.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    // ------ Remove series from group -----

    budgetView.variable.removeFromGroup("Home");
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-370.00", "-930.00", "-1490.00", "", "", "", "")
      .add("Balance", "-120.00", "-640.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "70.00", "450.00", "400.00", "400.00", "", "", "", "")
      .add("Food", "70.00", "250.00", "200.00", "200.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    // ------ Undo : recreates series -----

    operations.undo();
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-370.00", "-930.00", "-1490.00", "", "", "", "")
      .add("Balance", "-120.00", "-640.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Food", "70.00", "250.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    seriesAnalysis.toggleExpansion("Groceries");
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-370.00", "-930.00", "-1490.00", "", "", "", "")
      .add("Balance", "-120.00", "-640.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Groceries", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();

    // ------ Delete group -----

    budgetView.variable.deleteGroup("Groceries");
    seriesAnalysis.initContent()
      .add("Main accounts", "270.00", "-370.00", "-930.00", "-1490.00", "", "", "", "")
      .add("Balance", "-120.00", "-640.00", "-560.00", "-560.00", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "30.00", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Electricity", "", "60.00", "60.00", "60.00", "", "", "", "")
      .add("Variable", "120.00", "550.00", "500.00", "500.00", "", "", "", "")
      .add("Food", "70.00", "250.00", "200.00", "200.00", "", "", "", "")
      .add("Home", "50.00", "100.00", "100.00", "100.00", "", "", "", "")
      .add("Leisures", "", "200.00", "200.00", "200.00", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testChartsShowDataForGroup() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/10", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/10", -60.00, "EDF")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setNewRecurring("EDF", "Electricity");

    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    seriesAnalysis.select("Groceries");

    seriesAnalysis.balanceChart.getLeftDataset().checkEmpty();
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 500.00, true)
      .checkValue("Recurring", 60.00);

    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 300.00, true)
      .checkValue("Leisures", 200.00);

    seriesAnalysis.histoChart
      .checkColumnCount(4)
      .checkDiffColumn(0, "Dec", "2013", 300.00, 120.00)
      .checkDiffColumn(1, "Jan", "2014", 300.00, 80.00, true)
      .checkDiffColumn(2, "Feb", "2014", 300.00, 0.00)
      .checkDiffColumn(3, "Mar", "2014", 300.00, 0.00);
  }

  public void testNavigation() throws Exception {
    fail("tbd: vue analyse + navigation");
  }
}
