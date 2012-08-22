package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SubSeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2012/05");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2012/05/12")
      .addTransaction("2012/05/01", 300.00, "WorldCo")
      .addTransaction("2012/05/05", -50.00, "EDF")
      .addTransaction("2012/04/12", -80.00, "Auchan")
      .addTransaction("2012/04/12", -25.00, "MacDo")
      .addTransaction("2012/04/12", -50.00, "OtherFoods")
      .addTransaction("2012/05/12", -100.00, "Auchan")
      .addTransaction("2012/05/05", -20.00, "MacDo")
      .addTransaction("2012/05/06", -40.00, "MacDo")
      .addTransaction("2012/05/12", -100.00, "OtherFoods")
      .load();

    budgetView.variable.createSeries()
      .setName("Food")
      .gotoSubSeriesTab()
      .addSubSeries("Restaurant")
      .addSubSeries("Groceries")
      .addSubSeries("Fouquet's")
      .validate();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setVariable("Auchan", "Food", "Groceries");
    categorization.setVariable("MacDo", "Food", "Restaurant");
    categorization.setVariable("OtherFoods", "Food");

    seriesAnalysis.toggleTable();
  }

  public void testSelectingSubSeries() throws Exception {

    seriesAnalysis
      .checkBudgetStackShown()
      .checkStackButtonsHidden();
    seriesAnalysis.initContent(5)
      .add("Main accounts", "-115.00", "-125.00", "125.00")
      .add("Balance", "-155.00", "-10.00", "250.00")
      .add("Savings accounts", "", "", "")
      .add("To categorize", "", "", "")
      .add("Income", "", "300.00", "300.00")
      .add("Salary", "", "300.00", "300.00")
      .add("Recurring", "", "50.00", "50.00")
      .add("Energy", "", "50.00", "50.00")
      .add("Variable", "155.00", "260.00", "")
      .add("Food", "155.00", "260.00", "")
      .add("Fouquet's", "", "", "")
      .add("Groceries", "80.00", "100.00", "")
      .add("Restaurant", "25.00", "60.00", "")
      .add("Extras", "", "", "")
      .add("Savings", "", "", "")
      .check();

    seriesAnalysis.select("Food");
    seriesAnalysis
      .checkBudgetStackShown()
      .checkGotoSubSeriesShown()
      .gotoSubSeriesStack()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);

    seriesAnalysis.checkExpansionEnabled("Salary", false);

    seriesAnalysis.checkExpansionEnabled("Food", true);
    seriesAnalysis.checkExpanded("Food", true);
    seriesAnalysis.toggleExpansion("Food");
    seriesAnalysis.subSeriesChart.select("Restaurant");
    seriesAnalysis.checkExpanded("Food", true);
    seriesAnalysis
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00, true);
    seriesAnalysis.histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 25.00)
      .checkLineColumn(1, "May", "2012", 60.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);
    seriesAnalysis.checkSelected("Restaurant");

    seriesAnalysis.subSeriesChart.select("Groceries");
    seriesAnalysis
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00, true)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);
    seriesAnalysis.histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 80.00)
      .checkLineColumn(1, "May", "2012", 100.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);
    seriesAnalysis.checkSelected("Groceries");

    seriesAnalysis.gotoBudgetStack();
    seriesAnalysis.select("Restaurant");
    seriesAnalysis
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00, true);
    seriesAnalysis.histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 25.00)
      .checkLineColumn(1, "May", "2012", 60.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);

    seriesAnalysis
      .gotoBudgetStack()
      .checkBudgetStackShown()
      .checkGotoSubSeriesShown();
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    budgetView.variable.gotoAnalysis("Food");
    seriesAnalysis
      .checkBudgetStackShown()
      .checkGotoSubSeriesShown()
      .gotoSubSeriesStack()
      .checkSubSeriesStackShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);
  }

  public void testDeletingAndAddingSubSeries() throws Exception {

    seriesAnalysis.select("Food");
    seriesAnalysis.gotoSubSeriesStack();
    seriesAnalysis.subSeriesChart.select("Restaurant");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);

    seriesAnalysis.seriesChart.rightClickAndEditSeries("Food", "Edit")
      .gotoSubSeriesTab()
      .deleteSubSeriesAndConfirm("Restaurant")
      .validate();

    seriesAnalysis
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    seriesAnalysis.checkSelected("Food");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 160.00);

    seriesAnalysis.gotoBudgetStack();
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    seriesAnalysis.gotoSubSeriesStack();
    seriesAnalysis.checkExpanded("Food", true);
    seriesAnalysis.toggleExpansion("Food");
    budgetView.variable.editSeries("Food")
      .gotoSubSeriesTab()
      .addSubSeries("FastFood")
      .validate();

    views.checkAnalysisSelected();
    seriesAnalysis.checkExpanded("Food", true);

    categorization.setVariable("MacDo", "Food", "FastFood");

    views.selectAnalysis();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("FastFood", 60.00);

    seriesAnalysis.gotoBudgetStack();
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    seriesAnalysis.gotoSubSeriesStack();
    seriesAnalysis.seriesChart
      .rightClickAndEditSeries("Food", "Edit")
      .gotoSubSeriesTab()
      .deleteSubSeriesAndConfirm("FastFood")
      .deleteSubSeriesAndConfirm("Groceries")
      .deleteSubSeries("Fouquet's")
      .checkSubSeriesListIsEmpty()
      .validate();

    seriesAnalysis
      .checkBudgetStackShown()
      .checkStackButtonsHidden();

    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);
  }

  public void testCreatingUnassigningAndDeletingTransactions() throws Exception {

    seriesAnalysis.select("Food");
    seriesAnalysis.gotoSubSeriesStack();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);

    categorization.setVariable("MacDo", "Food", "Groceries");

    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 160.00)
      .checkValue("Food", 100.00);

    categorization.showSelectedMonthsOnly();
    categorization.initContent()
      .add("12/05/2012", "Food / Groceries", "AUCHAN", -100.00)
      .add("05/05/2012", "Energy", "EDF", -50.00)
      .add("05/05/2012", "Food / Groceries", "MACDO", -20.00)
      .add("06/05/2012", "Food / Groceries", "MACDO", -40.00)
      .add("12/05/2012", "Food", "OTHERFOODS", -100.00)
      .add("01/05/2012", "Salary", "WORLDCO", 300.00)
      .check();

    // -- REASSIGN --

    categorization.selectTableRow(2).selectVariable().selectSubSeries("Food", "Restaurant");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 140.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 20.00);

    // -- SHIFT TRANSACTION --

    categorization.initContent()
      .add("12/05/2012", "Food / Groceries", "AUCHAN", -100.00)
      .add("05/05/2012", "Energy", "EDF", -50.00)
      .add("05/05/2012", "Food / Restaurant", "MACDO", -20.00)
      .add("06/05/2012", "Food / Groceries", "MACDO", -40.00)
      .add("12/05/2012", "Food", "OTHERFOODS", -100.00)
      .add("01/05/2012", "Salary", "WORLDCO", 300.00)
      .check();
    categorization.selectTableRow(3);
    transactionDetails.shift();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 20.00);

    // -- UNDO --

    operations.undo();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 140.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 20.00);

    // -- DELETE --

    categorization.initContent()
      .add("12/05/2012", "Food / Groceries", "AUCHAN", -100.00)
      .add("05/05/2012", "Energy", "EDF", -50.00)
      .add("05/05/2012", "Food / Restaurant", "MACDO", -20.00)
      .add("06/05/2012", "Food / Groceries", "MACDO", -40.00)
      .add("12/05/2012", "Food", "OTHERFOODS", -100.00)
      .add("01/05/2012", "Salary", "WORLDCO", 300.00)
      .check();
    categorization.delete(3).validate();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 20.00);

    // -- UNASSIGN --

    categorization.initContent()
      .add("12/05/2012", "Food / Groceries", "AUCHAN", -100.00)
      .add("05/05/2012", "Energy", "EDF", -50.00)
      .add("05/05/2012", "Food / Restaurant", "MACDO", -20.00)
      .add("12/05/2012", "Food", "OTHERFOODS", -100.00)
      .add("01/05/2012", "Salary", "WORLDCO", 300.00)
      .check();
    categorization.setVariable("AUCHAN", "Food");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 200.00)
      .checkValue("Restaurant", 20.00);
  }
}
