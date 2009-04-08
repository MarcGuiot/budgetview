package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationGaugeChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class RestartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    setInMemory(false);
    setDeleteLocalPrevayler(false);
    super.setUp();
  }

  public void testTransactionsOnly() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    views.selectHome();
    monthSummary.checkNoSeriesMessage();
    mainAccounts
      .checkEstimatedPosition(0.00)
      .setThreshold(25.00);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();

    restartApplication();

    views.selectHome();
    monthSummary.checkNoSeriesMessage();
    mainAccounts
      .checkEstimatedPosition(0.00)
      .checkLimit(25.00);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();
  }

  public void testSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(1).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    views.selectCategorization();
    categorization.setIncome("Company", "Salary", true);

    views.selectBudget();
    budgetView.income.checkSeries("Salary", 1000.0, 1000.0);

    timeline.selectMonth("2008/09");
    budgetView.income.checkSeries("Salary", 0.0, 1000.0);
    mainAccounts.checkEstimatedPosition(1000.0);
    mainAccounts.openEstimatedPositionDetails().checkIncome(1000.0).close();

    timeline.selectMonth("2008/08");
    views.selectHome();
    monthSummary.checkIncome(1000.0, 1000.0);
    mainAccounts.checkEstimatedPosition(0.0);
    mainAccounts.openEstimatedPositionDetails().checkIncome(0.0).close();

    restartApplication();

    timeline.checkSelection("2008/08");

    views.selectHome();
    monthSummary.checkIncome(1000.0, 1000.0);
    mainAccounts.openEstimatedPositionDetails().checkIncome(0.0).close();

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

  public void testRestartAfterCurrentMonthChanged() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setIncome("Company", "Salaire", true);
    categorization.setEnvelope("Auchan", "Course", MasterCategory.FOOD, true);
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("26/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course", MasterCategory.FOOD)
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course", MasterCategory.FOOD)
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course", MasterCategory.FOOD)
      .check();
    TimeService.setCurrentDate(Dates.parse("2008/09/02"));
    restartApplication();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("26/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course", MasterCategory.FOOD)
      .add("26/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course", MasterCategory.FOOD)
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course", MasterCategory.FOOD)
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course", MasterCategory.FOOD)
      .check();
  }

  public void testChangeDayChangeTransactionFromPlannedToRealAndViceversaForNotImportedAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("100")
      .setDay("5")
      .validate();
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .check();
    mainWindow.dispose();
    mainWindow = null;
    TimeService.setCurrentDate(Dates.parse("2008/09/06"));
    mainWindow = getMainWindow();
    initCheckers();
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("05/11/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("05/09/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .check();
  }

  public void testBackupAndRestore() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setIncome("Company", "Salaire", true);
    categorization.setEnvelope("Auchan", "Course", MasterCategory.FOOD, true);
    views.selectData();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course", MasterCategory.FOOD)
      .check();

    String result = operations.backup(System.getProperty("java.io.tmpdir"));
    views.selectCategorization();
    categorization.getTable().selectRows(0, 1);
    categorization.setUncategorized();
    operations.restore(result);
    views.selectData();
    timeline.selectAll();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course", MasterCategory.FOOD)
      .check();
  }

  public void testBackupAndRestoreWithOtherPassword() throws Exception {
    fail("Demander le mots de passe pour lire un backup si on ne reussi pas a decrypter le snaphot");
  }

  public void testSavingsSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setCategory(MasterCategory.INCOME)
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("300")
      .setDay("25")
      .validate();
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1300);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1600);
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("25/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("25/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("25/08/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalAmounts(0, 0);

    views.selectSavings();
    savingsView.checkAmount("Epargne" ,"CAF", 0, 300);

    restartApplication();

    views.selectSavings();

    savingsView.checkAmount("Epargne" ,"CAF", 0, 300);
    timeline.selectMonth("2008/08");

    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1300);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1600);
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("25/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("25/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("25/08/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .check();
  }

}
