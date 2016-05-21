package com.budgetview.functests.analysis;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class AnalysisSubSeriesViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2012/05");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    addOns.activateAnalysis();

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
      .editSubSeries()
      .addSubSeries("Restaurant")
      .addSubSeries("Groceries")
      .addSubSeries("Fouquet's")
      .validate();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setVariable("Auchan", "Food", "Groceries");
    categorization.setVariable("MacDo", "Food", "Restaurant");
    categorization.setVariable("OtherFoods", "Food");

    analysis.budget();
  }

  public void testSelectingSubSeries() throws Exception {

    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkStackButtonsHidden();
    analysis.table().initContent(5)
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
      .add("Transfers", "", "", "")
      .check();

    analysis.table().select("Food");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoSubSeriesShown().gotoDown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);

    analysis.table().checkExpansionEnabled("Salary", false);

    analysis.table().checkExpansionEnabled("Food", true);
    analysis.table().checkExpanded("Food", true);
    analysis.table().toggleExpansion("Food");
    analysis.budget().subSeriesChart.select("Restaurant");
    analysis.table().checkExpanded("Food", true);
    analysis.budget()
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00, true);
    analysis.budget().histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 25.00)
      .checkLineColumn(1, "May", "2012", 60.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);
    analysis.table().checkSelected("Restaurant");

    analysis.budget().subSeriesChart.select("Groceries");
    analysis.budget()
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00, true)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);
    analysis.budget().histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 80.00)
      .checkLineColumn(1, "May", "2012", 100.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);
    analysis.table().checkSelected("Groceries");

    analysis.budget().gotoUp();
    analysis.table().select("Restaurant");
    analysis.budget()
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00, true);
    analysis.budget().histoChart
      .checkColumnCount(4)
      .checkLineColumn(0, "Apr", "2012", 25.00)
      .checkLineColumn(1, "May", "2012", 60.00, true)
      .checkLineColumn(2, "June", "2012", 0.00)
      .checkLineColumn(3, "Jul", "2012", 0.00);

    analysis.budget()
      .gotoUp()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoSubSeriesShown();
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    budgetView.variable.gotoAnalysis("Food");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoSubSeriesShown()
      .gotoDown()
      .checkSubSeriesStackShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);
  }

  public void testDeletingAndAddingSubSeries() throws Exception {

    analysis.table().select("Food");
    analysis.budget().gotoDown();
    analysis.budget().subSeriesChart.select("Restaurant");
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);

    analysis.budget().seriesChart.rightClickAndEditSeries("Food", "Edit")
      .editSubSeries()
      .deleteSubSeriesAndConfirm("Restaurant")
      .validate();

    analysis.table().select("Food");
    analysis.budget()
      .checkSubSeriesStackShown()
      .checkGotoBudgetShown();
    analysis.table().checkSelected("Food");
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 160.00);

    analysis.budget().gotoUp();
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    analysis.budget().gotoDown();
    analysis.table().checkExpanded("Food", true);
    analysis.table().toggleExpansion("Food");
    budgetView.variable.editSeries("Food")
      .editSubSeries()
      .addSubSeries("FastFood")
      .validate();

    views.selectAnalysis();
    analysis.table().select("Balance");
    analysis.budget().checkBudgetAndSeriesStacksShown();

    analysis.table().select("Food");
    analysis.table().checkExpanded("Food", true);
    analysis.budget().gotoDown();

    categorization.setVariable("MacDo", "Food", "FastFood");

    views.selectAnalysis();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("FastFood", 60.00);

    analysis.budget().gotoUp();
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);

    analysis.budget().gotoDown();

    analysis.budget().seriesChart
      .rightClickAndEditSeries("Food", "Edit")
      .editSubSeries()
      .deleteSubSeriesAndConfirm("FastFood")
      .deleteSubSeriesAndConfirm("Groceries")
      .deleteSubSeries("Fouquet's")
      .checkSubSeriesListIsEmpty()
      .validate();

    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkStackButtonsHidden();

    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 260.00, true)
      .checkValue("Recurring", 50.00);
  }

  public void testCreatingUnassigningAndDeletingTransactions() throws Exception {

    analysis.table().select("Food");
    analysis.budget().gotoDown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 60.00);

    categorization.setVariable("MacDo", "Food", "Groceries");

    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
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
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
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
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 100.00)
      .checkValue("Food", 100.00)
      .checkValue("Restaurant", 20.00);

    // -- UNDO --

    operations.undo();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 260.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
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
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
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
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Food", 220.00, true);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 200.00)
      .checkValue("Restaurant", 20.00);
  }

  public void testSubSeriesHiddenWhenSeriesNotShown() throws Exception {

    budgetView.variable.createSeries()
      .setName("MySeries")
      .editSubSeries()
      .addSubSeries("Sub1")
      .addSubSeries("Sub2")
      .validate();

    views.selectAnalysis();
    analysis.budget();

    String[] contentWithoutMySeries = {"Main accounts",
                                       "Balance",
                                       "Savings accounts",
                                       "To categorize",
                                       "Income", "Salary",
                                       "Recurring", "Energy",
                                       "Variable", "Food", "Fouquet's", "Groceries", "Restaurant",
                                       "Extras",
                                       "Transfers"};
    analysis.table().checkRowLabels(contentWithoutMySeries);

    budgetView.variable.editSeries("MySeries")
      .setAmount(500.00)
      .validate();

    String[] contentWithMySeries = {"Main accounts",
                                    "Balance",
                                    "Savings accounts",
                                    "To categorize",
                                    "Income", "Salary",
                                    "Recurring", "Energy",
                                    "Variable", "Food", "Fouquet's", "Groceries", "Restaurant", "MySeries", "Sub1", "Sub2",
                                    "Extras",
                                    "Transfers"};
    analysis.table().checkRowLabels(contentWithMySeries);

    budgetView.variable.editSeries("MySeries")
      .selectAllMonths()
      .setAmount(0.00)
      .validate();
    analysis.table().checkRowLabels(contentWithoutMySeries);
  }
}
