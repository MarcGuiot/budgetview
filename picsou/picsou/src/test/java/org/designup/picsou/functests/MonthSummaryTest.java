package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class MonthSummaryTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentMonth("2008/08");
  }

  public void testNoData() throws Exception {
    views.selectHome();
    infochecker.checkNoNewVersion();
    monthSummary
      .checkNoBudgetAreasDisplayed()
      .checkNoDataMessage();
    monthSummary.openImportHelp().checkContains("import").close();
    mainAccounts.checkNoEstimatedPosition();
    monthSummary.balanceGraph.checkHidden();
    timeline.checkMonthTooltip("2008/08", "August 2008");

    String file = OfxBuilder.init(this)
      .addBankAccount(12345, 456456, "120901111", 125.00, "2008/08/25")
      .addTransaction("2008/08/26", 1000, "Company")
      .save();

    monthSummary
      .openImport()
      .selectFiles(file)
      .acceptFile()
      .doImport();

    timeline.checkSelection("2008/08");

    monthSummary
      .checkNoBudgetAreasDisplayed()
      .checkNoSeriesMessage();
    mainAccounts
      .checkEstimatedPosition(125.00);
    timeline.checkYearTooltip(2008, "2008");
  }

  public void testNoSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .addTransaction("2008/05/15", -100, "FNAC")
      .load();

    timeline.selectMonth("2008/06");

    views.selectHome();
    monthSummary
      .checkNoBudgetAreasDisplayed()
      .checkNoSeriesMessage()
      .openSeriesWizard()
      .validate();

    views.checkCategorizationSelected();
    categorization
      .checkTable(new Object[][]{
        {"15/06/2008", "", "Company", 1000.0},
        {"15/05/2008", "", "FNAC", -100.0},
      })
      .setIncome("Company", "Salary", true);

    views.selectHome();
    monthSummary
      .checkNoHelpMessageDisplayed()
      .checkIncomeOverrun(1000.0, 1000.0, 1000.0);
  }

  public void testOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "free telecom")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -60, "Auchan")
      .addTransaction("2008/07/10", -20, "ED")
      .addTransaction("2008/07/11", -10, "fnac")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/07/13", -23, "cheque")
      .addTransaction("2008/07/13", -200, "Air France")
      .load();

    views.selectCategorization();
    categorization.setRecurring("free telecom", "internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("Loyer", "rental", MasterCategory.HOUSE, true);
    categorization.setEnvelope("Auchan", "groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("ED", "groceries", MasterCategory.FOOD, false);
    categorization.setOccasional("fnac", MasterCategory.EQUIPMENT);
    categorization.setIncome("Salaire", "Salaire", true);
    categorization.setSpecial("Air France", "voyage", MasterCategory.LEISURES, true);

    double incomeFor200807 = 1500;
    double expensesFor200807 = 29.9 + 1500 + 60 + 20 + 10 + 23 + 200;
    double balance = incomeFor200807 - expensesFor200807;

    timeline.selectMonth("2008/07");
    views.selectHome();
    monthSummary
      .total(incomeFor200807, expensesFor200807)
      .checkBalance(balance)
      .checkBalanceGraph(0.81, 1)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkProjects(200)
      .checkUncategorized("-23.00");
    monthSummary.balanceGraph.checkTooltip(incomeFor200807, expensesFor200807);

    mainAccounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000, "Air France");
    timeline.checkMonthTooltip("2008/07", balance, 1000.00);

    timeline.selectAll();
    mainAccounts
      .checkEstimatedPosition(880.10);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(1000.00)
      .checkIncome(1500.00)
      .checkFixed(-1529.90)
      .checkEnvelope(-80)
      .checkSavings(0.00)
      .checkOccasional(-10.00)
      .checkProjects(0.00)
      .close();
  }

  public void testTwoMonths() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/07", -29.90, "free telecom")
      .addTransaction("2008/07/08", -1500, "Loyer")
      .addTransaction("2008/07/09", -60, "Auchan")
      .addTransaction("2008/07/10", -20, "ED")
      .addTransaction("2008/07/11", -10, "fnac")
      .addTransaction("2008/07/12", 1500, "Salaire")
      .addTransaction("2008/08/07", -29.90, "free telecom")
      .addTransaction("2008/08/08", -1500, "Loyer")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setRecurring("free telecom", "internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("Loyer", "rental", MasterCategory.HOUSE, true);
    categorization.setEnvelope("Auchan", "groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("ED", "groceries", MasterCategory.FOOD, false);
    categorization.setOccasional("fnac", MasterCategory.EQUIPMENT);
    categorization.setIncome("Salaire", "Salaire", true);

    views.selectHome();

    double balanceFor200807 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10))
      .checkBalance(balanceFor200807)
      .checkBalanceGraph(0.92, 1)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkNoUncategorized();

    mainAccounts
      .checkEstimatedPosition(1529.90)
      .checkEstimatedPositionDate("31/07/2008")
      .checkNoEstimatedPositionDetails();

    timeline.checkMonthTooltip("2008/07", balanceFor200807, 1529.90);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("free telecom", "internet", MasterCategory.TELECOMS, false)
      .setRecurring("Loyer", "rental", MasterCategory.HOUSE, false);

    views.selectHome();
    double balanceFor200808 = 1500 - (29.9 + 1500 + 60 + 20 + 10);
    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10))
      .checkBalance(balanceFor200808)
      .checkIncome(0)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(0)
      .checkOccasional(0);
    mainAccounts.checkEstimatedPosition(1410.00);
    mainAccounts.openEstimatedPositionDetails()
      .checkEnvelope(-80)
      .checkInitialPosition(0.0)
      .checkFixed(0)
      .checkEnvelope(-80)
      .checkOccasional(-10)
      .checkIncome(1500)
      .close();
    timeline.checkMonthTooltip("2008/08", balanceFor200808, 1410.00);

    timeline.selectMonths("2008/07", "2008/08");
    monthSummary
      .total(1500 + 1500, (29.9 + 1500 + 60 + 20 + 10) + (29.9 + 1500 + 60 + 20 + 10))
      .checkIncome(1500)
      .checkRecurring(1500 + 29.90 + 1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);
    mainAccounts
      .checkEstimatedPosition(1410)
      .checkEstimatedPositionDate("31/08/2008");

    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(0)
      .checkIncome(1500)
      .checkFixed(0)
      .checkEnvelope(-80)
      .checkOccasional(-10)
      .close();

    timeline.selectMonth("2008/09");
    mainAccounts
      .checkEstimatedPositionDate("30/09/2008")
      .checkEstimatedPosition(1420 + 1500 - 1529.90 - 80 - 10 - 10);
    mainAccounts.openEstimatedPositionDetails()
      .checkInitialPosition(1410)
      .checkIncome(1500)
      .checkFixed(-1529.90)
      .checkEnvelope(-80)
      .checkOccasional(-10)
      .close();
  }

  public void testSpecialWithAPositiveAmount() throws Exception {
    views.selectBudget();
    budgetView.specials.createSeries()
      .setName("Reimbursement")
      .setCategory(MasterCategory.GIFTS)
      .selectPositiveAmounts()
      .setAmount(2000)
      .validate();

    views.selectHome();
    monthSummary.checkProjects("0.00");
    monthSummary.checkProjectsPlanned("+2000.00");
  }

  public void testOccasionalIsTakenIntoAccountWhenComputingFuturePosition() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0001234", 500, "2008/08/15")
      .addTransaction("2008/07/05", 1000, "WorldCo")
      .addTransaction("2008/07/15", -100, "SAPN")
      .addTransaction("2008/08/05", 1000, "WorldCo")
      .addTransaction("2008/08/15", -25, "FNAC")
      .load();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary", true);
    categorization.setOccasional("SAPN", MasterCategory.EQUIPMENT);
    categorization.setOccasional("FNAC", MasterCategory.EQUIPMENT);

    views.selectHome();
    timeline.selectMonth("2008/07");
    monthSummary
      .total(1000, 100)
      .checkOccasional(100, 100);
    mainAccounts.checkEstimatedPosition((500 - 75) - (1000 - 100));
    mainAccounts.checkNoEstimatedPositionDetails();

    timeline.selectMonth("2008/08");
    monthSummary
      .total(1000, 100)
      .checkOccasional(25, 100);
    mainAccounts.checkEstimatedPosition(500 - 75);
    mainAccounts.openEstimatedPositionDetails()
      .checkOccasional(-75)
      .close();
  }

  public void testAdjustedValueShownAfterOverrun() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/05", 1000, "WorldCo")
      .addTransaction("2008/07/15", -100, "Auchan")
      .addTransaction("2008/08/05", 1200, "WorldCo")
      .addTransaction("2008/08/05", -80, "Auchan")
      .addTransaction("2008/08/15", -70, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary", true);
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);

    timeline.selectMonth("2008/08");
    views.selectHome();
    monthSummary
      .checkIncomeOverrun(1200, 1200, 200)
      .checkEnvelopeOverrun(150, 150, 50);
  }

  public void testUncategorized() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "MyCompany")
      .addTransaction("2008/08/26", -10, "FNAC")
      .addTransaction("2008/07/26", -10, "Another month")
      .load();

    timeline.selectMonth("2008/08");

    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Groceries").setCategory(MasterCategory.FOOD).validate();

    views.selectHome();
    monthSummary
      .checkBalance(990)
      .checkIncome(0, 0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(0)
      .checkUncategorized("1000.00 / -10.00");

    monthSummary.categorize();
    views.checkCategorizationSelected();
    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"26/08/2008", "", "FNAC", -10.0},
      {"26/08/2008", "", "MyCompany", 1000.0},
    });
    categorization.selectTableRow(0);
    categorization.selectOccasional();
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    views.selectHome();
    monthSummary
      .checkIncome(0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(10)
      .checkUncategorized("1000.00");

    monthSummary.categorize();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"26/08/2008", "Leisures", "FNAC", -10.0},
      {"26/08/2008", "", "MyCompany", 1000.0},
    });
    categorization.selectTableRow(1);
    categorization.selectIncome();
    categorization.selectIncomeSeries("Salary", true);

    views.selectHome();
    monthSummary
      .checkIncome(1000)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(10)
      .checkNoUncategorized();
  }

  public void testGauge() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/26", -10, "FNAC")
      .addTransaction("2008/08/26", -15, "Virgin")
      .load();

    views.selectCategorization();
    categorization
      .checkTable(new Object[][]{
        {"26/08/2008", "", "Company", 1000.0},
        {"26/08/2008", "", "FNAC", -10.0},
        {"26/08/2008", "", "Virgin", -15.0},
      });
    categorization.setIncome("Company", "Salary", true);
    categorization.setOccasional("FNAC", MasterCategory.LEISURES);

    views.selectHome();
    monthSummary
      .checkIncome(1000, 1000)
      .checkOccasional(10, 10);
  }

  public void testNavigatingToBudgetView() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/26", -10, "FNAC")
      .addTransaction("2008/08/26", -15, "Virgin")
      .load();

    views.selectCategorization();
    categorization.setIncome("Company", "Salary", true);

    views.selectHome();
    monthSummary.gotoBudget(BudgetArea.INCOME);
    views.checkBudgetSelected();
  }

  public void testNavigatingToTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/26", -10, "FNAC")
      .addTransaction("2008/08/26", -15, "Virgin")
      .load();

    views.selectCategorization();
    categorization.setIncome("Company", "Salary", true);

    views.selectData();
    categories.select(MasterCategory.FOOD);

    views.selectHome();
    monthSummary.gotoTransactions(BudgetArea.INCOME);
    views.checkDataSelected();
    series.checkSelection("Income");
    categories.checkSelection(MasterCategory.ALL);
    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salary", MasterCategory.INCOME)
      .check();
  }

  public void testEditingTheAccountBalanceLimit() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "FNAC")
      .load();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary", true);

    views.selectHome();
    timeline.selectMonth("2008/08");

    mainAccounts
      .checkEstimatedPosition(100)
      .checkEstimatedPositionColor("darkGreen")
      .checkLimit(0);

    mainAccounts
      .setLimit(1000, true)
      .checkLimit(1000)
      .checkEstimatedPositionColor("red");

    mainAccounts.setLimit(-2000, false)
      .checkLimit(-2000)
      .checkEstimatedPositionColor("green");

    mainAccounts.setLimit(0, false)
      .checkEstimatedPositionColor("darkGreen");

    timeline.selectMonth("2008/07");
    mainAccounts
      .checkEstimatedPosition(-100)
      .checkEstimatedPositionColor("darkRed");
  }

  public void testMonthTooltipWithNoPositionAvailable() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .load();

    operations.openPreferences().setFutureMonthsCount(1).validate();

    timeline.checkMonthTooltip("2008/07", 1000, 100);
    timeline.checkMonthTooltip("2008/08", "August 2008");
  }
}
