package com.budgetview.functests.projects;

import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.RestartTestCase;
import org.junit.Test;

public class ProjectRestartTest extends RestartTestCase {
  protected String getCurrentDate() {
    return "2008/08/30";
  }

  protected void setUp() throws Exception {
    super.setUp();
    addOns.activateProjects();
  }

  @Test
  public void testDefaultCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/08/30")
      .addTransaction("2008/08/01", 1000.00, "Income")
      .addTransaction("2008/08/15", -150.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();

    projects.createFirst();
    currentProject.setNameAndValidate("MyProject");
    currentProject
      .addExpenseItem(0, "Booking", 200808, -200.00)
      .addExpenseItem(1, "Travel", 200810, -100.00)
      .addExpenseItem(2, "Hotel", 200810, -500.00);
    categorization.selectTransaction("RESA")
      .selectExtras().selectSeries("Booking");

    projects.checkProjectList("MyProject");
    projects.checkProject("MyProject", 200808, 200810, 800.00);
    currentProject.checkProjectGauge(-150.00, -800.00);

    timeline.selectMonth("2008/08");
    budgetView.extras.checkSeries("MyProject", -150.00, -200.00);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Hotel", -500.00, "Hotel", 350.00, 350.00, "Account n. 001111")
      .add("11/10/2008", "Planned: Travel", -100.00, "Travel", 850.00, 850.00, "Account n. 001111")
      .add("15/08/2008", "Planned: Booking", -50.00, "Booking", 950.00, 950.00, "Account n. 001111")
      .add("15/08/2008", "RESA", -150.00, "Booking", 1000.00, 1000.00, "Account n. 001111")
      .add("01/08/2008", "INCOME", 1000.00, "To categorize", 1150.00, 1150.00, "Account n. 001111")
      .check();

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);

    restartApplication();

    projects.checkProjectList("MyProject");
    projects.checkProject("MyProject", 200808, 200810, 800.00);

    projects.select("MyProject");
    currentProject
      .checkProjectGauge(-150.00, -800.00)
      .checkItems("| Booking | Aug | 150.00 | 200.00 |\n" +
                  "| Travel  | Oct | 0.00   | 100.00 |\n" +
                  "| Hotel   | Oct | 0.00   | 500.00 |");

    timeline.selectMonth("2008/08");
    budgetView.extras.checkSeries("MyProject", -150.00, -200.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Hotel", -500.00, "Hotel", 350.00, 350.00, "Account n. 001111")
      .add("11/10/2008", "Planned: Travel", -100.00, "Travel", 850.00, 850.00, "Account n. 001111")
      .add("15/08/2008", "Planned: Booking", -50.00, "Booking", 950.00, 950.00, "Account n. 001111")
      .add("15/08/2008", "RESA", -150.00, "Booking", 1000.00, 1000.00, "Account n. 001111")
      .add("01/08/2008", "INCOME", 1000.00, "To categorize", 1150.00, 1150.00, "Account n. 001111")
      .check();

  }

  @Test
  public void testProjectsWithMultiMonthTransfers() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    views.selectHome();
    accounts.createNewAccount()
      .setName("Main account 1")
      .selectBank("CIC")
      .setAsMain()
      .validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/12/01")
      .addTransaction("2008/11/01", 1000.00, "Income")
      .addTransaction("2008/12/01", 1000.00, "Income")
      .addTransaction("2008/12/01", 100.00, "Transfer 1")
      .loadInAccount("Main account 1");

    accounts.createNewAccount()
      .setName("Savings account 1")
      .selectBank("CIC")
      .setAsSavings()
      .validate();
    OfxBuilder.init(this)
      .addBankAccount("00222", 1000.00, "2008/12/01")
      .addTransaction("2008/12/01", -100.00, "Transfer 1")
      .loadInAccount("Savings account 1");

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem()
      .editTransfer(0)
      .setLabel("Transfer")
      .setFromAccount("Savings account 1")
      .setToAccount("Main account 1")
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .setMonth(200901)
      .setTableMonthCount(3)
      .setMonthAmount(0, 100.00)
      .setMonthAmount(1, 200.00)
      .setMonthAmount(2, 300.00)
      .validate();
    currentProject
      .addExpenseItem(1, "Expense", 200901, -300.00);

    restartApplication();

    views.selectHome();
    projects.select("Trip");
    currentProject.checkProjectGauge(0.00, -300.00);
    currentProject.checkPeriod("January - March 2009");

