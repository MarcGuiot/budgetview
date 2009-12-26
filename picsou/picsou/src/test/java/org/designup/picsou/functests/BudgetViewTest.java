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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(-84.0, -84.0);
    budgetView.recurring.checkSeries("Internet", -29.0, -29.0);
    budgetView.recurring.checkSeries("Electricity", -55.0, -55.0);

    budgetView.envelopes.checkTitle("Envelopes");
    budgetView.envelopes.checkTotalAmounts(-145.0, -145);
    budgetView.envelopes.checkSeries("Groceries", -145.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income
      .checkTotalAmounts(3740.0, 3540.00)
      .checkTotalGauge(3740.0, 3540.00)
      .checkTotalPositiveOverrun();

    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 200.0, 0.0);

    budgetView.checkBalance((3540 - 145 - 84));

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

    budgetView.envelopes.checkTitle("Envelopes");
    budgetView.envelopes.checkTotalAmounts(0.0, -145);
    budgetView.envelopes.checkSeries("Groceries", -0.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(0.0, 3540.00);
    budgetView.income.checkSeries("Salary", 0.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 0, 0);
  }

  public void testSpecialSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .check();

    timeline.checkSelection("2008/07");
    views.selectCategorization();
    categorization.setNewSpecial("Auchan", "Anniversaire");

    views.selectBudget();

    budgetView.specials.checkTitle("Special");
    budgetView.specials.checkTotalAmounts(-95.0, -95.0);
    budgetView.specials.checkSeries("Anniversaire", -95.0, -95.0);

    views.selectCategorization();
    categorization.getSpecial().checkSeriesIsSelected("Anniversaire");
  }

  public void testSavingsSeries() throws Exception {
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -25.00, "Virt Compte Epargne")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Virt Compte Epargne", "", -25.00)
      .check();


    views.selectCategorization();
    categorization.setNewSavings("Virt Compte Epargne", "Epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Livret");

    views.selectBudget();

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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");
    timeline.selectMonth("2008/07");
    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring");
    budgetView.recurring.checkTotalAmounts(-29.0, -29.0);
    budgetView.recurring.checkSeries("Internet", -29.0, -29.0);

    budgetView.envelopes.checkTitle("Envelopes");
    budgetView.envelopes.checkTotalAmounts(-145.0, -145.);
    budgetView.envelopes.checkSeries("Groceries", -145.0, -145.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3540.0, 3540.00);
    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);

    budgetView.checkBalance((3540 - 145 - 29));
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
      .checkTitle("Recurring")
      .checkName("Internet")
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");

    categorization.selectEnvelopes().editSeries()
      .selectSeries("Groceries")
      .switchToManual()
      .selectAllMonths()
      .setAmount("95")
      .validate();
    categorization.selectRecurring().editSeries()
      .selectSeries("Internet")
      .switchToManual()
      .selectAllMonths()
      .setAmount("29.0")
      .validate();
    categorization.selectIncome().editSeries()
      .selectSeries("Salary")
      .switchToManual()
      .selectAllMonths()
      .setAmount("3540.0")
      .validate();

    timeline.selectMonth("2008/07");
    views.selectBudget();

    OfxBuilder.init(this)
      .addTransaction("2008/06/13", -50.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries");

    views.selectBudget();
    timeline.checkSelection("2008/06");
    budgetView.recurring.checkTotalAmounts(0.00, -29.00);
    budgetView.recurring.checkSeries("Internet", 0.0, -29.0);

    budgetView.envelopes.checkTotalAmounts(-50.00, -95.00);
    budgetView.envelopes.checkSeries("Groceries", -50.00, -95.00);

    budgetView.income.checkTotalAmounts(0.00, 3540.00);
    budgetView.income.checkSeries("Salary", 0.00, 3540.00);

    budgetView.checkBalance(3540 - 95 - 29);
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
    budgetView.envelopes.createSeries().setName("courantED")
      .setEndDate(200805)
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.specials.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.envelopes.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .selectMonth(200808)
      .setAmount("0")
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Monoprix", "courantMonoprix");
    categorization.setSpecial("Auchan", "courantAuchan");
    categorization.setEnvelope("ED1", "courantED");
    categorization.setEnvelope("ED2", "courantED");

    views.selectBudget();
    timeline.selectMonths("2008/04", "2008/05", "2008/06", "2008/07");

    budgetView.envelopes.checkSeries("courantMonoprix", -50, -200).checkSeries("courantED", -79, -200);

    budgetView.specials.checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/05");
    budgetView.envelopes.checkSeries("courantED", -29, -100)
      .checkSeriesNotPresent("courantMonoprix");

    budgetView.specials
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("courantMonoprix", -0, -100)
      .checkSeriesNotPresent("courantED");

    budgetView.specials.checkSeries("courantAuchan", -95, -100);

    timeline.selectMonth("2008/07");

    budgetView.envelopes.checkSeries("courantMonoprix", -50, -100)
      .checkSeriesNotPresent("courantED");

    budgetView.specials
      .checkSeriesNotPresent("courantAuchan");

    timeline.selectMonth("2008/08");
    budgetView.envelopes
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
      .switchToManual()
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
    budgetView.envelopes.createSeries()
      .setName("santé")
      .validate();
    views.selectCategorization();
    timeline.selectAll();

    categorization.selectTableRows(0, 1)
      .selectEnvelopes()
      .selectSeries("santé");

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("santé", 25, -30);

    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("santé", -30, -30);

    timeline.selectMonths("2008/06", "2008/07");
    budgetView.envelopes.checkSeries("santé", -5, -60);
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
      .switchToManual()
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

    views.selectData();
    series.select("Groceries");

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
      .switchToManual()
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

  public void testEditingAPlannedSeriesAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    // First update with propagation + switching to manual mode
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkNegativeAmountsSelected()
      .checkAmount("29.00")
      .checkAmountIsSelected()
      .setAmount("100")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.editSeries("Internet")
      .checkManualModeSelected()
      .cancel();

    // Propagation disabled
    timeline.selectMonth("2008/07");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount("100.00")
      .checkAmountIsSelected()
      .checkPropagationDisabled()
      .setAmountAndValidate("150");
    budgetView.recurring.checkSeries("Internet", -29.00, -150.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Multi-selection without propagation
    timeline.selectMonths("2008/07", "2008/09");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountIsEmpty()
      .checkPropagationDisabled()
      .setAmountAndValidate("200");
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -200.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Internet", 0.00, -200.00);
    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Multi-selection with propagation
    timeline.selectMonths("2008/07", "2008/09");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount("200.00")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .setAmountAndValidate("300");
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -300.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
  }

  public void testAmountEditionDialogPeriodicityLabels() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every month")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .setEndDate(200810)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months until october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .setStartDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months from july to october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setEveryMonth()
      .setEndDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("July 2008 only")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setIrregular()
      .removeEndDate()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Irregular from july 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setCustom()
      .setStartDate(200801)
      .setEndDate(200812)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPeriodicity("Custom from january to december 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setCustom()
      .setStartDate(200801)
      .setEndDate(200912)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPeriodicity("Custom from 2008 to 2009")
      .validate();
  }

  public void testEditingPlannedSeriesAmountsWithCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkAmount("29.00")
      .checkAmountIsSelected()
      .setAmount("100")
      .checkPropagationDisabled()
      .cancel();
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.editSeries("Internet")
      .checkAutomaticModeSelected()
      .cancel();

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount("29.00")
      .setAmount("100")
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount("100.00")
      .setAmount("200")
      .cancel();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
  }

  public void testAligningThePlannedSeriesAmountOnTheActualAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", +1500.00, "WorldCo")
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("Free Telecom", "Internet");

    // First update with propagation + switching to manual mode
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .setAmount("100")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkNegativeAmountsSelected()
      .checkAmount("100.00")
      .checkActualAmount("29.00")
      .alignPlannedAndActual()
      .checkNegativeAmountsSelected()
      .checkAmount("29.00")
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -29.00);

    timeline.selectMonths("2008/07", "2008/08");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount("29.00")
      .checkActualAmount("Actual")
      .alignPlannedAndActual()
      .setPropagationEnabled()
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, 0.00);

    // Positive amount
    timeline.selectMonth("2008/07");
    budgetView.income.editPlannedAmount("Salary")
      .checkPositiveAmountsSelected()
      .checkAmount("1500.00")
      .checkActualAmount("1500.00")
      .setAmount(1000.00)
      .alignPlannedAndActual()
      .checkPositiveAmountsSelected()
      .checkAmount("1500.00")
      .setPropagationEnabled()
      .validate();
    budgetView.income.checkSeries("Salary", 1500.00, 1500.00);
    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("Salary", 0.00, 1500.00);
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");

    views.selectData();
    series.checkExpanded("Envelopes", true);
    series.toggleExpansion("Envelopes");
    series.checkExpanded("Envelopes", false);

    views.selectBudget();
    budgetView.envelopes.gotoData("Groceries");

    views.checkDataSelected();
    views.selectData();
    series.checkSelection("Groceries");
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();
    series.checkExpanded("Envelopes", true);

    views.selectBudget();
    budgetView.envelopes.gotoData("Groceries");
    views.checkDataSelected();
    series.checkSelection("Groceries");
    series.checkExpanded("Envelopes", true);
  }

  public void testSeriesAreOrderedByDecreasingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Free Telecom", "tel");
    categorization.setNewEnvelope("Auchan", "Auchan");
    categorization.setNewEnvelope("Monoprix", "Monop");

    views.selectBudget();
    budgetView.envelopes.checkOrder("Auchan", "Monop", "tel");
  }

  public void testHelpMessage() throws Exception {
    views.selectBudget();
    budgetView.checkHelpMessageDisplayed(false);

    budgetView.recurring.createSeries()
      .setName("Taxes")
      .switchToManual()
      .setAmount(100)
      .validate();
    budgetView.checkHelpMessageDisplayed(true);

    budgetView.hideHelpMessage();
    budgetView.checkHelpMessageDisplayed(false);
  }

  public void testPositiveEnvelopeBudgetDoNotCreateNegativePlannedTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "Loto")
      .addTransaction("2008/07/05", 19.00, "Loto")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Loto")
      .switchToManual()
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
    categorization.setEnvelope("Loto", "Loto");
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
    budgetView.envelopes.createSeries()
      .setName("Loto")
      .switchToManual()
      .selectAllMonths()
      .setAmount("15")
      .selectPositiveAmounts()
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Loto", "Loto");
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto")
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto")
      .check();
    views.selectBudget();
    budgetView.envelopes
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
    budgetView.envelopes.createSeries()
      .setName("ZeroSeries")
      .switchToManual()
      .selectAllMonths()
      .setAmount("0")
      .selectPositiveAmounts()
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Loto", "ZeroSeries");
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "ZeroSeries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00)
      .check();

    views.selectCategorization();
    categorization
      .selectTransactions("Loto")
      .setUncategorized();
    categorization.setEnvelope("Auchan", "ZeroSeries");
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("ZeroSeries").selectAllMonths().setAmount("10").validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries")
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("ZeroSeries").selectAllMonths().setAmount("29").validate();
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
    budgetView.envelopes.createSeries()
      .setName("Loto")
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .selectPositiveAmounts()
      .createSeries()
      .setName("Auchan")
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .selectNegativeAmounts()
      .validate();
    views.selectBudget();
    budgetView.envelopes.checkTotalAmounts(0, 0);
    views.selectCategorization();
  }

  public void testDescriptionsAreUsedAsTooltips() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Groceries")
      .setDescription("Everything about food")
      .validate();
    budgetView.envelopes.checkSeriesTooltip("Groceries", "Everything about food");

    budgetView.envelopes.editSeries("Groceries")
      .checkDescription("Everything about food")
      .setDescription("Lunch")
      .validate();
    budgetView.envelopes.checkSeriesTooltip("Groceries", "Lunch");

    budgetView.envelopes.editSeries("Groceries")
      .setDescription("")
      .validate();
    budgetView.envelopes.checkSeriesTooltip("Groceries", "");
  }
}
