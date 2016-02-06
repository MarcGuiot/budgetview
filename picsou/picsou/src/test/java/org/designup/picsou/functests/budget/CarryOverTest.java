package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.SignpostStatus;

public class CarryOverTest extends LoggedInFunctionalTestCase {

  public void testExpensesOverdraw() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -150.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    budgetView.variable.checkSeries("Courses", -150.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOver("Courses");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkSeries("Courses", -150.00, -150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1150.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
  }

  public void testUndo() throws Exception {
    addOns.activateAll();
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/06/01", 1000.00, "WorldCo")
      .addTransaction("2008/06/03", -75.00, "Auchan")
      .addTransaction("2008/07/01", 1000.00, "WorldCo")
      .addTransaction("2008/07/03", -75.00, "Auchan")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -75.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonth("2008/08");

    budgetView.variable.checkContent("| Courses | 75.00 | 100.00 |");
    mainAccounts.getChart("Account n. 0001234")
      .checkValue(200808, 1, 275.00)
      .checkValue(200808, 3, 200.00)
      .checkValue(200808, 4, 175.00)
      .checkValue(200809, 4, 1075.00)
      .checkValue(200810, 4, 1975.00);

    budgetView.variable.carryExpensesRemainderOver("Courses");

    budgetView.variable.checkContent("| Courses | 75.00 | 75.00 |");
    mainAccounts.getChart("Account n. 0001234")
      .checkValue(200808, 1, 275.00)
      .checkValue(200808, 3, 200.00)
      .checkValue(200809, 4, 1075.00)
      .checkValue(200810, 4, 1975.00);

    operations.undo();

    budgetView.variable.checkContent("| Courses | 75.00 | 100.00 |");
    mainAccounts.getChart("Account n. 0001234")
      .checkValue(200808, 1, 275.00)
      .checkValue(200808, 3, 200.00)
      .checkValue(200808, 4, 175.00)
      .checkValue(200809, 4, 1075.00)
      .checkValue(200810, 4, 1975.00);
  }

  public void testExpensesRemainder() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -50.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkSeries("Courses", -50.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1050.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesRemainderOver("Courses");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkSeries("Courses", -50.00, -50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -150.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1050.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
  }

  public void testExpensesWithPositiveValue() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", 50.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkSeries("Courses", 50.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 950.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesRemainderOver("Courses");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkSeries("Courses", 50.00, 50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -250.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 950.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
  }

  public void testExpensesOverdrawExceedsPlannedForNextMonth() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -250.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .cancel();
    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1100.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .selectOption("Carry 100.00 over next month and leave 50.00 this month.")
      .validate();
    budgetView.variable.checkSeries("Courses", -250.00, -200.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1200.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithConfirm("Courses")
      .checkMessageContains("Nothing is planned for next month for this envelope. " +
                            "Do you want to carry over 50.00 over the following months?")
      .validate();

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2150.00);
  }

  public void testExpensesOverdrawCarriedOverSeveralMonths() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -250.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .selectOption("Carry 150.00 over the next months.")
      .validate();

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkSeries("Courses", -250.00, -250.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2150.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
  }

  public void testExpensesOverdrawWithNoPossibilityForCarryingOverInTheNextMonths() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -250.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    budgetView.variable.editSeries("Courses")
      .selectMonth(200809)
      .setPropagationEnabled()
      .setAmount(0.00)
      .validate();

    timeline.selectMonth("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithMessage("Courses")
      .checkInfoMessageContains("There is no planned amount for this envelope in the next months. Nothing can be carried over.")
      .close();

    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkCarryExpensesOverdrawOverEnabled("Courses");
  }

  public void testExpensesOverdrawCarriedOverSeveralMonthsWithRemainder() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -500.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .selectOption("Carry 400.00 over the next months.")
      .validate();

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.variable.checkSeries("Courses", -500.00, -500.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2200.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
  }

