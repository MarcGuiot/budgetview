package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;

public class BudgetViewTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    super.setUp();
  }

  public void test() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/03", -15.00, "McDo")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00)
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -145.);
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(-84.00, -84.00);
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    budgetView.recurring.checkSeries("Electricity", -55.00, -55.00);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(-145.00, -145);
    budgetView.variable.checkSeries("Groceries", -145.00, -145.00);

    budgetView.income.checkTitle("Income");
    budgetView.income
      .checkTotalAmounts(3740.00, 3540.00)
      .checkTotalGauge(3740.00, 3540.00)
      .checkTotalPositiveOverrun();

    budgetView.income.checkSeries("Salary", 3540.00, 3540.00);
    budgetView.income.checkSeries("Exceptional Income", 200.00, 0.00);

    timeline.selectMonths("2008/08");

    transactions
      .showPlannedTransactions().initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -145.00, "Groceries")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Electricity", "", -55.00, "Electricity")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.00, "Internet")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Salary", "", 3540.00, "Salary")
      .check();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(0.00, -84.00);
    budgetView.recurring.checkSeries("Internet", -0.00, -29.00);
    budgetView.recurring.checkSeries("Electricity", -0.00, -55.00);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(0.00, -145);
    budgetView.variable.checkSeries("Groceries", -0.00, -145.00);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(0.00, 3540.00);
    budgetView.income.checkSeries("Salary", 0.00, 3540.00);
    budgetView.income.checkSeries("Exceptional Income", 0, 0);
  }

  public void testExtraSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .check();

    timeline.checkSelection("2008/07");
    views.selectCategorization();
    categorization.setNewExtra("Auchan", "Anniversaire");

    views.selectBudget();

    budgetView.extras.checkTitle("Extras");
    budgetView.extras.checkTotalAmounts(-95.00, -95.00);
    budgetView.extras.checkSeries("Anniversaire", -95.00, -95.00);

    views.selectCategorization();
    categorization.getExtras().checkSelectedSeries("Anniversaire");
  }

  public void testSavingsSeries() throws Exception {
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -25.00, "Virt Compte Epargne")
      .loadInNewAccount();

    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Virt Compte Epargne", "", -25.00)
      .check();

    categorization.setNewTransfer("Virt Compte Epargne", "Epargne", "Account n. 00001123", "Livret");

    views.selectBudget();
    budgetView.transfer.alignAndPropagate("Epargne");

    budgetView.transfer.checkTitle("Transfers");
    budgetView.transfer.checkSeries("Epargne", "25.00", "25.00");
    budgetView.transfer.checkTotalAmounts("25.00", "25.00");

    categorization.getSavings().checkSelectedSeries("Epargne");

    savingsAccounts.select("Livret");
    budgetView.transfer.checkSeries("Epargne", "0.00", "+25.00");
    budgetView.transfer.checkTotalAmounts("0.00", "+25.00");
  }

  public void testUnset() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");

    views.selectBudget();
    budgetView.variable.checkPlannedUnset("Groceries");

    timeline.selectAll();

    budgetView.variable.checkPlannedUnset("Groceries");
  }

  public void testImportWithUserDateAndBankDateAtNextMonth() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30066, 1234, "000111", 5000.00, "2008/08/15")
      .addTransaction("2008/07/31", "2008/08/02", -100.00, "Auchan")
      .addTransaction("2008/07/30", "2008/08/01", -50.00, "Monoprix")
      .addTransaction("2008/07/29", "2008/08/01", -30.00, "Free Telecom")
      .addTransaction("2008/07/28", "2008/08/01", 3500.00, "WorldCo")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -150.00);
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");

    timeline.selectMonths(200807, 200808);
    transactions.showPlannedTransactions()
      .initAmountContent()
      .add("31/07/2008", "AUCHAN", -100.00, "Groceries", 5000.00, 5000.00, "Account n. 000111")
      .add("30/07/2008", "MONOPRIX", -50.00, "Groceries", 5100.00, 5100.00, "Account n. 000111")
      .add("29/07/2008", "FREE TELECOM", -30.00, "Internet", 5150.00, 5150.00, "Account n. 000111")
      .add("28/07/2008", "WORLDCO", 3500.00, "Salary", 5180.00, 5180.00, "Account n. 000111")
      .check();

    timeline.selectMonths(200807);
    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(-30.00, -30.00);
    budgetView.recurring.checkSeries("Internet", -30.00, -30.00);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(-150.00, -150.00);
    budgetView.variable.checkSeries("Groceries", -150.00, -150.00);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3500.00, 3500.00);
    budgetView.income.checkSeries("Salary", 3500.00, 3500.00);

    mainAccounts.showChart("Account n. 000111");
    mainAccounts.getChart("Account n. 000111")
      .checkRange(200807, 200808)
      .checkCurrentDay(200808, 2)
      .checkValue(200807, 1, 1680.00)
      .checkValue(200808, 1, 5100.00)
      .checkValue(200808, 2, 5000.00);
  }

  public void testAddMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/31", -95.00, "Auchan")
      .addTransaction("2008/07/30", -50.00, "Monoprix")
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .addTransaction("2008/07/28", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("31/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("30/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("29/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("28/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    timeline.checkSpanEquals("2008/07", "2008/08");

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");

    categorization.selectVariable().editSeries("Groceries")
      .selectAllMonths()
      .setAmount("95")
      .validate();
    categorization.selectRecurring().editSeries("Internet")
      .selectAllMonths()
      .setAmount("29.0")
      .validate();
    categorization.selectIncome().editSeries("Salary")
      .selectAllMonths()
      .setAmount("3540.0")
      .validate();

    timeline.selectMonth("2008/07");
    views.selectBudget();

    OfxBuilder.init(this)
      .addTransaction("2008/06/13", -50.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setVariable("Auchan", "Groceries");

    views.selectBudget();
    timeline.checkSelection("2008/06");
    budgetView.recurring.checkTotalAmounts(0.00, -29.00);
    budgetView.recurring.checkSeries("Internet", 0.00, -29.00);

    budgetView.variable.checkTotalAmounts(-50.00, -95.00);
    budgetView.variable.checkSeries("Groceries", -50.00, -95.00);

    budgetView.income.checkTotalAmounts(0.00, 3540.00);
    budgetView.income.checkSeries("Salary", 0.00, 3540.00);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, -3366.00);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);
  }

  public void testSeveralMonthsShowOrNotSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/30", -50.00, "Monoprix")
      .addTransaction("2008/06/14", -95.00, "Auchan")
      .addTransaction("2008/05/29", -29.00, "ED2")
      .addTransaction("2008/04/29", -50.00, "ED1")
      .load();

    timeline.selectAll();

    views.selectBudget();
    budgetView.variable.createSeries().setName("courantED")
      .setEndDate(200805)
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.extras.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.variable.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .selectMonth(200808)
      .setAmount("0")
      .validate();
    views.selectCategorization();
    categorization.setVariable("Monoprix", "courantMonoprix");
    categorization.setExtra("Auchan", "courantAuchan");
    categorization.setVariable("ED1", "courantED");
    categorization.setVariable("ED2", "courantED");

    views.selectBudget();
    timeline.selectMonths("2008/04", "2008/05", "2008/06", "2008/07");

    budgetView.variable
      .checkSeries("courantMonoprix", -50, -200)
      .checkSeries("courantED", -79, -200);

    budgetView.extras.checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/05");
    budgetView.variable
      .checkSeries("courantED", -29, -100)
      .checkSeriesNotPresent("courantMonoprix");
    budgetView.extras
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/06");
    budgetView.variable
      .checkSeries("courantMonoprix", -0, -100)
      .checkSeriesNotPresent("courantED");
    budgetView.extras
      .checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/07");
    budgetView.variable
      .checkSeries("courantMonoprix", -50, -100)
      .checkSeriesNotPresent("courantED");

    budgetView.extras
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/08");
    budgetView.variable
      .checkSeries("courantMonoprix", 0., 0.);
  }

  public void testReimbursementsAreShownWithAPlus() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", 25.00, "Secu")
      .addTransaction("2008/06/29", -30.00, "medecin")
      .load();
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("santé")
      .selectAllMonths()
      .setAmount(30)
      .validate();
    views.selectCategorization();
    timeline.selectAll();

    categorization.selectTableRows(0, 1)
      .selectVariable()
      .selectSeries("santé");

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.variable.checkSeries("santé", 25, -30);

    timeline.selectMonth("2008/06");
    budgetView.variable.checkSeries("santé", -30, -30);

    timeline.selectMonths("2008/06", "2008/07");
    budgetView.variable.checkSeries("santé", -5, -60);
  }

  public void testDeactivatingSeriesBudgets() throws Exception {
    accounts.createNewAccount()
      .setName("Main")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("salaire")
      .selectAllMonths()
      .setAmount("1000")
      .setTargetAccount("Main")
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("salaire", 0, 1000);
    views.selectData();
    timeline.selectLast();
    transactions
      .showPlannedTransactions().initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: salaire", "", 1000.00, "salaire")
      .check();

    views.selectBudget();
    budgetView.income.editSeries("salaire").setRepeatCustom().toggleMonth("Aug").validate();
    budgetView.income.checkSeriesNotPresent("salaire");
    views.selectData();
    timeline.selectLast();
    transactions.initContent().check();
  }

  public void testNavigatingToTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/03", -15.00, "McDo")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewVariable("McDo", "Food");

    views.selectBudget();
    budgetView.variable.gotoData("Groceries");

    views.checkDataSelected();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();
    transactions.clearCurrentFilter();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Groceries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "FREE TELECOM", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -15.00, "Food")
      .add("02/07/2008", TransactionType.VIREMENT, "WORLDCO - BONUS", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WORLDCO", "", 3540.00)
      .check();

    views.selectBudget();
    budgetView.variable.gotoDataThroughMenu("Groceries");
    views.checkDataSelected();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();
    transactions.clearCurrentFilter();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Groceries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "FREE TELECOM", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -15.00, "Food")
      .add("02/07/2008", TransactionType.VIREMENT, "WORLDCO - BONUS", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WORLDCO", "", 3540.00)
      .check();
  }

  public void testSeriesAreOrderedByDecreasingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .load();

    categorization.setNewVariable("Free Telecom", "Tel");
    categorization.setNewVariable("Auchan", "Auchan");
    categorization.setNewVariable("Monoprix", "Monop");

    budgetView.variable.checkOrder("Auchan", "Monop", "Tel");
  }

  public void testPositiveEnvelopeBudgetDoesNotCreateNegativePlannedTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", 19.00, "Loto")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Loto")
      .selectAllMonths()
      .setAmount("15")
      .setTargetAccount("Account n. 00001123")
      .selectPositiveAmounts()
      .validate();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", 15.00, "Loto")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00)
      .check();
    views.selectCategorization();
    categorization.setVariable("Loto", "Loto");
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();
  }

  public void testInvertSignOfBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", 19.00, "Loto")
      .load();

    budgetView.variable.createSeries()
      .setName("Loto")
      .selectAllMonths()
      .setAmount("15")
      .selectPositiveAmounts()
      .validate();

    categorization.setVariable("Loto", "Loto");

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();

    budgetView.variable
      .editSeries("Loto")
      .selectAllMonths()
      .selectNegativeAmounts()
      .validate();

    transactions
      .initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", -49.00, "Loto")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();

    categorization.selectTransactions("Loto").setUncategorized();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", -15.00, "Loto")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00)
      .check();
  }

  public void testSeriesBudgetEqualZero() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", -19.00, "Auchan")
      .load();

    budgetView.variable.createSeries()
      .setName("ZeroSeries")
      .selectAllMonths()
      .setAmount("0")
      .selectPositiveAmounts()
      .validate();

    categorization.setVariable("Loto", "ZeroSeries");

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "ZeroSeries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00)
      .check();

    categorization
      .selectTransactions("Loto")
      .setUncategorized();
    categorization.setVariable("Auchan", "ZeroSeries");

    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    budgetView.variable.editSeries("ZeroSeries").selectAllMonths().setAmount("10").validate();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    budgetView.variable.editSeries("ZeroSeries").selectAllMonths().setAmount("29").validate();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: ZeroSeries", "", -10.00, "ZeroSeries")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();
  }

  public void testMixPositiveAndNegativeBudgetInTotalBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", -19.00, "Auchan")
      .load();

    budgetView.variable.createSeries()
      .setName("Loto")
      .selectAllMonths()
      .setAmount("100")
      .selectPositiveAmounts()
      .validate();

    budgetView.variable.createSeries()
      .setName("Auchan")
      .selectAllMonths()
      .setAmount("100")
      .selectNegativeAmounts()
      .validate();

    budgetView.variable.checkTotalAmounts(0, 0);
  }

  public void testDescriptionsAreUsedAsTooltips() throws Exception {
    budgetView.variable.createSeries()
      .setName("Groceries")
      .showDescription()
      .setDescription("Everything about food")
      .validate();
    budgetView.variable.checkSeriesTooltip("Groceries", "Everything about food");

    budgetView.variable.editSeries("Groceries")
      .showDescription()
      .checkDescription("Everything about food")
      .setDescription("Lunch")
      .validate();
    budgetView.variable.checkSeriesTooltip("Groceries", "Lunch");

    budgetView.variable.editSeries("Groceries")
      .showDescription()
      .setDescription("")
      .validate();
    budgetView.variable.checkSeriesTooltip("Groceries", "");
  }

  public void testSeriesCanBeReorderedByClickingOnColumnTitles() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 100.00, "Retraite 1")
      .addTransaction("2008/07/10", 200.00, "Retraite 2")
      .addTransaction("2008/07/05", 400.00, "Retraite 3")
      .load();

    categorization
      .selectTransaction("Retraite 1")
      .selectIncome().selectNewSeries("Retraite 1");

    categorization
      .selectTransaction("Retraite 2")
      .selectIncome().selectNewSeries("Retraite 2");
    categorization
      .selectTransaction("Retraite 3")
      .selectIncome().selectNewSeries("Retraite 3");

    budgetView.income.checkOrder("Retraite 3", "Retraite 2", "Retraite 1");

    budgetView.income.clickTitleSeriesName();
    budgetView.income.checkOrder("Retraite 1", "Retraite 2", "Retraite 3");

    budgetView.income.clickTitleSeriesName();
    budgetView.income.checkOrder("Retraite 3", "Retraite 2", "Retraite 1");

    budgetView.income.clickTitleSeriesName();
    budgetView.income.checkOrder("Retraite 3", "Retraite 2", "Retraite 1");

    budgetView.income.clickTitleRealAmount();
    budgetView.income.checkOrder("Retraite 3", "Retraite 2", "Retraite 1");

    budgetView.income.clickTitleRealAmount();
    budgetView.income.checkOrder("Retraite 1", "Retraite 2", "Retraite 3");

    budgetView.income.clickTitleSeriesName();
    budgetView.income.checkOrder("Retraite 1", "Retraite 2", "Retraite 3");

    budgetView.income.clickTitleRealAmount();
    budgetView.income.checkOrder("Retraite 1", "Retraite 2", "Retraite 3");

    budgetView.income.clickTitleSeriesName();
    budgetView.income.checkOrder("Retraite 1", "Retraite 2", "Retraite 3");
  }

  public void testNegativeAndPositiveOperations() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -10.00, "Auchan")
      .addTransaction("2008/07/01", -150.00, "Auchan")
      .addTransaction("2008/07/20", 50.00, "Auchan")
      .load();

    categorization
      .selectTransactions("Auchan")
      .selectVariable()
      .selectNewSeries("Courses", -250);
    timeline.selectMonth("2008/08");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("10/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -240.00, "Courses")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -10.00, "Courses")
      .check();
  }

  public void testShowHideInactiveSeries() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -50.00, "Auchan")
      .addTransaction("2008/07/01", -150.00, "Auchan")
      .addTransaction("2008/07/20", -50.00, "Auchan")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .addTransaction("2008/07/20", -50.00, "FNAC")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewVariable("FNAC", "Leisures", -100.00);

    budgetView.variable.checkSeriesList("Groceries", "Leisures")
      .checkSeries("Groceries", -50.00, -200.00)
      .checkSeries("Leisures", 0.00, -100.00);

    budgetView.variable.editSeries("Leisures").setEndDate(200807).validate();
    budgetView.variable.checkSeriesList("Groceries");

    budgetView.variable
      .checkAvailableActions("Add", "Disable month filtering")
      .showInactiveSeries();

    budgetView.variable.checkOrder("Groceries", "Leisures")
      .checkSeries("Groceries", -50.00, -200.00)
      .checkSeriesDisabled("Leisures");

    budgetView.variable.editSeries("Leisures")
      .checkAmountEditionDisabled("Envelope not active for period: august 2008")
      .clearEndDate()
      .checkAmountEditionEnabled()
      .setPropagationEnabled()
      .selectMonth(200808)
      .setAmount(100.00)
      .validate();

    timeline.selectMonth(200809);

    budgetView.variable.editSeries("Groceries")
      .checkAmountEditionEnabled()
      .setEndDate(200808)
      .checkAmountEditionEnabled()
      .validate();

    budgetView.variable.checkOrder("Leisures", "Groceries")
      .checkSeries("Leisures", 0.00, -100.00)
      .checkSeriesDisabled("Groceries");

    budgetView.variable.createSeries()
      .setName("Aaa")
      .setPropagationEnabled()
      .setAmount(10)
      .setStartDate(200812)
      .validate();
    budgetView.variable.createSeries()
      .setName("Ccc")
      .setPropagationEnabled()
      .setAmount(20)
      .setStartDate(200812)
      .validate();
    budgetView.variable.createSeries()
      .setName("Bbbb")
      .setPropagationEnabled()
      .setAmount(30)
      .setStartDate(200812)
      .validate();

    budgetView.variable.checkOrder("Leisures", "Groceries");
  }

  public void testOneMonth() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -30, "Free")
      .addTransaction("2008/07/08", -1500.00, "Loyer")
      .addTransaction("2008/07/09", -300, "Auchan")
      .addTransaction("2008/07/11", -100.00, "FNAC")
      .addTransaction("2008/07/12", 2200, "Salaire")
      .addTransaction("2008/07/13", -20, "cheque")
      .addTransaction("2008/07/13", -200, "Air France")
      .addTransaction("2008/07/15", -100.00, "VIRT ING")
      .load();
    timeline.checkDisplays("2008/07", "2008/08");

    views.selectCategorization();
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Loyer", "Rental");
    categorization.setNewVariable("Auchan", "Groceries", -300.00);
    categorization.setNewVariable("FNAC", "Equipment", -100.00);
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewExtra("Air France", "Trips");
    categorization.setNewTransfer("VIRT ING", "Epargne", "Account n. 00001123", "External account");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.transfer.alignAndPropagate("Epargne");

    double incomeFor200807 = 2200;
    double incomeFor200808 = 2200;
    double expensesFor200808 = 30 + 1500 + 300 + 100 + 100;
    double balanceFor200808 = incomeFor200808 - expensesFor200808;

    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);
    budgetView.income.checkTotalObserved(incomeFor200807);
    budgetView.recurring.checkTotalAmounts("1530.00", "1530.00");
    budgetView.transfer.checkTotalAmounts("100.00", "100.00");

    mainAccounts.changePosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000.00, "VIRT ING");
    timeline.checkMonthTooltip("2008/07", -880.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000 + balanceFor200808);

    budgetView.recurring.checkTotalAmounts("0.00", "1530.00");
    budgetView.variable.checkTotalAmounts("0.00", "400.00");

    timeline.selectAll();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000 + incomeFor200808 - expensesFor200808);
    budgetView.income.checkTotalAmounts("2200.00", "4400.00");

    timeline.selectMonth("2008/08");
    budgetView.extras.createSeries()
      .setName("Trip")
      .setTargetAccount("Account n. 00001123")
      .setAmount(170)
      .validate();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000.00);
  }

  public void testTwoMonths() throws Exception {

    addOns.activateAnalysis();
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "Free")
      .addTransaction("2008/07/08", -1500.00, "Loyer")
      .addTransaction("2008/07/09", -60.00, "Auchan")
      .addTransaction("2008/07/10", -20.00, "ED")
      .addTransaction("2008/07/11", -10.00, "FNAC")
      .addTransaction("2008/07/12", 1500.00, "Salaire")
      .addTransaction("2008/08/07", -29.90, "Free")
      .addTransaction("2008/08/08", -1500.00, "Loyer")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewRecurring("Free", "internet");
    categorization.setNewRecurring("Loyer", "rental");
    categorization.setNewVariable("Auchan", "groceries", -80.00);
    categorization.setVariable("ED", "groceries");
    categorization.setNewVariable("FNAC", "Equipment", -10.00);
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectBudget();

    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1529.90);

    timeline.checkMonthTooltip("2008/07", 29.90);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("Free", "internet")
      .setRecurring("Loyer", "rental");

    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410.00);

    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1500.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 1529.90)
      .checkValue("Variable", 90.00);

    views.selectHome();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    timeline.checkMonthTooltip("2008/08", 0.0);

    timeline.selectMonths("2008/07", "2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1410);
    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 3000.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 3059.80)
      .checkValue("Variable", 180.00);

    timeline.selectMonth("2008/09");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1420 + 1500 - 1529.90 - 80 - 10 - 10);
    analysis.budget().balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1500.00);
    analysis.budget().balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 1529.90)
      .checkValue("Variable", 90.00);
  }

  public void testBudgetSummaryDetailsShowsActualPositionInThePast() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.00, "2008/08/05")
      .addTransaction("2008/07/01", 1500.00, "WorldCo")
      .addTransaction("2008/07/05", -500.00, "Auchan")
      .addTransaction("2008/08/01", 1500.00, "WorldCo")
      .addTransaction("2008/08/05", -1000.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "Salary");

    timeline.selectMonth("2008/07");
    views.selectHome();
    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 500);
  }

  public void testWithPositiveEnvelope() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -200, "ED")
      .addTransaction("2008/07/09", 40.00, "remboursement")
      .addTransaction("2008/07/12", 1500.00, "Salaire")
      .addTransaction("2008/08/07", -100.00, "ED")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewRecurring("ED", "courses");
    categorization.setNewVariable("remboursement", "secu", 40.00);
    categorization.setNewIncome("Salaire", "Salaire");

    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 100.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1440.00);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1440.00);
    timeline.checkMonthTooltip("2008/08", -100.00);
  }
}
