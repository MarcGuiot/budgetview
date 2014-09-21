package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.RestartTestCase;

public class ProjectRestartTest extends RestartTestCase {
  protected String getCurrentDate() {
    return "2008/08/30";
  }

  public void testDefaultCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/08/30")
      .addTransaction("2008/08/01", 1000.00, "Income")
      .addTransaction("2008/08/15", -150.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();

    projectChart.create();
    currentProject
      .setNameAndDefaultAccount("MyProject", "Account n. 001111")
      .addExpenseItem(0, "Booking", 200808, -200.00)
      .addExpenseItem(1, "Travel", 200810, -100.00)
      .addExpenseItem(2, "Hotel", 200810, -500.00);
    categorization.selectTransaction("RESA")
      .selectExtras().selectSeries("Booking");

    projectChart.checkProjectList("MyProject");
    projectChart.checkProject("MyProject", 200808, 200810, 800.00);
    currentProject.checkProjectGauge(-150.00, -800.00);

    timeline.selectMonth("2008/08");
    budgetView.extras.checkSeries("MyProject", -150.00, -200.00);
    budgetView.getSummary().checkEndPosition(950.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);

    budgetView.getSummary().checkEndPosition(350.00);

    restartApplication();

    projectChart.checkProjectList("MyProject");
    projectChart.checkProject("MyProject", 200808, 200810, 800.00);

    projectChart.select("MyProject");
    currentProject
      .checkProjectGauge(-150.00, -800.00)
      .checkItems("| Booking | Aug | 150.00 | 200.00 |\n" +
                  "| Travel  | Oct | 0.00   | 100.00 |\n" +
                  "| Hotel   | Oct | 0.00   | 500.00 |");

    timeline.selectMonth("2008/08");
    budgetView.extras.checkSeries("MyProject", -150.00, -200.00);
    budgetView.getSummary().checkEndPosition(950.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);
    budgetView.getSummary().checkEndPosition(350.00);
  }

  public void testProjectsWithMultiMonthTransfers() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    mainAccounts.createNewAccount()
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

    mainAccounts.createNewAccount()
      .setName("Savings account 1")
      .selectBank("CIC")
      .setAsSavings()
      .validate();
    OfxBuilder.init(this)
      .addBankAccount("00222", 1000.00, "2008/12/01")
      .addTransaction("2008/12/01", -100.00, "Transfer 1")
      .loadInAccount("Savings account 1");

    projectChart.create();
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
    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    budgetView.savings.checkSeries("Transfer", 0.00, -100.00);

    currentProject
      .toggleAndEditTransfer(0)
      .checkMonthAmounts("| Jan 2009 | 100.00 |\n" +
                         "| Feb 2009 | 200.00 |\n" +
                         "| Mar 2009 | 300.00 |")
      .validate();

    timeline.selectMonth(200901);
    budgetView.savings.checkSeries("Transfer", 0.00, -100.00);

    timeline.selectMonth(200902);
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);

    timeline.selectMonth(200903);
    budgetView.savings.checkSeries("Transfer", 0.00, -300.00);
  }

  public void testDisabledProject() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2008/08/30")
      .addTransaction("2008/08/01", 1000.00, "Income")
      .addTransaction("2008/08/15", -150.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();

    projectChart.create();
    currentProject
      .setNameAndDefaultAccount("MyProject", "Account n. 001111")
      .addExpenseItem(0, "Booking", 200808, -200.00)
      .addExpenseItem(1, "Travel", 200810, -100.00)
      .addExpenseItem(2, "Hotel", 200810, -500.00);
    currentProject.setInactive();
    budgetView.extras.checkNoSeriesShown();

    restartApplication();

    projectChart.checkProjectList("MyProject");
    projectChart.checkProject("MyProject", 200808, 200810, 800.00);
    budgetView.extras.checkNoSeriesShown();

    projectChart.select("MyProject");

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
    budgetView.getSummary().checkEndPosition(950.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);
    budgetView.getSummary().checkEndPosition(350.00);
  }

}