    timeline.selectMonth(200901);
    budgetView.extras.checkSeries("Trip", "0.00", "300.00");
    budgetView.transfer.checkSeries("Transfer", "0.00", "+100.00");

    currentProject
      .toggleAndEditTransfer(0)
      .checkMonthAmounts("| Jan 2009 | 100.00 |\n" +
                         "| Feb 2009 | 200.00 |\n" +
                         "| Mar 2009 | 300.00 |")
      .validate();

    timeline.selectMonth(200901);
    budgetView.transfer.checkSeries("Transfer", "0.00", "+100.00");

    timeline.selectMonth(200902);
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");

    timeline.selectMonth(200903);
    budgetView.transfer.checkSeries("Transfer", "0.00", "+300.00");
  }

  @Test
  public void testDisabledProject() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/08/30")
      .addTransaction("2008/08/01", 1000.00, "Income")
      .addTransaction("2008/08/15", -150.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();

    projects.createFirst();
    currentProject.setNameAndValidate("MyProject");
    currentProject
      .addExpenseItem(0, "Booking", 200808, -200.00)
      .addExpenseItem(1, "Travel", 200810, -100.00)
      .addExpenseItem(2, "Hotel", 200810, -500.00);
    currentProject.setInactive();
    budgetView.extras.checkNoSeriesShown();

    restartApplication();

    projects.checkProjectList("MyProject");
    projects.checkProject("MyProject", 200808, 200810, 800.00);
    budgetView.extras.checkNoSeriesShown();

    projects.select("MyProject");

    currentProject.setActive();
    categorization.selectTransaction("RESA")
      .selectExtras().selectSeries("Booking");
    currentProject
      .checkProjectGauge(-150.00, -800.00)
      .checkItems("| Booking | Aug | 150.00 | 200.00 |\n" +
                  "| Travel  | Oct | 0.00   | 100.00 |\n" +
                  "| Hotel   | Oct | 0.00   | 500.00 |");

    timeline.selectMonth("2008/08");
    budgetView.extras.checkSeries("MyProject", -150.00, -200.00);

    transactions.showPlannedTransactions().initAmountContent().add("15/08/2008", "Planned: Booking", -50.00, "Booking", 950.00, 950.00, "Account n. 001111")
      .add("15/08/2008", "RESA", -150.00, "Booking", 1000.00, 1000.00, "Account n. 001111")
      .add("01/08/2008", "INCOME", 1000.00, "To categorize", 1150.00, 1150.00, "Account n. 001111")
      .check();


    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);
  }

  @Test
  public void testRestartWithIncompleteTransfer() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();

    accounts.createNewAccount()
      .setName("Main account 1")
      .selectBank("CIC")
      .setAsMain()
      .validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/12/01")
      .addTransaction("2008/11/01", 1000.00, "Income")
      .addTransaction("2008/12/01", 1000.00, "Income")
      .addTransaction("2008/12/01", 100.00, "Transfer 1")
      .loadInAccount("Main account 1");

    accounts.createNewAccount()
      .setName("Savings account 1")
      .selectBank("CIC")
      .setAsSavings()
      .validate();
    OfxBuilder.init(this)
      .addBankAccount("00222", 1000.00, "2008/12/01")
      .addTransaction("2008/12/01", -100.00, "Transfer 1")
      .loadInAccount("Savings account 1");

    projects.createFirst();
    currentProject.setNameAndValidate("Trip");
    currentProject.addExpenseItem(0, "Travel", 200812, -200.00);
    currentProject
      .addTransferItem()
      .editTransfer(1)
      .setLabel("Transfer");

    budgetView.extras.checkContent("| Trip | 0.00 | 200.00 |");
    budgetView.transfer.checkNoSeriesShown();

    restartApplication();

    projects.select("Trip");
    currentProject.checkProjectGauge(0.00, -200.00);
    currentProject.checkPeriod("December 2008");

    timeline.selectMonth(200812);
    budgetView.extras.checkSeries("Trip", "0.00", "200.00");
    budgetView.extras.checkContent("| Trip | 0.00 | 200.00 |");

    currentProject
      .editTransfer(1)
      .checkFromAccount("Select the source account")
      .checkToAccount("Select the target account")
      .cancel();
    currentProject.deleteItem(1);

    currentProject.checkItems("| Travel | Dec | 0.00 | 200.00 |");
    budgetView.extras.checkContent("| Trip | 0.00 | 200.00 |");
    budgetView.transfer.checkNoSeriesShown();
  }
}
