package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationGaugeChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class RestartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
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
    
    operations.checkUndoNotAvailable();

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
    categorization.setNewIncome("Company", "Salary");

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
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salary", "", 1000.00, "Salary")
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salary")
      .check();
  }

  public void testNotes() throws Exception {

    views.selectHome();
    notes.setText("A little note");

    restartApplication();

    views.selectHome();
    notes.checkText("A little note");
  }

  public void testBudgetView() throws Exception {

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courant")
      .switchToManual()
      .selectAllMonths()
      .setAmount("2500")
      .validate();

    budgetView.income.createSeries()
      .setName("Salaire")
      .switchToManual()
      .selectAllMonths().setAmount("3000")
      .validate();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .switchToManual()
      .selectAllMonths().setAmount("100")
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.checkBalance((double)400);

    budgetView.recurring.createSeries()
      .setName("Loyer")
      .switchToManual()
      .setAmount("1000")
      .validate();
    double free1 = -600;
    budgetView.checkBalance(free1);

    budgetView.checkHelpMessageDisplayed(true);
    budgetView.hideHelpMessage();

    restartApplication();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    double free = -600;
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

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
    gauge.checkLevel(1, "100%");
    gauge.checkProgressMessageHidden();

    categorization.setNewIncome("WorldCo", "Salaire");
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

    categorization.getCompletionGauge()
      .checkLevel(0.5, "50%")
      .checkProgressMessageHidden();
  }

  public void testCategorizationProgressMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Income");

    categorization.getCompletionGauge().checkCompleteProgressMessageShown();
    categorization.getCompletionGauge().hideProgressMessage();
    categorization.getCompletionGauge().checkProgressMessageHidden();

    restartApplication();

    views.selectCategorization();
    categorization.getCompletionGauge().checkProgressMessageHidden();
  }

  public void testRestartAfterCurrentMonthChanged() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewEnvelope("Auchan", "Course");
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("26/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();
    setCurrentDate("2008/09/02");
    restartApplication();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("26/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("26/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
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
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF")
      .check();
    TimeService.setCurrentDate(Dates.parse("2008/09/06"));
    restartApplication();

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("05/11/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("05/09/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF")
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF")
      .check();
  }

  public void testSavingsSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("300")
      .setDay("25")
      .validate();

    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Epargne", 1600);

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("25/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("25/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("25/08/2008", TransactionType.PLANNED, "CAF", "", 300.00, "CAF")
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalAmounts(0, 0);

    views.selectSavings();
    savingsView.checkAmount("Epargne" ,"CAF", 300, 300);

    restartApplication();

    views.selectSavings();

    savingsView.checkAmount("Epargne" ,"CAF", 300, 300);
    timeline.selectMonth("2008/08");

    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Epargne", 1600);

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("25/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("25/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("25/08/2008", TransactionType.PLANNED, "CAF", "", 300.00, "CAF")
      .check();
  }
}
