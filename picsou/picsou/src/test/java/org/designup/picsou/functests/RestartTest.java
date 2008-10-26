package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.checkers.CategorizationGaugeChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class RestartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    setInMemory("false");
    setDeleteLocalPrevayler("false");
    super.setUp();
  }

  public void testTransactionsOnly() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    views.selectHome();
    monthSummary.checkNoSeriesMessage();
    balanceSummary
      .checkBalance(00.00)
      .checkIncome(0.00)
      .checkFixed(0.00)
      .checkSavings(0.00)
      .checkProjects(0.00)
      .checkTotal(0.00)
      .setLimit(25.00, true);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();

    restartApplication();

    views.selectHome();
    monthSummary.checkNoSeriesMessage();
    balanceSummary
      .checkBalance(00.00)
      .checkIncome(0.00)
      .checkFixed(0.00)
      .checkSavings(0.00)
      .checkProjects(0.00)
      .checkTotal(0.00)
      .checkLimit(25.00);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();
  }

  public void testSeries() throws Exception {
    LicenseChecker.enterLicense(mainWindow, "admin", "", 1);

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    views.selectCategorization();
    categorization.setIncome("Company", "Salary", true);

    views.selectBudget();
    budgetView.income.checkSeries("Salary", 1000.0, 1000.0);

    timeline.selectMonth("2008/09");
    budgetView.income.checkSeries("Salary", 0.0, 1000.0);

    timeline.selectMonth("2008/08");
    views.selectHome();
    monthSummary.checkIncome(1000.0, 1000.0);
    balanceSummary.checkIncome(0.0);

    restartApplication();

    timeline.checkSelection("2008/08");

    views.selectHome();
    monthSummary.checkIncome(1000.0, 1000.0);
    balanceSummary.checkIncome(0.0);

    views.selectBudget();
    budgetView.income.checkSeries("Salary", 1000.0, 1000.0);

    timeline.selectMonth("2008/09");
    budgetView.income.checkSeries("Salary", 0.0, 1000.0);

    timeline.selectMonths("2008/08", "2008/09");

    views.selectData();
    transactions.initContent()
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salary", "", 1000.00, "Salary", MasterCategory.INCOME)
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salary", MasterCategory.INCOME)
      .check();
  }

  public void testBudgetView() throws Exception {

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courant")
      .switchToManual()
      .selectAllMonths()
      .setAmount("2500")
      .setCategory(MasterCategory.HEALTH)
      .validate();

    budgetView.income.createSeries()
      .setName("Salaire")
      .switchToManual()
      .selectAllMonths().setAmount("3000")
      .setCategory(MasterCategory.INCOME)
      .validate();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .switchToManual()
      .selectAllMonths().setAmount("100")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.occasional.checkTotalAmount((double)0, 0);
    budgetView.checkBalance((double)400);

    budgetView.recurring.createSeries()
      .setName("Loyer")
      .switchToManual()
      .setAmount("1000")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    double free1 = -600;
    budgetView.occasional.checkTotalAmount((double)0, 0);
    budgetView.checkBalance(free1);

    budgetView.checkHelpMessageDisplayed(true);
    budgetView.hideHelpMessage();

    restartApplication();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    double free = -600;
    budgetView.occasional.checkTotalAmount((double)0, 0);
    budgetView.checkBalance(free);

    budgetView.checkHelpMessageDisplayed(false);
  }

  public void testCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/07/10", -1000.0, "FNAC")
      .load();

    views.selectCategorization();
    
    CategorizationGaugeChecker gauge = categorization.getGauge();
    gauge.checkLevel(1, "100%");
    gauge.checkProgressMessageHidden();

    categorization.setIncome("WorldCo", "Salaire", true);
    gauge.checkLevel(0.5, "50%");
    gauge.checkProgressMessageHidden();

    categorization.showUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"10/07/2008", "", "FNAC", -1000.00}
    });

    restartApplication();

    views.selectCategorization();

    categorization.checkShowsUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"10/07/2008", "", "FNAC", -1000.00}
    });

    categorization.getGauge()
      .checkLevel(0.5, "50%")
      .checkProgressMessageHidden();
  }

  public void testCategorizationProgressMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setOccasional("WorldCo", MasterCategory.INCOME);

    categorization.getGauge().checkCompleteProgressMessageShown();
    categorization.getGauge().hideProgressMessage();
    categorization.getGauge().checkProgressMessageHidden();

    restartApplication();

    views.selectCategorization();
    categorization.getGauge().checkProgressMessageHidden();
  }
  
  protected void restartApplication() {
    mainWindow.dispose();
    mainWindow = null;
    mainWindow = getMainWindow();
    initCheckers();
  }
}
