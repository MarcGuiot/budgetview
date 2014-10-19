package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetWizardTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentMonth("2008/08");
  }

  public void testOneMonth() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -30, "Free")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -300, "Auchan")
      .addTransaction("2008/07/11", -100, "FNAC")
      .addTransaction("2008/07/12", 2200, "Salaire")
      .addTransaction("2008/07/13", -20, "cheque")
      .addTransaction("2008/07/13", -200, "Air France")
      .addTransaction("2008/07/15", -100, "VIRT ING")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Loyer", "Rental");
    categorization.setNewVariable("Auchan", "Groceries", -300.);
    categorization.setNewVariable("FNAC", "Equipment", -100.);
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewExtra("Air France", "Trips");
    categorization.setNewSavings("VIRT ING", "Epargne", "Account n. 00001123", "External account");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    double incomeFor200807 = 2200;
    double expensesFor200807 = (30 + 1500) + (300 + 100) + 200 + 100 + 20;
    double balanceFor200807 = incomeFor200807 - expensesFor200807;

    double incomeFor200808 = 2200;
    double expensesFor200808 = 30 + 1500 + 300 + 100 + 100;
    double balanceFor200808 = incomeFor200808 - expensesFor200808;

    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);
    budgetView.income.checkTotalObserved(incomeFor200807);
    budgetView.recurring.checkTotalObserved(-1530.00);
    budgetView.savings.checkTotalObserved(100.);

    views.selectHome();

    mainAccounts.changePosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000, "VIRT ING");
    timeline.checkMonthTooltip("2008/07", -880.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000 + balanceFor200808);

    budgetView.recurring.checkTotalPlanned(-30 - 1500);
    budgetView.variable.checkTotalPlanned(-300 - 100);

    timeline.selectAll();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000 + incomeFor200808 - expensesFor200808);
    budgetView.income.checkTotalPlanned(4400);

    timeline.selectMonth("2008/08");
    budgetView.extras.createSeries()
      .setName("Trip")
      .setTargetAccount("Account n. 00001123")
      .setAmount(170)
      .validate();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000.00);
  }

  public void testTwoMonths() throws Exception {

    addOns.activateAnalysis();
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "Free")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -60, "Auchan")
      .addTransaction("2008/07/10", -20, "ED")
      .addTransaction("2008/07/11", -10, "FNAC")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/08/07", -29.90, "Free")
      .addTransaction("2008/08/08", -1500, "Loyer")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewRecurring("Free", "internet");
    categorization.setNewRecurring("Loyer", "rental");
    categorization.setNewVariable("Auchan", "groceries", -80.);
    categorization.setVariable("ED", "groceries");
    categorization.setNewVariable("FNAC", "Equipment", -10.);
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectBudget();

    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1529.90);

    timeline.checkMonthTooltip("2008/07", 29.90);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("Free", "internet")
      .setRecurring("Loyer", "rental");

    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410.00);

    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1500.00);
    seriesAnalysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 1529.90)
      .checkValue("Variable", 90.00);

    views.selectHome();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    timeline.checkMonthTooltip("2008/08", 0.0);

    timeline.selectMonths("2008/07", "2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 3000.00);
    seriesAnalysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 3059.80)
      .checkValue("Variable", 180.00);

    timeline.selectMonth("2008/09");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1420 + 1500 - 1529.90 - 80 - 10 - 10);
    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1500.00);
    seriesAnalysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 1529.90)
      .checkValue("Variable", 90.00);
  }

  public void testBudgetSummaryDetailsShowsActualPositionInThePast() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.00, "2008/08/05")
      .addTransaction("2008/07/01", 1500, "WorldCo")
      .addTransaction("2008/07/05", -500, "Auchan")
      .addTransaction("2008/08/01", 1500, "WorldCo")
      .addTransaction("2008/08/05", -1000, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "Salary");

    timeline.selectMonth("2008/07");
    views.selectHome();
    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 500);
  }

  public void testWithPositiveEnvelope() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -200, "ED")
      .addTransaction("2008/07/09", 40, "remboursement")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/08/07", -100, "ED")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewRecurring("ED", "courses");
    categorization.setNewVariable("remboursement", "secu", 40.);
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 100.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1440.00);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1440.00);
    timeline.checkMonthTooltip("2008/08", -100.00);
  }
}
