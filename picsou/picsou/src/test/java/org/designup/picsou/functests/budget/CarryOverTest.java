package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

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
    budgetView.getSummary().checkEndPosition(200.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOver("Courses");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.variable.checkSeries("Courses", -150.00, -150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    budgetView.getSummary().checkEndPosition(1150.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkCarryOverDisabled("Courses");
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
    budgetView.getSummary().checkEndPosition(150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1050.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesRemainderOver("Courses");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.variable.checkSeries("Courses", -50.00, -50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -150.00);
    budgetView.getSummary().checkEndPosition(1050.00);

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
    budgetView.getSummary().checkEndPosition(50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(950.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesRemainderOver("Courses");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.variable.checkSeries("Courses", 50.00, 50.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -250.00);
    budgetView.getSummary().checkEndPosition(950.00);

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
    budgetView.getSummary().checkEndPosition(200.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .cancel();
    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 100.00 available for next month")
      .selectOption("Carry 100.00 over next month and leave 50.00 this month.")
      .validate();
    budgetView.variable.checkSeries("Courses", -250.00, -200.00);
    budgetView.getSummary().checkEndPosition(200.00);

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(1200.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.carryExpensesOverdrawOverWithConfirm("Courses")
      .checkMessageContains("Nothing is planned for next month for this envelope. " +
                            "Do you want to carry over 50.00 over the following months?")
      .validate();

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    budgetView.getSummary().checkEndPosition(2150.00);
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

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.variable.checkSeries("Courses", -250.00, -250.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    budgetView.getSummary().checkEndPosition(2150.00);

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
      .checkMessageContains("There is no planned amount for this envelope in the next months. Nothing can be carried over.")
      .close();

    budgetView.variable.checkSeries("Courses", -250.00, -100.00);
    budgetView.getSummary().checkEndPosition(200.00);
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

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.variable.checkSeries("Courses", -500.00, -500.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(2200.00);

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
    budgetView.getSummary().checkEndPosition(400.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    budgetView.getSummary().checkEndPosition(1300.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeRemainderOver("Salary");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.income.checkSeries("Salary", 800.00, 800.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1200.00);
    budgetView.getSummary().checkEndPosition(1300.00);

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
    budgetView.getSummary().checkEndPosition(200.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeOverrunOver("Salary");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.income.checkSeries("Salary", 1200.00, 1200.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 800.00);
    budgetView.getSummary().checkEndPosition(900.00);

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
    budgetView.getSummary().checkEndPosition(1700.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 1000.00);
    budgetView.getSummary().checkEndPosition(2600.00);

    timeline.selectMonths("2008/08");
    budgetView.income.carryIncomeRemainderOver("Salary");

    budgetView.getSummary().checkEndPosition(200.00);
    budgetView.income.checkSeries("Salary", -500.00, -500.00);

    timeline.selectMonths("2008/09");
    budgetView.income.checkSeries("Salary", 0.00, 2500.00);
    budgetView.getSummary().checkEndPosition(2600.00);

    timeline.selectMonths("2008/08");
    budgetView.income.checkCarryOverDisabled("Salary");
  }

  public void testNotShownForProjects() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    timeline.selectMonth("2008/08");

    projects.create()
      .setName("My Project")
      .setItem(0, "Item 0", 200808, -10.00)
      .addItem(1, "Item 1", 200808, -10.00)
      .validate();

    budgetView.extras.checkCarryOverDisabled("My Project");
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
    budgetView.getSummary().checkEndPosition(200.00);

    budgetView.variable.editSeries("Courses")
      .checkEndDate("Sep 2008")
      .validate();
    budgetView.variable.checkCarryOverDisabled("Courses");

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -50.00);
    budgetView.getSummary().checkEndPosition(1150.00);
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
    budgetView.getSummary().checkEndPosition(150.00);

    timeline.selectMonths("2008/08");
    budgetView.recurring.carryExpensesRemainderOver("Energy");

    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Energy", 0.00, -50.00);
    budgetView.getSummary().checkEndPosition(1150.00);

    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Energy", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(2050.00);

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
    budgetView.getSummary().checkEndPosition(-750.00);

    timeline.selectMonths("2008/08");
    budgetView.variable.checkSeries("Courses", -50.00, -100.00);
    budgetView.getSummary().checkEndPosition(150.00);

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1050.00);

    timeline.selectMonths("2008/07");
    budgetView.variable.carryExpensesOverdrawOverWithDialog("Courses")
      .checkMessage("Only 50.00 available for next month")
      .validate();
    budgetView.variable.checkSeries("Courses", -250.00, -150.00);

    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Courses", -50.00, -50.00);
    budgetView.getSummary().checkEndPosition(200.00);

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    timeline.selectMonth("2008/07");
    budgetView.variable.carryExpensesOverdrawOverWithConfirm("Courses")
      .checkMessageContains("Nothing is planned for next month for this envelope. " +
                            "Do you want to carry over 100.00 over the following months?")
      .validate();

    timeline.selectMonths("2008/09");
    budgetView.variable.checkSeries("Courses", 0.00, 0.00);
    budgetView.getSummary().checkEndPosition(1200.00);

    timeline.selectMonths("2008/10");
    budgetView.variable.checkSeries("Courses", 0.00, -100.00);
    budgetView.getSummary().checkEndPosition(2100.00);
  }

  public void testSavingsRemainderWithAutoFilledAccount() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(4)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2008/08/20")
      .addTransaction("2008/08/10", -200.00, "Virt ING")
      .load();

    savingsAccounts
      .createNewAccount()
      .checkIsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(500)
      .validate();

    budgetView.savings.editSeries("To account ING")
      .setPropagationEnabled()
      .setAmount(400)
      .validate();

    categorization
      .selectTransaction("VIRT ING")
      .selectSavings()
      .checkActiveSeries("To account ING")
      .selectSeries("To account ING");

    budgetView.savings.checkSeries("To account ING", 200.00, 400.00);

    timeline.selectMonth("2008/08");
    budgetView.savings.carryExpensesRemainderOver("To account ING");
    budgetView.savings.checkSeries("To account ING", 200.00, 200.00);

    timeline.selectMonth("2008/09");
    budgetView.savings.checkSeries("To account ING", 0.00, 600.00);
    
    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: To account ING", 400.00, "To account ING", 1500.00, 1500.00, "ING")
      .add("11/10/2008", "Planned: To account ING", -400.00, "To account ING", 0.00, "Main accounts")
      .add("11/09/2008", "Planned: To account ING", 600.00, "To account ING", 1100.00, 1100.00, "ING")
      .add("11/09/2008", "Planned: To account ING", -600.00, "To account ING", 400.00, "Main accounts")
      .add("10/08/2008", "VIRT ING", 200.00, "To account ING", 500.00, 500.00, "ING")
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

    budgetView.savings.editSeries("To account ING")
      .setPropagationEnabled()
      .setAmount(400)
      .validate();

    categorization
      .selectTransaction("VIRT ING - MAIN")
      .selectSavings()
      .selectSeries("To account ING");

    categorization
      .selectTransaction("VIRT ING - SAVINGS")
      .selectSavings()
      .selectSeries("To account ING");

    budgetView.savings.checkSeries("To account ING", 200.00, 400.00);

    timeline.selectMonth("2008/08");
    budgetView.savings.carryExpensesRemainderOver("To account ING");
    budgetView.savings.checkSeries("To account ING", 200.00, 200.00);

    timeline.selectMonth("2008/09");
    budgetView.savings.checkSeries("To account ING", 0.00, 600.00);

    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: To account ING", 400.00, "To account ING", 1500.00, 1500.00, "ING")
      .add("11/10/2008", "Planned: To account ING", -400.00, "To account ING", 0.00, "Main accounts")
      .add("11/09/2008", "Planned: To account ING", 600.00, "To account ING", 1100.00, 1100.00, "ING")
      .add("11/09/2008", "Planned: To account ING", -600.00, "To account ING", 400.00, "Main accounts")
      .add("10/08/2008", "VIRT ING - SAVINGS", 200.00, "To account ING", 500.00, 500.00, "ING")
      .add("10/08/2008", "VIRT ING - MAIN", -200.00, "To account ING", 1000.00, 1000.00, "Account n. 00000123")
      .check();
  }
}