  public void testIncomeRemainder() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 800.00, "WorldCo")
      .addTransaction("2008/08/03", -100.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkSeries("Salary", 800.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 400.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1300.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeRemainderOver("Salary");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.income.checkSeries("Salary", 800.00, 800.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1200.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1300.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkCarryOverDisabled("Salary");
  }

  public void testIncomeOverrun() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1200.00, "WorldCo")
      .addTransaction("2008/08/03", -100.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkSeries("Salary", 1200.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1100.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeOverrunOver("Salary");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.income.checkSeries("Salary", 1200.00, 1200.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 800.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 900.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkCarryOverDisabled("Salary");
  }

  public void testIncomeWithNegativeValue() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", -500.00, "WorldCo")
      .addTransaction("2008/08/03", -100.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkSeries("Salary", -500.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1700.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2600.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeRemainderOver("Salary");

    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);
    budgetView.income.checkSeries("Salary", -500.00, -500.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 2500.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2600.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkCarryOverDisabled("Salary");
  }

  public void testNotShownForProjectGroups() throws Exception {
    addOns.activateProjects();
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", -500.00, "WorldCo")
      .addTransaction("2008/08/03", -100.00, "Auchan")
      .load();
    operations.hideSignposts();

    timeline.selectMonth("2008/08");

    views.selectHome();
    projects.createFirst();
    currentProject
      .setNameAndValidate("My Project")
      .addExpenseItem(0, "Item 0", 200808, -10.00)
      .addExpenseItem(1, "Item 1", 200808, -10.00);

    budgetView.extras.expandGroup("My Project")
      .checkCarryOverNotShown("Item 0");
  }

  public void testPostponesEndDateIfNeededButCannotGoPastLastMonth() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -50.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkCarryOverDisabled("Courses");

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryExpensesRemainderOverEnabled("Courses");

    budgetView.variable.editSeries("Courses")
      .setEndDate(200808)
      .validate();
    budgetView.variable.carryExpensesRemainderOver("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    budgetView.variable.editSeries("Courses")
      .checkEndDate("september 2008")
      .validate();
    budgetView.variable.checkCarryOverDisabled("Courses");

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1150.00);
  }

  public void testUsesNextMonthRegardlessOfPeriodicity() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(4)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -50.00, "EDF")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewRecurring("EDF", "Energy");

    budgetView.recurring.editSeries("Energy")
      .setRepeatEveryTwoMonths()
      .selectAllMonths()
      .setAmount(100)
      .validate();

    budgetView.recurring.checkSeries("Energy", -50.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 150.00);

    timeline.selectMonths("2008/08");
    budgetView.recurring.carryExpensesRemainderOver("Energy");

    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Energy", 0.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1150.00);

    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Energy", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2050.00);

    timeline.selectMonths("2008/08");
    budgetView.recurring.editSeries("Energy")
      .checkRepeatsEveryTwoMonths()
      .cancel();
    budgetView.recurring.checkCarryOverDisabled("Energy");
  }

  public void testExpensesOverdrawCarryOverTakesIntoAccountActualValues() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("0001234", 200.00, "2008/08/20")
      .addTransaction("2008/07/01", 1000.00, "WorldCo")
      .addTransaction("2008/07/03", -250.00, "Auchan")
      .addTransaction("2008/08/01", 1000.00, "WorldCo")
      .addTransaction("2008/08/03", -50.00, "Auchan")
      .load();

    categorization
      .setNewIncome("WorldCo", "Salary", 1000.00)
      .setNewVariable("Auchan", "Courses", -100.00);

    timeline.selectMonths("2008/07");
    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", -750.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkSeries("Courses", -50.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1050.00);

    timeline.selectMonths("2008/07");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 50.00 available for next month")
      .validate();
    budgetView.variable.checkSeries("Courses", -250.00, -150.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Courses", -50.00, -50.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 200.00);

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1100.00);

    timeline.selectMonth("2008/07");
    budgetView.variable.carryExpensesOverdrawOverWithConfirm("Courses")
      .checkMessageContains("Nothing is planned for next month for this envelope. " +
                            "Do you want to carry over 100.00 over the following months?")
      .validate();

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 0001234", 2100.00);
  }

  public void testSavingsRemainderWithNotImportedAccount() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(4)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/08/20")
      .addTransaction("2008/08/10", -200.00, "Virt ING")
      .load();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(500)
      .validate();

    budgetView.transfer.createSavingSeries("To account ING", "Account n. 00000123", "ING");

    budgetView.transfer.editSeries("To account ING")
      .setPropagationEnabled()
      .setAmount(400)
      .validate();

    categorization
      .selectTransaction("VIRT ING")
      .selectTransfers()
      .checkSeriesIsActive("To account ING")
      .selectSeries("To account ING");

    budgetView.transfer.checkSeries("To account ING", "200.00", "400.00");

    timeline.selectMonth("2008/08");
    budgetView.transfer.carryExpensesRemainderOver("To account ING");
    budgetView.transfer.checkSeries("To account ING", "200.00", "200.00");

    timeline.selectMonth("2008/09");
    budgetView.transfer.checkSeries("To account ING", "0.00", "600.00");

    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: To account ING", 400.00, "To account ING", 1700.00, 1700.00, "ING")
      .add("11/10/2008", "Planned: To account ING", -400.00, "To account ING", 0.00, 0.00, "Account n. 00000123")
      .add("11/09/2008", "Planned: To account ING", 600.00, "To account ING", 1300.00, 1300.00, "ING")
      .add("11/09/2008", "Planned: To account ING", -600.00, "To account ING", 400.00, 400.00, "Account n. 00000123")
      .add("11/08/2008", "Planned: To account ING", 200.00, "To account ING", 700.00, 700.00, "ING")
      .add("10/08/2008", "VIRT ING", -200.00, "To account ING", 1000.00, 1000.00, "Account n. 00000123")
      .check();
  }

  public void testSavingsRemainderWithImportedSavingsAccount() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(4)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/08/20")
      .addTransaction("2008/08/10", -200.00, "Virt ING - main")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 14559, "666", 500.0, "2008/08/30")
      .addTransaction("2008/08/10", 200.00, "Virt ING - savings")
      .load();

    mainAccounts
      .edit("Account n. 666")
      .setAsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .validate();

    budgetView.transfer.createSavingSeries("To account ING", "Account n. 00000123", "ING");

    budgetView.transfer.editSeries("To account ING")
      .setPropagationEnabled()
      .setAmount(400)
      .validate();

    categorization
      .selectTransaction("VIRT ING - MAIN")
      .selectTransfers()
      .selectSeries("To account ING");

    categorization
      .selectTransaction("VIRT ING - SAVINGS")
      .selectTransfers()
      .selectSeries("To account ING");

    budgetView.transfer.checkSeries("To account ING", "200.00", "400.00");

    timeline.selectMonth("2008/08");
    budgetView.transfer.carryExpensesRemainderOver("To account ING");
    budgetView.transfer.checkSeries("To account ING", "200.00", "200.00");

    timeline.selectMonth("2008/09");
    budgetView.transfer.checkSeries("To account ING", "0.00", "600.00");

    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: To account ING", 400.00, "To account ING", 1500.00, 1500.00, "ING")
      .add("11/10/2008", "Planned: To account ING", -400.00, "To account ING", 0.00, 0.00, "Account n. 00000123")
      .add("11/09/2008", "Planned: To account ING", 600.00, "To account ING", 1100.00, 1100.00, "ING")
      .add("11/09/2008", "Planned: To account ING", -600.00, "To account ING", 400.00, 400.00, "Account n. 00000123")
      .add("10/08/2008", "VIRT ING - SAVINGS", 200.00, "To account ING", 500.00, 500.00, "ING")
      .add("10/08/2008", "VIRT ING - MAIN", -200.00, "To account ING", 1000.00, 1000.00, "Account n. 00000123")
      .check();
  }
}
