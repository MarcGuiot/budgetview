package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetSummaryDetailsTest extends LoggedInFunctionalTestCase {

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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewEnvelope("FNAC", "Equipment");
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewExtra("Air France", "Trips");
    categorization.setNewSavings("VIRT ING", "Epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "External account");

    double incomeFor200807 = 2200;
    double expensesFor200807 = (30 + 1500) + (300 + 100) + 200 + 100 + 20;
    double balanceFor200807 = incomeFor200807 - expensesFor200807;

    double incomeFor200808 = 2200;
    double expensesFor200808 = 30 + 1500 + 300 + 100 + 100;
    double balanceFor200808 = incomeFor200808 - expensesFor200808;

    double fixed = 30 + 1500;

    timeline.selectMonth("2008/07");
    views.selectHome();
    mainAccounts.checkBalance(balanceFor200807);
    mainAccounts.openEstimatedPositionDetails()
      .checkBalance(balanceFor200807)
      .checkBalanceText("You have to spend 50.00 less")
      .checkBalanceDetails(incomeFor200807, 1530.00, 400.00, 100.00, 200.00)
      .close();

    mainAccounts.changePosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000, "VIRT ING");
    timeline.checkMonthTooltip("2008/07", balanceFor200807, 1000.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEstimatedPosition(1000 + balanceFor200808);

    mainAccounts.openEstimatedPositionDetails()
      .checkTitle("Budget summary for august 2008")
      .checkBalance(balanceFor200808)
      .checkBalanceText("You have 170.00 left to distribute")
      .checkBalanceDetails(incomeFor200808, 1530.00, 400.00, 100.00, 0)
      .checkPositionDescriptionContains("Computation details")
      .checkInitialPosition(1000.00)
      .checkIncome(2200.00)
      .checkFixed(30 + 1500)
      .checkEnvelope(300 + 100)
      .checkSavingsIn(0.00)
      .checkSavingsOut(100.00)
      .checkExtras(0.00)
      .close();

    timeline.selectAll();
    mainAccounts.checkEstimatedPosition(1000 + incomeFor200808 - expensesFor200808);
    mainAccounts.openEstimatedPositionDetails()
      .checkTitle("Budget summary for july - august 2008")
      .checkBalance(balanceFor200807 + balanceFor200808)
      .checkBalanceText("You have 120.00 left to distribute")
      .checkInitialPosition(1000.00)
      .checkIncome(2200.00)
      .checkFixed(30 + 1500)
      .checkEnvelope(300 + 100)
      .checkSavingsOut(100.00)
      .checkSavingsIn(0.00)
      .checkExtras(0.00)
      .close();

    timeline.selectMonth("2008/08");
    views.selectBudget();
    budgetView.extras.createSeries()
      .setName("Trip")
      .setAmount(170)
      .validate();
    
    budgetView.getSummary().openEstimatedPositionDetails()
      .checkTitle("Budget summary for august 2008")
      .checkBalance(0)
      .checkBalanceText("Your budget is balanced")
      .close();
  }

  public void testTwoMonths() throws Exception {
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
    categorization.setNewEnvelope("Auchan", "groceries");
    categorization.setEnvelope("ED", "groceries");
    categorization.setNewEnvelope("FNAC", "Equipment");
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectHome();

    double balanceFor200807 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    mainAccounts.checkBalance(balanceFor200807);

    mainAccounts.checkEstimatedPosition(1529.90);

    timeline.checkMonthTooltip("2008/07", balanceFor200807, 1529.90);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("Free", "internet")
      .setRecurring("Loyer", "rental");

    views.selectHome();
    double balanceFor200808 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    mainAccounts.checkBalance(balanceFor200808);
    mainAccounts.checkEstimatedPosition(1410.00);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(0.0)
      .checkFixed(0)
      .checkEnvelope(90)
      .checkIncome(1500)
      .close();
    timeline.checkMonthTooltip("2008/08", balanceFor200808, 1410.00);

    timeline.selectMonths("2008/07", "2008/08");
    mainAccounts
      .checkBalance((1500 + 1500) - ((29.9 + 1500 + 60 + 20 + 10) + (29.9 + 1500 + 60 + 20 + 10)));
    mainAccounts.checkEstimatedPosition(1410);

    mainAccounts.openEstimatedPositionDetails()
      .checkPositionDate("31/08/2008")
      .checkInitialPosition(0)
      .checkIncome(1500)
      .checkFixed(0)
      .checkEnvelope(90)
      .close();

    timeline.selectMonth("2008/09");
    mainAccounts.checkEstimatedPosition(1420 + 1500 - 1529.90 - 80 - 10 - 10);

    mainAccounts.openEstimatedPositionDetails()
      .checkPositionDate("30/09/2008")
      .checkInitialPosition(1410)
      .checkIncome(1500)
      .checkFixed(1529.90)
      .checkEnvelope(90)
      .close();
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "Salary");

    timeline.selectMonth("2008/07");
    views.selectHome();
    mainAccounts.openEstimatedPositionDetails()
      .checkTitle("Budget summary for july 2008")
      .checkBalance(1000.00)
      .checkPosition(500)
      .checkNoPositionDetails()
      .checkPositionDescriptionContains("observed")
      .close();
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
    categorization.setNewEnvelope("remboursement", "secu");
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectHome();

    double balanceFor200807 = 1500 - (200 - 40);
    mainAccounts.checkBalance(balanceFor200807);

    timeline.selectMonth("2008/08");
    views.selectHome();
    double balanceFor200808 = 1500 - (200 - 40);
    mainAccounts.checkBalance(balanceFor200808);
    mainAccounts.checkEstimatedPosition(1440.00);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(0.0)
      .checkFixed(100)
      .checkEnvelope(-40)
      .checkIncome(1500)
      .close();
    timeline.checkMonthTooltip("2008/08", balanceFor200808, 1440.00);
  }

  public void testDetailForInAndOutOfSavings() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.00, "2008/08/05")
      .addTransaction("2008/08/01", 1500, "WorldCo")
      .addTransaction("2008/08/05", -20, "Virement vers Livret A")
      .addTransaction("2008/08/05", 200, "Virement du Livret A")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewSavings("Virement vers Livret A", "Epargne",
                                 OfxBuilder.DEFAULT_ACCOUNT_NAME, "External account");
    categorization.setNewSavings("Virement du Livret A", "Finanencement tele",
                                 "External account", OfxBuilder.DEFAULT_ACCOUNT_NAME);

    timeline.selectMonth("2008/08");
    views.selectBudget();
    budgetView.savings.editSeries("Epargne")
      .switchToManual()
      .selectAllMonths()
      .setAmount("50")
      .validate();

    budgetView.savings.editSeries("Finanencement tele")
      .switchToManual()
      .selectAllMonths()
      .setAmount("300")
      .validate();

    views.selectHome();
    mainAccounts.openEstimatedPositionDetails()
      .checkPositionDate("31/08/2008")
      .checkInitialPosition(1000)
      .checkSavingsOut(30)
      .checkSavingsIn(100)
      .close();

    timeline.selectMonth("2008/09");
    mainAccounts.openEstimatedPositionDetails()
      .checkPositionDate("30/09/2008")
      .checkInitialPosition(1070)
      .checkSavingsOut(50)
      .checkSavingsIn(300)
      .close();
  }

  public void testThresholdDisplayedAndSettableFromThePositionDetailsDialog() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder.init(this)
      .addBankAccount(30066, 10674, "0123456", 1500.00, "2008/08/30")
      .addTransaction("2008/07/15", -200, "FNAC")
      .addTransaction("2008/08/15", -500, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "groceries");
    categorization.setNewEnvelope("FNAC", "Equipment");

    views.selectHome();

    timeline.selectMonth("2008/07");
    mainAccounts.openEstimatedPositionDetails()
      .checkPosition(2000.00)
      .checkThreshold(0.00, "The position is greater than the threshold", 2000.00)
      .checkThresholdHelpAvailable()
      .setThreshold(3000.00)
      .checkThreshold(3000.00, "The position is less than the threshold", -1000.00)
      .close();
    mainAccounts.checkThreshold(0.00);

    timeline.selectMonth("2008/08");
    mainAccounts.openEstimatedPositionDetails()
      .checkPosition(1300.00)
      .checkThreshold(0.00, "The position is greater than the threshold", 1300.00)
      .setThreshold(1500.00)
      .checkThreshold(1500.00, "The position is less than the threshold", -200.00)
      .validate();
    mainAccounts.checkThreshold(1500.00);

    mainAccounts.openEstimatedPositionDetails()
      .clearThreshold()
      .validate();
    mainAccounts.checkThreshold(0.00);

    mainAccounts.openEstimatedPositionDetails()
      .checkPosition(1300.00)
      .checkThreshold(0.00, "The position is greater than the threshold", 1300.00)
      .setThreshold(1500.00)
      .checkThreshold(1500.00, "The position is less than the threshold", -200.00)
      .validate();
    mainAccounts.checkThreshold(1500.00);

    timeline.selectMonths("2008/07", "2008/08");
    mainAccounts.openEstimatedPositionDetails()
      .checkPosition(1300.00)
      .checkThreshold(1500.00, "The position is less than the threshold", -200.00)
      .setThreshold(1300.00)
      .checkThreshold(1300.00, "The position is equal to the threshold", 0.00)
      .validate();
    mainAccounts.checkThreshold(1300.00);
  }
}
