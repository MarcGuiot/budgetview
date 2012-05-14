package org.designup.picsou.functests;

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
      .validate();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setVariable("Auchan", "Food", "Groceries");
    categorization.setVariable("MacDo", "Food", "Restaurant");
    categorization.setVariable("OtherFoods", "Food");

    seriesAnalysis.toggleTable();
  }

  public void test() throws Exception {

    seriesAnalysis.checkBudgetStackShown();

    seriesAnalysis.select("Food");
    seriesAnalysis.checkSubSeriesStackShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);

    seriesAnalysis.subSeriesChart.select("Restaurant");
    seriesAnalysis.checkSubSeriesStackShown();
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

    seriesAnalysis.subSeriesChart.select("Groceries");
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

    seriesAnalysis.gotoBudgetStack();
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    budgetView.variable.gotoAnalysis("Food");
    seriesAnalysis.checkSubSeriesStackShown();
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
    seriesAnalysis.subSeriesChart.select("Restaurant");

    budgetView.variable.editSeries("Food")
      .gotoSubSeriesTab()
      .deleteSubSeriesAndConfirm("Restaurant")
      .validate();

    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 160.00);

    budgetView.variable.editSeries("Food")
      .gotoSubSeriesTab()
      .addSubSeries("FastFood")
      .validate();

    categorization.setVariable("MacDo", "Food", "FastFood");

    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("FastFood", 60.00);
  }

  public void testCreatingUnassigningAndDeletingTransactions() throws Exception {

    seriesAnalysis.select("Food");
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
