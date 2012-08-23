package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

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
    budgetView.recurring.checkTotalAmounts(-84.0, -84.0);
    budgetView.recurring.checkSeries("Internet", -29.0, -29.0);
    budgetView.recurring.checkSeries("Electricity", -55.0, -55.0);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(-145.0, -145);
    budgetView.variable.checkSeries("Groceries", -145.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income
      .checkTotalAmounts(3740.0, 3540.00)
      .checkTotalGauge(3740.0, 3540.00)
      .checkTotalPositiveOverrun();

    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 200.0, 0.0);

    timeline.selectMonths("2008/08");

    transactions
      .showPlannedTransactions().initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -145.00, "Groceries")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Electricity", "", -55.00, "Electricity")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.00, "Internet")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Salary", "", 3540.00, "Salary")
      .check();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(0.0, -84.0);
    budgetView.recurring.checkSeries("Internet", -0.0, -29.0);
    budgetView.recurring.checkSeries("Electricity", -0.0, -55.0);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(0.0, -145);
    budgetView.variable.checkSeries("Groceries", -0.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(0.0, 3540.00);
    budgetView.income.checkSeries("Salary", 0.0, 3540.0);
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
    budgetView.extras.checkTotalAmounts(-95.0, -95.0);
    budgetView.extras.checkSeries("Anniversaire", -95.0, -95.0);

    views.selectCategorization();
    categorization.getExtras().checkSeriesIsSelected("Anniversaire");
  }

  public void testSavingsSeries() throws Exception {
    savingsAccounts.createNewAccount()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -25.00, "Virt Compte Epargne")
      .loadInNewAccount();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Virt Compte Epargne", "", -25.00)
      .check();

    views.selectCategorization();
    categorization.setNewSavings("Virt Compte Epargne", "Epargne", "Main accounts", "Livret");

    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    budgetView.savings.checkTitle("Savings");
    budgetView.savings.checkTotalAmounts(25.0, 25.0);
    budgetView.savings.checkSeries("Epargne", 25.0, 25.0);

    views.selectCategorization();
    categorization.getSavings().checkSeriesIsSelected("Epargne");
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
      .addTransaction("2008/07/31", "2008/08/02", -95.00, "Auchan")
      .addTransaction("2008/07/30", "2008/08/01", -50.00, "Monoprix")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/28", "2008/08/01", 3540.00, "WorldCo")
      .load();

    timeline.selectMonth("2008/07");

    views.selectData();
    transactions.initContent()
      .add("31/07/2008", "02/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("30/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("29/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("28/07/2008", "01/08/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries", -145.00);
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");
    timeline.selectMonth("2008/07");
    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(-29.0, -29.0);
    budgetView.recurring.checkSeries("Internet", -29.0, -29.0);

    budgetView.variable.checkTitle("Variable");
    budgetView.variable.checkTotalAmounts(-145.0, -145.);
    budgetView.variable.checkSeries("Groceries", -145.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3540.0, 3540.00);
    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);

    budgetView.getSummary()
      .checkEndPosition(-3366.00)
      .getChart()
      .checkRange(200807, 200808)
      .checkCurrentDay(200808, 2)
      .checkValue(200807, 1, -3366.00)
      .checkValue(200808, 1, 95.00)
      .checkValue(200808, 2, 0.00)
      .checkValue(200808, 27, 3366.00);
  }

  public void testEditingASeriesWithTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editSeries("Internet")
      .checkTitle("Editing a series")
      .checkName("Internet")
      .checkBudgetArea("Recurring")
      .setName("Free")
      .validate();

    budgetView.recurring.checkSeries("Free", -29.00, -29.00);
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
    budgetView.recurring.checkSeries("Internet", 0.0, -29.0);

    budgetView.variable.checkTotalAmounts(-50.00, -95.00);
    budgetView.variable.checkSeries("Groceries", -50.00, -95.00);

    budgetView.income.checkTotalAmounts(0.00, 3540.00);
    budgetView.income.checkSeries("Salary", 0.00, 3540.00);
    budgetView.getSummary().checkEndPosition(-3366.00);

    timeline.selectMonth("2008/07");
    budgetView.getSummary().checkEndPosition(0.00);
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

    budgetView.variable.checkSeries("courantMonoprix", -50, -200).checkSeries("courantED", -79, -200);

    budgetView.extras.checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/05");
    budgetView.variable.checkSeries("courantED", -29, -100)
      .checkSeriesNotPresent("courantMonoprix");

    budgetView.extras
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/06");
    budgetView.variable.checkSeries("courantMonoprix", -0, -100)
      .checkSeriesNotPresent("courantED");

    budgetView.extras.checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/07");

    budgetView.variable.checkSeries("courantMonoprix", -50, -100)
      .checkSeriesNotPresent("courantED");

    budgetView.extras
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/08");
    budgetView.variable
      .checkSeries("courantMonoprix", 0., 0.);
  }

  public void testEditingASeriesAmountHasNoImpactOnOtherSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Auchan")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Auchan")
      .load();

    timeline.selectMonth("2008/07");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Groceries")
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0, 0);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    budgetView.recurring.editSeries("Groceries")
      .selectMonth(200807)
      .setAmount("200")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0.00, -200.00);
    budgetView.recurring.checkSeries("Fuel", 0, 0);
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

  public void testCreateAndDeleteManySeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -29.00, "Auchan")
      .addTransaction("2008/06/29", -60.00, "ELF")
      .load();

    timeline.selectMonths("2008/06", "2008/07");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Groceries")
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0, 0);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    budgetView.recurring.editSeries("Groceries")
      .selectMonth(200807)
      .setAmount("200")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0.00, -200.00);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    views.selectCategorization();
    categorization.selectTransactions("ELF")
      .selectRecurring()
      .selectSeries("Fuel");

    categorization.selectTransactions("Auchan")
      .selectRecurring()
      .selectSeries("Groceries");

    views.selectBudget();
    budgetView.recurring.checkSeries("Fuel", -60, -120);

    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeries("Groceries");
    editionDialogChecker
      .deleteCurrentSeriesWithConfirmation();
    editionDialogChecker.validate();
    budgetView.recurring.checkSeriesNotPresent("Groceries");
  }

  public void testDeactivatingSeriesBudgets() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("salaire")
      .selectAllMonths()
      .setAmount("1000")
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("salaire", 0, 1000);
    views.selectData();
    timeline.selectLast();
    transactions
      .showPlannedTransactions().initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: salaire", "", 1000, "salaire")
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
      .setDescription("Everything about food")
      .validate();
    budgetView.variable.checkSeriesTooltip("Groceries", "Everything about food");

    budgetView.variable.editSeries("Groceries")
      .checkDescription("Everything about food")
      .setDescription("Lunch")
      .validate();
    budgetView.variable.checkSeriesTooltip("Groceries", "Lunch");

    budgetView.variable.editSeries("Groceries")
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
      .add("10/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -240.0, "Courses")
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

  public void testGaugeWidthsAreAdjustedDependingOnTheirRespectiveSizes() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/10", -50.00, "Auchan")
      .addTransaction("2008/07/01", -150.00, "Auchan")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .addTransaction("2008/07/20", -50.00, "FNAC")
      .addTransaction("2008/07/05", -25.00, "Zara")
      .addTransaction("2008/07/20", -25.00, "Zara")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewVariable("FNAC", "Leisures", -50.00);
    categorization.setNewVariable("Zara", "Clothes", -50.00);

    budgetView.variable.checkGaugeWidthRatio("Groceries", 1.0);
    budgetView.variable.checkGaugeWidthRatio("Leisures", 0.5);
    budgetView.variable.checkGaugeWidthRatio("Clothes", 0.25);
  }

  public void testDeltaGauge() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", 1000.00, "WorldCo")
      .addTransaction("2008/07/10", 1000.00, "WorldCo")
      .addTransaction("2008/08/10", 1200.00, "WorldCo")
      .addTransaction("2008/06/10", -55.00, "EDF")
      .addTransaction("2008/07/10", -55.00, "EDF")
      .addTransaction("2008/08/10", -55.00, "EDF")
      .addTransaction("2008/07/10", -50.00, "Auchan")
      .addTransaction("2008/08/01", -150.00, "Auchan")
      .addTransaction("2008/08/20", -50.00, "FNAC")
      .addTransaction("2008/06/05", -150.00, "Zara")
      .addTransaction("2008/07/05", -100.00, "Zara")
      .addTransaction("2008/08/10", -50.00, "Zara")
      .load();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewExtra("FNAC", "Leisures");
    categorization.setNewVariable("Zara", "Clothes", -50.00);

    timeline.selectMonth("2008/08");
    budgetView.income.checkDeltaGauge("Salary", 1000.0, 1200.0, 0.20,
                                      "The amount is increasing - it was 1000.00 in july 2008");
    budgetView.recurring.checkDeltaGauge("Electricity", -55.0, -55.0, 0.0,
                                         "The amount is the same as in july 2008");
    budgetView.variable.editPlannedAmount("Groceries").setPropagationDisabled().setAmount(250.00).validate();
    budgetView.variable.checkDeltaGauge("Groceries", -50.0, -250.0, 1.00,
                                        "The amount is increasing - it was 50.00 in july 2008");
    budgetView.extras.checkDeltaGauge("Leisures", null, -50.0, 1.0,
                                      "This envelope was not used in july 2008");
    budgetView.variable.checkDeltaGauge("Clothes", -100.0, -50.0, -0.5,
                                        "The amount is decreasing - it was 100.00 in july 2008");

    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    budgetView.recurring.checkDeltaGauge("Electricity", -55.0, -55.0, 0.0,
                                         "The amount is the same as in june 2008");
    budgetView.variable.checkDeltaGauge("Clothes", -150.0, -50.0, -0.67,
                                        "The amount is decreasing - it was 150.00 in june 2008");

    timeline.selectMonth("2008/09");
    budgetView.variable.editSeries("Clothes").setAmount(0.00).validate();
    budgetView.variable.checkDeltaGauge("Clothes", -50.0, 0.0, -1.00,
                                        "The amount was 50.00 in august 2008, and it is set to zero in september 2008");
    budgetView.income.editSeries("Salary").setAmount(0.00).validate();
    budgetView.income.checkDeltaGauge("Salary", 1200.0, 0.0, -1.00,
                                      "The amount was 1200.00 in august 2008, and it is set to zero in september 2008");
  }
}
