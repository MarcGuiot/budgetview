package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class BudgetViewTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    super.setUp();
  }


  public void testUnset() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");

    views.selectBudget();
    budgetView.variable.checkPlannedUset("Groceries");

    timeline.selectAll();

    budgetView.variable.checkPlannedUset("Groceries");
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

    budgetView.getSummary().checkMonthBalance(3496.00);

    timeline.selectMonths("2008/08");

    transactions.initContent()
      .add("12/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -145.00, "Groceries")
      .add("05/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.00, "Internet")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Electricity", "", -55.00, "Electricity")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Salary", "", 3540.00, "Salary")
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
      .setAccountName("Livret")
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
    categorization.setNewVariable("Auchan", "Groceries", -145.);
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

    budgetView.getSummary().checkMonthBalance(3540.00 - 145.00 - 29.00);
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

    categorization.selectVariable().editSeries()
      .selectSeries("Groceries")
      .selectAllMonths()
      .setAmount("95")
      .validate();
    categorization.selectRecurring().editSeries()
      .selectSeries("Internet")
      .selectAllMonths()
      .setAmount("29.0")
      .validate();
    categorization.selectIncome().editSeries()
      .selectSeries("Salary")
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
    budgetView.getSummary().checkMonthBalance(-50.00);

    timeline.selectMonth("2008/07");
    budgetView.getSummary().checkMonthBalance((double)(3540 - 95 - 29 - 50));
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

    budgetView.recurring.editSeriesList().selectSeries("Groceries")
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

    budgetView.recurring.editSeriesList().selectSeries("Groceries")
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

    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeriesList()
      .selectSeries("Groceries");
    editionDialogChecker
      .deleteSelectedSeriesWithConfirmation()
      .validate();
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
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: salaire", "", 1000, "salaire")
      .check();

    views.selectBudget();
    budgetView.income.editSeriesList().setName("salaire").setCustom().toggleMonth("Aug").validate();
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

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setVariable("Monoprix", "Groceries");

    views.selectBudget();
    budgetView.variable.gotoData("Groceries");

    views.checkDataSelected();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();
    transactions.clearFilters();

    views.selectBudget();
    budgetView.variable.gotoData("Groceries");
    views.checkDataSelected();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();
  }

  public void testSeriesAreOrderedByDecreasingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Free Telecom", "tel");
    categorization.setNewVariable("Auchan", "Auchan");
    categorization.setNewVariable("Monoprix", "Monop");

    views.selectBudget();
    budgetView.variable.checkOrder("Auchan", "Monop", "tel");
  }

  public void testPositiveEnvelopeBudgetDoNotCreateNegativePlannedTransaction() throws Exception {
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
    transactions.initContent()
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

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Loto")
      .selectAllMonths()
      .setAmount("15")
      .selectPositiveAmounts()
      .validate();

    views.selectCategorization();
    categorization.setVariable("Loto", "Loto");

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();

    views.selectBudget();
    budgetView.variable
      .editSeries("Loto")
      .selectAllMonths()
      .selectNegativeAmounts()
      .validate();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", -49.00, "Loto")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();

    views.selectCategorization();
    categorization.selectTransactions("Loto").setUncategorized();
    views.selectData();
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

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("ZeroSeries")
      .selectAllMonths()
      .setAmount("0")
      .selectPositiveAmounts()
      .validate();

    views.selectCategorization();
    categorization.setVariable("Loto", "ZeroSeries");

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "ZeroSeries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00)
      .check();

    views.selectCategorization();
    categorization
      .selectTransactions("Loto")
      .setUncategorized();
    categorization.setVariable("Auchan", "ZeroSeries");

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    views.selectBudget();
    budgetView.variable.editSeries("ZeroSeries").selectAllMonths().setAmount("10").validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    views.selectBudget();
    budgetView.variable.editSeries("ZeroSeries").selectAllMonths().setAmount("29").validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: ZeroSeries", "", -10.00, "ZeroSeries")
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();
  }

  public void testMixPositifAndNegativeBudgetInTotalBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", -19.00, "Auchan")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Loto")
      .selectAllMonths()
      .setAmount("100")
      .selectPositiveAmounts()
      .createSeries()
      .setName("Auchan")
      .selectAllMonths()
      .setAmount("100")
      .selectNegativeAmounts()
      .validate();

    views.selectBudget();
    budgetView.variable.checkTotalAmounts(0, 0);
    views.selectCategorization();
  }

  public void testDescriptionsAreUsedAsTooltips() throws Exception {
    views.selectBudget();
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

    views.selectCategorization();
    categorization
      .selectTransaction("Retraite 1")
      .selectIncome().selectNewSeries("Retraite 1");

    categorization
      .selectTransaction("Retraite 2")
      .selectIncome().selectNewSeries("Retraite 2");
    categorization
      .selectTransaction("Retraite 3")
      .selectIncome().selectNewSeries("Retraite 3");

    views.selectBudget();
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
}
