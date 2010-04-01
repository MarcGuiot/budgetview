package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.BalanceChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BalancePanelTest extends LoggedInFunctionalTestCase {

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
      .setNewEnvelope("ed", "courses", 300.)
      .setNewIncome("revenu", "revenue");

    views.selectBudget();
    timeline.selectMonth("2008/08");
    BalanceChecker balance_08 = budgetView.getSummary().openBalancePanel();
    int planned = -100;
    balance_08.checkBalance(500 - 200 - 200 - 100, 500 - 200 - 200 + planned - 25.).close();

    timeline.selectMonth("2008/07");
    BalanceChecker balance_07 = budgetView.getSummary().openBalancePanel();
    balance_07.checkBalance(500 - 200 - 200 - 25, 500 - 200 - 200 - 40.).close();

    timeline.selectMonth("2008/09");
    BalanceChecker balance_09 = budgetView.getSummary().openBalancePanel();
    balance_09.checkBalance(500 - 300 - 200, null).close();

    timeline.selectMonths("2008/07", "2008/08");
    BalanceChecker balance_past = budgetView.getSummary().openBalancePanel();
    balance_past.checkBalance(500 - 200 - 200 + 500 - 200 - 200 - 25, 500. - 200 - 200 - 40 + 500 - 200 - 200 - 25).close();
  }
}
