package com.budgetview.functests.analysis;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class AnalysisSeriesGroupViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/05");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    addOns.activateAnalysis();
    addOns.activateGroups();

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
      .editSubSeries()
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

    analysis.budget();
  }

  public void testSelectingGroupElements() throws Exception {

    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkStackButtonsHidden();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Restaurant", 25.00);

    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");

    analysis.budget().checkBudgetAndSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Food", 250.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00);

    analysis.table().select("Food");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoUpHidden()
      .checkGotoDownToGroupSeriesShown();
    analysis.budget()
      .gotoDown()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00);

    analysis.budget()
      .gotoUp()
      .checkBudgetAndSeriesStacksShown();
    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00, true)
      .checkValue("Recurring", 100.00);
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);

    analysis.budget().seriesChart.select("Food");
    analysis.budget().checkBudgetAndSeriesStacksShown()
      .gotoDown()
      .checkSeriesAndGroupSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00);

    analysis.budget().checkGotoDownHidden();
    analysis.budget().groupSeriesChart.select("Groceries");
    analysis.budget()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoSubSeriesShown()
      .gotoDown()
      .checkGroupSeriesAndSubSeriesStacksShown();
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Other", 175.00)
      .checkValue("Local", 50.00);

    analysis.budget()
      .checkGotoDownHidden()
      .gotoUp()
      .checkSeriesAndGroupSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    analysis.table().select("Restaurant");
    analysis.budget()
      .checkGotoDownHidden()
      .checkSeriesAndGroupSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00)
      .checkValue("Restaurant", 25.00, true);

    analysis.table().select("Groceries");
    analysis.budget()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown()
      .checkGotoSubSeriesShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    analysis.budget()
      .gotoUp()
      .checkBudgetAndSeriesStacksShown()
      .checkGotoUpHidden()
      .checkGotoDownToGroupSeriesShown();
    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00, true)
      .checkValue("Recurring", 100.00);
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);

    analysis.table().select("Local");
    analysis.budget()
      .checkGroupSeriesAndSubSeriesStacksShown()
      .checkGotoDownHidden()
      .checkGotoUpToGroupSeriesShown();
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
    analysis.budget().subSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Other", 175.00)
      .checkValue("Local", 50.00, true);

    analysis.budget()
      .gotoUp()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown()
      .checkGotoSubSeriesShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);
  }

  public void testDeletingAndAddingGroupElements() throws Exception {
    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");

    analysis.table().select("Groceries");
    analysis.budget()
      .checkSeriesAndGroupSeriesStacksShown()
      .checkGotoBudgetShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Food", 250.00, true)
      .checkValue("Leisures", 80.00);
    analysis.budget().groupSeriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 225.00, true)
      .checkValue("Restaurant", 25.00);

    budgetView.variable.addToNewGroup("Groceries", "NewGroup");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown();
    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 330.00)
      .checkValue("Recurring", 100.00);
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("NewGroup", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Food", 25.00);

    budgetView.variable.deleteGroup("NewGroup");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Food", 25.00);

    budgetView.variable.deleteGroup("Food");
    analysis.budget()
      .checkBudgetAndSeriesStacksShown();
    analysis.budget().seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 225.00)
      .checkValue("Energy", 100.00)
      .checkValue("Leisures", 80.00)
      .checkValue("Restaurant", 25.00);
  }
}
