package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.TransactionType;

public class MonthSummaryTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentMonth("2008/08");
  }

  public void testOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "free telecom")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -60, "Auchan")
      .addTransaction("2008/07/10", -20, "ED")
      .addTransaction("2008/07/11", -10, "fnac")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/07/13", -23, "cheque")
      .addTransaction("2008/07/13", -200, "Air France")
      .addTransaction("2008/07/15", -100, "epargne")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("free telecom", "Internet");
    categorization.setNewRecurring("Loyer", "Rental");
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("ED", "Groceries");
    categorization.setNewEnvelope("fnac", "Equipment");
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewSpecial("Air France", "Trips");
    categorization.setNewSavings("epargne", "Epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "External account");

    double incomeFor200807 = 1500;
    double expensesFor200807 = 29.9 + 1500 + 60 + 20 + 10 + 23 + 200 + 100;
    double balance = incomeFor200807 - expensesFor200807;

    timeline.selectMonth("2008/07");
    views.selectHome();
    mainAccounts.checkBalance(balance);
    mainAccounts.checkBalanceDetails(incomeFor200807, expensesFor200807);

    mainAccounts.changePosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000, "epargne");
    timeline.checkMonthTooltip("2008/07", balance, 1000.00);

    timeline.selectAll();
    mainAccounts
      .checkEstimatedPosition(780.10);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(1000.00)
      .checkIncome(1500.00)
      .checkFixed(-1529.90)
      .checkEnvelope(-90.00)
      .checkSavingsOut(0.00)
      .checkSavingsIn(-100.00)
      .checkProjects(0.00)
      .close();
  }

  public void testTwoMonths() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "free telecom")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -60, "Auchan")
      .addTransaction("2008/07/10", -20, "ED")
      .addTransaction("2008/07/11", -10, "fnac")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/08/07", -29.90, "free telecom")
      .addTransaction("2008/08/08", -1500, "Loyer")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewRecurring("free telecom", "internet");
    categorization.setNewRecurring("Loyer", "rental");
    categorization.setNewEnvelope("Auchan", "groceries");
    categorization.setEnvelope("ED", "groceries");
    categorization.setNewEnvelope("fnac", "Equipment");
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectHome();

    double balanceFor200807 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    mainAccounts.checkBalance(balanceFor200807);

    mainAccounts.checkEstimatedPosition(1529.90);

    timeline.checkMonthTooltip("2008/07", balanceFor200807, 1529.90);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("free telecom", "internet")
      .setRecurring("Loyer", "rental");

    views.selectHome();
    double balanceFor200808 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    mainAccounts.checkBalance(balanceFor200808);
    mainAccounts.checkEstimatedPosition(1410.00);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(0.0)
      .checkFixed(0)
      .checkEnvelope(-90)
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
      .checkEnvelope(-90)
      .close();

    timeline.selectMonth("2008/09");
    mainAccounts.checkEstimatedPosition(1420 + 1500 - 1529.90 - 80 - 10 - 10);

    mainAccounts.openEstimatedPositionDetails()
      .checkPositionDate("30/09/2008")
      .checkInitialPosition(1410)
      .checkIncome(1500)
      .checkFixed(-1529.90)
      .checkEnvelope(-90)
      .close();
  }
}
