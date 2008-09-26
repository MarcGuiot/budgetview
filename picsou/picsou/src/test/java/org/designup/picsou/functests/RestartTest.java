package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.MasterCategory;

public class RestartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    setInMemory("false");
    setDeleteLocalPrevayler("false");
    super.setUp();
  }

  public void testReloadBudgetViewStat() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courant")
      .setManual()
      .selectAllMonths()
      .setAmount("2500")
      .setCategory(MasterCategory.HEALTH)
      .validate();

    budgetView.income.createSeries()
      .setName("Salaire")
      .setManual()
      .selectAllMonths().setAmount("3000")
      .setCategory(MasterCategory.INCOME)
      .validate();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .setManual()
      .selectAllMonths().setAmount("100")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.occasional.checkTotalAmounts(0, 400);

    budgetView.recurring.createSeries()
      .setName("Loyer")
      .setManual()
      .setAmount("1000")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    budgetView.occasional.checkTotalAmounts(0, -600);

    mainWindow.dispose();
    mainWindow = null;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logUser("anonymous", "p@ssword");
    initCheckers();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    budgetView.occasional.checkTotalAmounts(0, -600);
  }
}
