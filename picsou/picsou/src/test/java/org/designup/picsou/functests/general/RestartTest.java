package org.designup.picsou.functests.general;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.RestartTestCase;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.ColorTheme;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RestartTest extends RestartTestCase {

  protected String getCurrentDate() {
    return "2008/08/30";
  }

  public void testTransactionsOnly() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    views.selectHome();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();

    restartApplication();

    operations.checkUndoNotAvailable();

    views.selectHome();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);

    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00)
      .check();
  }

  public void testSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(1).validate();
    addOns.activateAnalysis();

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .load();

    categorization.setNewIncome("Company", "Salary");
    budgetView.income.checkSeries("Salary", 1000.0, 1000.0);

    timeline.selectMonth("2008/09");
    budgetView.income.checkSeries("Salary", 0.0, 1000.0);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000.0);
    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    seriesAnalysis.budget().balanceChart.getRightDataset()
      .checkEmpty();

    timeline.selectMonth("2008/08");
    budgetView.income.checkTotalAmounts(1000.0, 1000.0);

    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.0);

    restartApplication();

    timeline.checkSelection("2008/08");

    budgetView.income.checkTotalAmounts(1000.0, 1000.0);

    seriesAnalysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1000.00);
    seriesAnalysis.budget().balanceChart.getRightDataset()
      .checkEmpty();
    budgetView.income.checkSeries("Salary", 1000.0, 1000.0);

    timeline.selectMonth("2008/09");
    budgetView.income.checkSeries("Salary", 0.0, 1000.0);

    timeline.selectMonths("2008/08", "2008/09");

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salary", "", 1000.00, "Salary")
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salary")
      .check();
  }

  public void testNotes() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .addTransaction("2008/05/15", -100, "FNAC")
      .load();

    views.selectHome();
    notes.setText("A little note");

    restartApplication();

    views.selectHome();
    notes.checkText("A little note");
  }

  public void testBudgetView() throws Exception {

    accounts.createMainAccount("Manual", 0);
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Courant")
      .setTargetAccount("Manual")
      .selectAllMonths()
      .setAmount("2500")
      .validate();

    budgetView.income.createSeries()
      .setName("Salaire")
      .setTargetAccount("Manual")
      .selectAllMonths()
      .setAmount("3000")
      .validate();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .selectAllMonths()
      .setTargetAccount("Manual")
      .setAmount("100")
      .validate();

    budgetView.recurring.createSeries()
      .setName("Loyer")
      .setTargetAccount("Manual")
      .setAmount("1000")
      .validate();

    mainAccounts.checkEndOfMonthPosition("Manual", -600.00);
    budgetView.recurring.clickTitleSeriesName().checkOrder("EDF", "Loyer");

    restartApplication();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    mainAccounts.checkEndOfMonthPosition("Manual", -600.00);
    budgetView.recurring.checkOrder("EDF", "Loyer");
  }

  public void testCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/07/10", -1000.0, "FNAC")
      .load();

    views.selectCategorization();

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
    gauge.checkLevel(1);
    categorization
      .selectTableRow(0)
      .selectIncome()
      .checkDescriptionShown()
      .hideDescription();
    checkNoSignpostVisible();

    categorization.setNewIncome("WorldCo", "Salaire");
    gauge.checkLevel(0.5);

    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");
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

    categorization.getCompletionGauge().checkLevel(0.5);
    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");

    categorization
      .selectTableRow(0)
      .selectIncome()
      .checkDescriptionHidden();

    checkNoSignpostVisible();
  }

  public void testProjects() throws Exception {
    addOns.activateProjects();

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
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 950.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);

    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 350.00);

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
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 950.00);

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("MyProject", 0, -600.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 350.00);
  }

  public void testRestartAfterCurrentMonthChanged() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .addTransaction("2008/08/10", -300.0, "ED")
      .addTransaction("2008/08/10", -200.0, "Monop")
      .addTransaction("2008/08/10", -100.0, "Fnac")
      .load();

    // on crée une serie a la main sans l'associé des le debut : du coup le montant initial de la series est a 0
    budgetView.variable.createSeries().setName("End date")
      .selectAllMonths()
      .setAmount("300")
      .validate();
    categorization.setNewIncome("Company", "Salaire")
      .setNewVariable("Auchan", "Course", -400.)
      .setVariable("ED", "End date")
      .setNewVariable("Monop", "Begin and end date", -200.)
      .setNewVariable("Fnac", "Begin date", -100.);
    budgetView.variable.editSeries("End date").setEndDate(200812).validate();
    budgetView.variable.editSeries("Begin and end date")
      .setStartDate(200808)
      .setEndDate(200901)
      .validate();
    budgetView.variable.editSeries("Begin date")
      .setStartDate(200808)
      .validate();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Begin date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "MONOP", "", -200.00, "Begin and end date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "ED", "", -300.00, "End date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();

    operations.checkDataIsOk();

    setCurrentDate("2008/09/02");
    restartApplication();
    operations.openPreferences().checkFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Begin date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "MONOP", "", -200.00, "Begin and end date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "ED", "", -300.00, "End date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();
    operations.checkDataIsOk();

    setCurrentDate("2008/10/02");
    restartApplication();
    operations.openPreferences().checkFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/12/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Begin date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "MONOP", "", -200.00, "Begin and end date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "ED", "", -300.00, "End date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();
    operations.checkDataIsOk();

    setCurrentDate("2009/01/02");
    restartApplication();
    operations.openPreferences().checkFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/03/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/03/2009", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/03/2009", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/02/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/02/2009", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/02/2009", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/01/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/12/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Begin date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "MONOP", "", -200.00, "Begin and end date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "ED", "", -300.00, "End date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();
    operations.checkDataIsOk();

    operations.openPreferences().setFutureMonthsCount(1).validate();
    timeline.selectAll();
    views.selectData();
    transactions
      .initContent()
      .add("27/02/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/02/2009", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/02/2009", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/01/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/01/2009", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/12/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/12/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/11/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/11/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Course", "", -400.00, "Course")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: End date", "", -300.00, "End date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin and end date", "", -200.00, "Begin and end date")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Begin date", "", -100.00, "Begin date")
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Begin date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "MONOP", "", -200.00, "Begin and end date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "ED", "", -300.00, "End date")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();
    operations.checkDataIsOk();
  }

  public void testChangeDayChangesPlannedTransactionsForSavingsSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    accounts.createSavingsAccount("Epargne", 1000.00);
    budgetView.transfer.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("100")
      .setDay("5")
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("11/08/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .check();

    TimeService.setCurrentDate(Dates.parse("2008/09/06"));
    restartApplication();

    fail("v40: cf CurrentMonthTrigger pour gerer le nouveau mois sur globReset ? Il ne faut plus générer de planned sur aout car ce mois est passé");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/11/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF")
      .check();
  }

  public void testSavingsSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    accounts.createSavingsAccount("Epargne", 1000.00);

    budgetView.transfer.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("300")
      .setDay("25")
      .validate();

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1300);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1600);
    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1900);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("11/08/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .check();

    timeline.selectMonth("2008/08");
    budgetView.transfer.checkTotalAmounts(0, 0);

    savingsAccounts.select("Epargne");
    budgetView.transfer.checkSeries("CAF", "0.00", "+300.00");

    restartApplication();

    savingsAccounts.select("Epargne");
    budgetView.transfer.checkSeries("CAF", "0.00", "+300.00");
    timeline.selectMonth("2008/08");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1300);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1600);
    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Epargne", 1900);

    views.selectData();
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .add("11/08/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF")
      .check();
  }

  public void testReloadBankEntity() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("unknown", 111, "111", 1000.00, "2008/08/19")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .addTransaction("2008/08/06", -30.00, "Virement")
      .loadUnknown("Other");

    OfxBuilder.init(this)
      .addCardAccount("123", 1000.00, "2008/08/19")
      .addTransaction("2008/08/06", -30.00, "FNAC")
      .loadOneDeferredCard("Other", "Account n. 111");

    OfxBuilder.init(this)
      .addBankAccount("unknown 222", 222, "222", 1000.00, "2008/08/19")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .loadUnknown("Other");

    // On veut juste verifier que l'import marche toujours.
    restartApplication();
    OfxBuilder.init(this)
      .addBankAccount("unknown 222", 222, "222", 1000.00, "2008/08/19")
      .addTransaction("2008/08/10", -50.00, "Virement")
      .load();
    mainAccounts.checkAccount("Account n. 222", 1000.00, "2008/08/10");
  }

  public void testCorrupt() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/05/01", 1000.00, "Salaire")
      .addTransaction("2008/05/01", -1000.00, "Loyer")
      .addTransaction("2008/06/02", 1000.00, "Salaire")
      .addTransaction("2008/06/02", -1000.00, "Loyer")
      .addTransaction("2008/07/01", 1000.00, "Salaire")
      .addTransaction("2008/07/01", -1000.00, "Loyer")
      .addTransaction("2008/08/06", 1000.00, "Salaire")
      .addTransaction("2008/08/06", -1000.00, "Loyer")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewVariable("Loyer", "Loyer");

    operations.exit();
    File file = new File(getLocalPrevaylerPath(), "data");
    String[] subFiles = file.list();
    for (String fileName : subFiles) {
      if (fileName.matches("[0-9]+")) {
        File file1 = new File(file, fileName);
        for (File journal : file1.listFiles()) {
          long size = journal.length();
          FileOutputStream fileOutputStream = new FileOutputStream(journal, true);
          FileChannel channel = fileOutputStream.getChannel();
          byte[] bytes = "Corruption".getBytes();
          channel.write(ByteBuffer.wrap(bytes), size - 10);
          channel.close();
          fileOutputStream.close();
        }
      }
    }

    mainWindow = null;
    operations = null;

    ApplicationChecker application = new ApplicationChecker();
    mainWindow = application.startWithoutSLA();
    UISpecAssert.waitUntil(mainWindow.containsSwingComponent(JPasswordField.class), 10000);
    LoginChecker.init(mainWindow).checkErrorMessage("data.load.error.journal");
    mainWindow.dispose();
    mainWindow = null;
  }

  public void testMultiSelectionAtStartup() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "Auchan")
      .addTransaction("2008/06/15", -40.0, "EDF")
      .addTransaction("2008/06/16", -30.0, "Monop")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "courses");
    categorization.setNewRecurring("EDF", "electricite");

    restartApplication();
    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.checkMultipleSeriesSelection();
  }

  public void testReconciliation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/20", 1000.00, "WorldCo")
      .addTransaction("2008/08/20", 100.00, "Auchan")
      .load();

    reconciliationAnnotations.show();
    reconciliationAnnotations.toggle("WORLDCO");
    categorization.checkTable(new Object[][]{
      {"-", "20/08/2008", "", "AUCHAN", 100.0},
      {"x", "20/08/2008", "", "WORLDCO", 1000.0}}
    );

    restartApplication();

    reconciliationAnnotations.checkColumnAndMenuShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/08/2008", "", "AUCHAN", 100.0},
      {"x", "20/08/2008", "", "WORLDCO", 1000.0}}
    );
  }

  public void testUserMustSelectAccountForTransactionCreation() throws Exception {

    accounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .setPosition(100.)
      .selectBank("CIC")
      .validate();

    restartApplication();

    transactionCreation
      .show()
      .checkNoErrorMessage()
      .createAndCheckErrorMessage("You must select an account")
      .selectAccount("Cash")
      .setAmount(-12.50)
      .setDay(15)
      .checkMonth("August 2008")
      .setLabel("Transaction 1")
      .create();

    categorization.checkTable(new Object[][]{
      {"15/08/2008", "", "TRANSACTION 1", -12.50}
    });
  }

  public void testColorThemeSelection() throws Exception {
    screen.checkBackgroundColorIsStandard();

    PreferencesChecker preferences = operations.openPreferences();
    preferences.selectColorTheme(ColorTheme.BLUE);
    preferences.validate();
    screen.checkBackgroundColorIsBlue();

    restartApplication();
    screen.checkBackgroundColorIsBlue();

    PreferencesChecker preferences2 = operations.openPreferences();
    preferences2.checkColorThemeSelected(ColorTheme.BLUE);
    preferences2.selectColorTheme(ColorTheme.STANDARD);
    preferences2.validate();

    screen.checkBackgroundColorIsStandard();
  }

  public void testAutoCompletionIsProperlyInitializedOnRestart() throws Exception {
    accounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    transactionCreation.show()
      .checkSelectedAccount("Main")
      .createToBeReconciled(15, "Auchan", -50.00);

    restartApplication();

    transactionCreation.show()
      .selectAccount("Main")
      .setDay(16)
      .checkLabelAutocompletion("Au", "AUCHAN");
  }
}
