package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.functests.checkers.BalanceChecker;
import org.designup.picsou.functests.checkers.PositionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BalancePositionPanelTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -50.00, "ed")
      .addTransaction("2008/06/30", "2008/07/02", -40.00, "ed")
      .addTransaction("2008/07/10", -200.00, "ed")
      .addTransaction("2008/07/10", -200.00, "loyer")
      .addTransaction("2008/07/20", 500, "revenu")
      .addTransaction("2008/07/30", "2008/08/02", -25.00, "ed")
      .addTransaction("2008/08/10", -200.00, "ed")
      .addTransaction("2008/08/10", -200.00, "loyer")
      .addTransaction("2008/08/20", 500, "revenu")
      .load();

    views.selectCategorization();
    categorization
      .setNewRecurring("loyer", "loyer")
      .setNewVariable("ed", "courses", 300.)
      .setNewIncome("revenu", "revenue");

    views.selectBudget();
    timeline.selectMonth("2008/08");

    BalanceChecker balance_08 = budgetView.getSummary().openBalancePanel();
    balance_08.check(-75., -25., 0., -100.).close();

    PositionChecker position_08 = budgetView.getSummary().openPositionPanel();
    position_08.checkPresent(0., 0., -100, 0, -100).close();

    timeline.selectMonth("2008/07");
    BalanceChecker balance_07 = budgetView.getSummary().openBalancePanel();
    balance_07.check(-135., -15., 75., -75.).close();

    PositionChecker position_07 = budgetView.getSummary().openPositionPanel();
    position_07.checkPast(-75).close();

    timeline.selectMonth("2008/09");
    BalanceChecker balance_09 = budgetView.getSummary().openBalancePanel();
    balance_09.check(-100., null, 0., -100.).close();

    PositionChecker position_09 = budgetView.getSummary().openPositionPanel();
    position_09.checkPresent(-100, 500, -500, 0, -100);
    position_09.checkThreshold(0)
      .changeThreshold(100)
      .checkThreshold(100)
      .close();

    timeline.selectMonths("2008/07", "2008/08");
    BalanceChecker balance_past = budgetView.getSummary().openBalancePanel();
    balance_past.check(-135., -40., 75., -100.).close();

    PositionChecker position_past = budgetView.getSummary().openPositionPanel();
    position_past.checkPresent(0., 0, -100, 0, -100).close();
  }


  public void testThresholdInPosition() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -50.00, "ed")
      .addTransaction("2008/06/30", "2008/07/02", -40.00, "ed")
      .addTransaction("2008/07/10", -200.00, "ed")
      .addTransaction("2008/07/10", -200.00, "loyer")
      .addTransaction("2008/07/20", 500, "revenu")
      .addTransaction("2008/07/30", "2008/08/02", -25.00, "ed")
      .addTransaction("2008/08/10", -200.00, "ed")
      .addTransaction("2008/08/10", -200.00, "loyer")
      .addTransaction("2008/08/20", 500, "revenu")
      .load();

    views.selectCategorization();
    categorization
      .setNewRecurring("loyer", "loyer")
      .setNewVariable("ed", "courses", 300.)
      .setNewIncome("revenu", "revenue");

    views.selectBudget();

    timeline.selectMonth("2008/08");
    PositionChecker position_08 = budgetView.getSummary().openPositionPanel();
    position_08.checkTooMuchExpence()
      .changeThreshold(-100)
      .checkBalanceZeroWithoutSavings()
      .changeThreshold(-200)
      .checkOpenSavings()
      .close();

    views.selectHome();
    savingsAccounts.createSavingsAccount("ING", 100.);

    views.selectBudget();
    timeline.selectMonth("2008/08");
    PositionChecker new_position_08 = budgetView.getSummary().openPositionPanel();
    new_position_08.checkSavingsExpected()
      .changeThreshold(-100)
      .checkBalanceZeroWithSavings()
      .close();
  }

  public void testWithSavings() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -50.00, "ed")
      .addTransaction("2008/07/02", -40.00, "ed")
      .addTransaction("2008/07/10", -200.00, "epargne")
      .addTransaction("2008/07/10", -200.00, "loyer")
      .addTransaction("2008/07/20", 500, "revenu")
      .addTransaction("2008/08/02", -25.00, "ed")
      .addTransaction("2008/08/10", -200.00, "epargne")
      .addTransaction("2008/08/10", -200.00, "loyer")
      .addTransaction("2008/08/20", 500, "revenu")
      .load();

    views.selectCategorization();
    AccountEditionChecker account = categorization
      .setNewRecurring("loyer", "loyer")
      .setNewVariable("ed", "courses", 300.)
      .setNewIncome("revenu", "revenue")
      .selectSavings()
      .createSavingsAccount();
    account.setAccountName("ING")
      .selectBank("Autre")
      .validate();

    categorization.setSavings("epargne", "To account ING");

    timeline.selectMonth("2008/08");
    views.selectBudget();

    PositionChecker position_08 = budgetView.getSummary().openPositionPanel();
    position_08.checkPresent(0, 0, -275, 0, -275);

    views.selectCategorization();
    categorization.showSelectedMonthsOnly();
    categorization.selectTransaction("epargne")
      .selectUncategorized().setUncategorized();
    views.selectBudget();
    position_08 = budgetView.getSummary().openPositionPanel();
    position_08
      .checkInitialPosition(0)
      .checkIncome(0)
      .checkExpense(-275)
      .checkSavingsOut(-200)
      .checkPosition(-475);
  }
}
