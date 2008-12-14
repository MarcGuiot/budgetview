package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
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
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setIncome("WorldCo", "Salary", true);

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
      .checkTotalAmounts(3740.0, 3740.00)
      .checkTotalGauge(3740.0, 3540.00)
      .checkTotalPositiveOverrun();

    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 200.0, 0.0);

    budgetView.occasional.checkTitle("Occasional");
    budgetView.occasional.checkTotalAmount(0., 0.);
    budgetView.checkBalance((3540 - 145 - 84));

    timeline.selectMonths("2008/08");

    transactions.initContent()
      .add("12/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -145.00, "Groceries", getCategoryName(MasterCategory.FOOD))
      .add("05/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.00, "Internet", getCategoryName(MasterCategory.TELECOMS))
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Electricity", "", -55.00, "Electricity", getCategoryName(MasterCategory.HOUSE))
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Salary", "", 3540.00, "Salary", getCategoryName(MasterCategory.INCOME))
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

  public void testOccasionalShowMasterCategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();
    timeline.selectAll();
    views.selectCategorization();
    categorization.setOccasional("Auchan", MasterCategory.FOOD, "Apero");
    views.selectBudget();
    budgetView.occasional.check(MasterCategory.FOOD, -95.);
    budgetView.occasional.checkNotDisplayed("Apero");
    views.selectCategorization();
    categorization.setOccasional("Auchan", MasterCategory.HEALTH, "health.medecin");
    views.selectBudget();
    budgetView.occasional.check(MasterCategory.HEALTH, -95.);
    budgetView.occasional.checkNotDisplayed("health.medecin");
  }

  public void testOccasionalAreRemovedEvenInCaseOfRoundingErrors() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.4600000001, "Auchan")
      .addTransaction("2008/07/15", -100.17, "Carouf")
      .addTransaction("2008/07/18", -10.00, "Ed")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setOccasional("Auchan", MasterCategory.FOOD);
    categorization.setOccasional("Carouf", MasterCategory.FOOD);

    views.selectBudget();
    budgetView.occasional.check(MasterCategory.FOOD, -195.63);

    views.selectCategorization();
    categorization.getTable().selectRows(0, 1, 2);
    categorization.setUncategorized();

    views.selectBudget();
    budgetView.occasional.checkNotDisplayed(MasterCategory.FOOD);
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
    categorization.setSpecial("Auchan", "Anniversaire", MasterCategory.FOOD, true);

    views.selectBudget();

    budgetView.specials.checkTitle("Special");
    budgetView.specials.checkTotalAmounts(-95.0, -95.0);
    budgetView.specials.checkSeries("Anniversaire", -95.0, -95.0);

    views.selectCategorization();
    categorization.checkSpecialSeriesIsSelected("Anniversaire", MasterCategory.FOOD);
  }

  public void testSavingsSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -25.00, "Virt Compte Epargne")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Virt Compte Epargne", "", -25.00)
      .check();

    views.selectCategorization();
    categorization.setSavings("Virt Compte Epargne", "Epargne", MasterCategory.SAVINGS, true);

    views.selectBudget();

    budgetView.savings.checkTitle("Savings");
    budgetView.savings.checkTotalAmounts(-25.0, -25.0);
    budgetView.savings.checkSeries("Epargne", -25.0, -25.0);

    views.selectCategorization();
    categorization.checkSavingsSeriesIsSelected("Epargne", MasterCategory.SAVINGS);
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
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setIncome("WorldCo", "Salary", true);
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

    budgetView.occasional.checkTitle("Occasional");
    budgetView.occasional.checkTotalAmount(0, 0);
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
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

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
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setIncome("WorldCo", "Salary", true);
    categorization.selectEnvelopes().editSeries(false).
      selectSeries("Groceries").switchToManual().selectAllMonths().setAmount("95").validate();
    categorization.selectRecurring().editSeries(true).
      selectSeries("Internet").switchToManual().selectAllMonths().setAmount("29.0").validate();
    categorization.selectIncome().editSeries(true)
      .selectSeries("Salary").switchToManual().selectAllMonths().setAmount("3540.0").validate();

    timeline.selectMonth("2008/07");
    views.selectBudget();

    OfxBuilder.init(this)
      .addTransaction("2008/06/13", -50.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, false);

    views.selectBudget();
    timeline.checkSelection("2008/06");
    budgetView.recurring.checkTotalAmounts(0.0, -29.0);
    budgetView.recurring.checkSeries("Internet", 0.0, -29.0);

    budgetView.envelopes.checkTotalAmounts(-50.0, -95);
    budgetView.envelopes.checkSeries("Groceries", -50.0, -95.0);

    budgetView.income.checkTotalAmounts(0.0, 3540.00);
    budgetView.income.checkSeries("Salary", 0.0, 3540.0);

    budgetView.occasional.checkTotalAmount(0, 0);
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
      .setCategory(MasterCategory.FOOD)
      .selectAllMonths()
      .setAmount("100")
      .validate();
    budgetView.specials.createSeries().setName("courantAuchan")
      .setStartDate(200806)
      .setEndDate(200806)
      .selectAllMonths()
      .setAmount("100")
      .setCategory(MasterCategory.FOOD)
      .validate();
    budgetView.envelopes.createSeries().setName("courantMonoprix")
      .setStartDate(200806)
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .selectMonth(200808)
      .setAmount("0")
      .setCategory(MasterCategory.FOOD)
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Monoprix", "courantMonoprix", MasterCategory.FOOD, false);
    categorization.setSpecial("Auchan", "courantAuchan", MasterCategory.FOOD, false);
    categorization.setEnvelope("ED1", "courantED", MasterCategory.FOOD, false);
    categorization.setEnvelope("ED2", "courantED", MasterCategory.FOOD, false);

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
      .setCategory(MasterCategory.FOOD)
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .setCategory(MasterCategory.TRANSPORTS)
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
      .setCategory(MasterCategory.HEALTH)
      .validate();
    views.selectCategorization();
    timeline.selectAll();

    categorization.selectTableRows(0, 1)
      .selectEnvelopes()
      .selectEnvelopeSeries("santé", MasterCategory.HEALTH, false);

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("santé", 25, -30);

    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkSeries("santé", -30, -30);

    timeline.selectMonths("2008/06", "2008/07");
    budgetView.envelopes.checkSeries("santé", -5, -60);
  }

  public void testOccasional() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", -5000.00, "moto")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courant")
      .switchToManual()
      .selectAllMonths()
      .setAmount("2500")
      .setCategory(MasterCategory.HEALTH)
      .validate();

    budgetView.income.createSeries()
      .setName("Salaire")
      .switchToManual()
      .selectAllMonths().setAmount("3000")
      .setCategory(MasterCategory.INCOME)
      .validate();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .switchToManual()
      .selectAllMonths().setAmount("100")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.occasional.checkTotalAmount(0, 0);
    budgetView.checkBalance(400);

    budgetView.recurring.createSeries()
      .setName("Loyer").switchToManual()
      .setAmount("1000")
      .setCategory(MasterCategory.HOUSE)
      .validate();
    views.selectCategorization();
    categorization.setOccasional("moto", MasterCategory.LEISURES);
    views.selectBudget();
    double free = -600;
    budgetView.occasional.checkTotalAmount(5000, 5000);
    budgetView.checkBalance(free);
    views.selectCategorization();
    transactionDetails.split("2500", "divers");
    views.selectBudget();
    budgetView.occasional.checkTotalAmount(2500, 2500);
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
      .setCategory(MasterCategory.FOOD)
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .setCategory(MasterCategory.TRANSPORTS)
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
    categorization.selectTableRows("ELF")
      .selectRecurring()
      .categorizeInRecurringSeries("Fuel");

    categorization.selectTableRows("Auchan")
      .selectRecurring()
      .categorizeInRecurringSeries("Groceries");

    views.selectData();
    series.select("Groceries");

    views.selectBudget();
    budgetView.recurring.checkSeries("Fuel", -60, -120);

    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeriesList()
      .selectSeries("Groceries");
    editionDialogChecker
      .deleteSeriesWithConfirmation()
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
      .setCategory(MasterCategory.INCOME)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("salaire", 0, 1000);
    views.selectData();
    timeline.selectLast();
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: salaire", "", 1000, "salaire", MasterCategory.INCOME)
      .check();
    views.selectBudget();
    budgetView.income.editSeriesList().setName("salaire").setCustom().toggleMonth("Aug").validate();
    budgetView.income.checkSeriesNotPresent("salaire");
    views.selectData();
    timeline.selectLast();
    transactions.initContent().check();
  }

  public void testEditingAPlannedSeriesAmountByClickingOnTheAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();
    budgetView.recurring.clickOnPlannedAmount("Internet")
      .checkTitle("Recurring")
      .checkName("Internet")
      .setName("Free")
      .switchToManual()
      .setAmount("100")
      .validate();

    budgetView.recurring.checkSeries("Free", -29.00, -100.00);
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
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setOccasional("Free Telecom", MasterCategory.TELECOMS);
    categorization.setOccasional("EDF", MasterCategory.HOUSE);
    categorization.setOccasional("WorldCo - Bonus", MasterCategory.INCOME);
    categorization.setOccasional("WorldCo", MasterCategory.INCOME);

    views.selectData();
    series.checkExpanded("Envelopes", true);
    series.toggle("Envelopes");
    series.checkExpanded("Envelopes", false);

    views.selectBudget();
    budgetView.envelopes.gotoData("Groceries");

    views.checkDataSelected();
    views.selectData();
    series.checkSelection("Groceries");
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries", MasterCategory.FOOD)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries", MasterCategory.FOOD)
      .check();
    series.checkExpanded("Envelopes", true);

    views.selectBudget();
    budgetView.occasional.gotoData(MasterCategory.TELECOMS);
    views.checkDataSelected();
    series.checkSelection("Occasional");
    categories.checkSelection(MasterCategory.TELECOMS);
    transactions.initContent()
      .addOccasional("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00, MasterCategory.TELECOMS)
      .check();

    views.selectBudget();
    budgetView.envelopes.gotoData("Groceries");
    views.checkDataSelected();
    series.checkSelection("Groceries");
    categories.checkSelection(MasterCategory.ALL);
    series.checkExpanded("Envelopes", true);
  }

  public void testSeriesAreOrderedByDecreasingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Free Telecom", "tel", MasterCategory.TELECOMS, true);
    categorization.setEnvelope("Auchan", "Auchan", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Monop", MasterCategory.FOOD, true);

    views.selectBudget();
    budgetView.envelopes.checkOrder("Auchan", "Monop", "tel");
  }

  public void testOccasionalAreOrderedByDecreasingAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setOccasional("Free Telecom", MasterCategory.TELECOMS);
    categorization.setOccasional("Auchan", MasterCategory.FOOD);
    categorization.setOccasional("Monoprix", MasterCategory.CLOTHING);

    views.selectBudget();
    budgetView.occasional.checkOrder(MasterCategory.FOOD, MasterCategory.CLOTHING, MasterCategory.TELECOMS);
  }

  public void testHelpMessage() throws Exception {
    views.selectBudget();
    budgetView.checkHelpMessageDisplayed(false);

    budgetView.recurring.createSeries()
      .setName("Taxes")
      .setCategory(MasterCategory.TAXES)
      .switchToManual()
      .setAmount(100)
      .validate();
    budgetView.checkHelpMessageDisplayed(true);

    budgetView.hideHelpMessage();
    budgetView.checkHelpMessageDisplayed(false);
  }

  public void testPositifEnvelopeBudgetDoNotCreateNegativePlannedTransaction() throws Exception {
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
      .setCategory(MasterCategory.CLOTHING)
      .validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", 15.00, "Loto", MasterCategory.CLOTHING)
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00)
      .check();
    views.selectCategorization();
    categorization.setEnvelope("Loto", "Loto", MasterCategory.CLOTHING, false);
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto", MasterCategory.CLOTHING)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto", MasterCategory.CLOTHING)
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
      .setCategory(MasterCategory.CLOTHING)
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Loto", "Loto", MasterCategory.CLOTHING, false);
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto", MasterCategory.CLOTHING)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto", MasterCategory.CLOTHING)
      .check();
    views.selectBudget();
    budgetView.envelopes
      .editSeries("Loto")
      .selectAllMonths()
      .selectNegativeAmounts()
      .validate();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", -49.00, "Loto", MasterCategory.CLOTHING)
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "Loto", MasterCategory.CLOTHING)
      .add("05/07/2008", TransactionType.VIREMENT, "Loto", "", 19.00, "Loto", MasterCategory.CLOTHING)
      .check();

    views.selectCategorization();
    categorization.selectTableRows("Loto").setUncategorized();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: Loto", "", -15.00, "Loto", MasterCategory.CLOTHING)
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
      .setCategory(MasterCategory.CLOTHING)
      .validate();
    views.selectCategorization();
    categorization.setEnvelope("Loto", "ZeroSeries", MasterCategory.CLOTHING, false);
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00, "ZeroSeries", MasterCategory.CLOTHING)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00)
      .check();

    views.selectCategorization();
    categorization
      .selectTableRows("Loto")
      .setUncategorized();
    categorization.setEnvelope("Auchan", "ZeroSeries", MasterCategory.CLOTHING, false);
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries", MasterCategory.CLOTHING)
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("ZeroSeries").selectAllMonths().setAmount("10").validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries", MasterCategory.CLOTHING)
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("ZeroSeries").selectAllMonths().setAmount("29").validate();
    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PLANNED, "Planned: ZeroSeries", "", -10.00, "ZeroSeries", MasterCategory.CLOTHING)
      .add("12/07/2008", TransactionType.VIREMENT, "Loto", "", 15.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -19.00, "ZeroSeries", MasterCategory.CLOTHING)
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
      .setCategory(MasterCategory.GIFTS)
      .createSeries()
      .setName("Auchan")
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .selectNegativeAmounts()
      .setCategory(MasterCategory.GIFTS)
      .validate();
    views.selectBudget();
    budgetView.envelopes.checkTotalAmounts(0, 0);
    views.selectCategorization();

  }
}
