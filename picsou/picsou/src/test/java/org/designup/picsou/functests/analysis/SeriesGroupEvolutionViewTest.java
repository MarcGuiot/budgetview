package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesGroupEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/05");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2014/05/12")
      .addTransaction("2014/04/05", 1000.00, "WorldCo")
      .addTransaction("2014/04/12", -100.00, "EDF")
      .addTransaction("2014/04/12", -50.00, "Auchan")
      .addTransaction("2014/04/13", -75.00, "Carrefour")
      .addTransaction("2014/04/14", -100.00, "Monoprix")
      .addTransaction("2014/04/08", -25.00, "McDo")
      .addTransaction("2014/04/10", -80.00, "FNAC")
      .load();

    budgetView.variable.createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Local")
      .addSubSeries("Other")
      .validate();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setVariable("Auchan", "Groceries", "Local");
    categorization.setVariable("Carrefour", "Groceries", "Other");
    categorization.setVariable("Monoprix", "Groceries", "Other");
    categorization.setNewVariable("McDo", "Restaurant");
    categorization.setNewVariable("FNAC", "Leisures");

    seriesAnalysis.toggleTable();
  }

  public void testSelectingGroupElements() throws Exception {

    seriesAnalysis
      .checkBudgetAndSeriesStacksShown()
      .checkStackButtonsHidden();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Restaurant", 25.00);

    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");

    seriesAnalysis.checkBudgetAndSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Food", 250.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00);

    seriesAnalysis.select("Food");
    seriesAnalysis
      .checkBudgetAndSeriesStacksShown()
      .checkGotoUpHidden()
      .checkGotoDownToGroupSeriesShown();
    seriesAnalysis
      .gotoDown()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00);

    seriesAnalysis
      .gotoUp()
      .checkBudgetAndSeriesStacksShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00, true)
      .checkValue("Recurring", 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);

    seriesAnalysis.seriesChart.select("Food");
    seriesAnalysis.checkBudgetAndSeriesStacksShown()
      .gotoDown()
      .checkSeriesAndGroupSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00);

    seriesAnalysis.checkGotoDownHidden();
    seriesAnalysis.groupSeriesChart.select("Groceries");
    seriesAnalysis
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoSubSeriesShown()
      .gotoDown()
      .checkGroupSeriesAndSubSeriesStacksShown();
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Other", 175.00)
      .checkValue("Local", 50.00);

    seriesAnalysis
      .checkGotoDownHidden()
      .gotoUp()
      .checkSeriesAndGroupSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    seriesAnalysis.select("Restaurant");
    seriesAnalysis
      .checkGotoDownHidden()
      .checkSeriesAndGroupSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00, true);

    seriesAnalysis.select("Groceries");
    seriesAnalysis
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown()
      .checkGotoSubSeriesShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    seriesAnalysis
      .gotoUp()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoUpHidden()
      .checkGotoDownToGroupSeriesShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00, true)
      .checkValue("Recurring", 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);

    seriesAnalysis.select("Local");
    seriesAnalysis
      .checkGroupSeriesAndSubSeriesStacksShown()
      .checkGotoDownHidden()
      .checkGotoUpToGroupSeriesShown();
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
    seriesAnalysis.subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Other", 175.00)
      .checkValue("Local", 50.00, true);

    seriesAnalysis
      .gotoUp()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown()
      .checkGotoSubSeriesShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
  }

  public void testDeletingAndAddingGroupElements() throws Exception {
    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");

    seriesAnalysis.select("Groceries");
    seriesAnalysis
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    seriesAnalysis.groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    budgetView.variable.addToNewGroup("Groceries", "NewGroup");
    seriesAnalysis
      .checkBudgetAndSeriesStacksShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00)
      .checkValue("Recurring", 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("NewGroup", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Food", 25.00);

    budgetView.variable.deleteGroup("NewGroup");
    seriesAnalysis
      .checkBudgetAndSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Food", 25.00);

    budgetView.variable.deleteGroup("Food");
    seriesAnalysis
      .checkBudgetAndSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Restaurant", 25.00);
  }
}
