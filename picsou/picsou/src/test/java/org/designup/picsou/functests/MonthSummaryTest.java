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
    balanceSummary
      .checkNoTotal()
      .checkNothingShown();
    timeline.checkMonthTooltip("2008/08", "August 2008");

    String file = OfxBuilder.init(this)
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
    balanceSummary
      .checkBalance(00.00)
      .checkIncome(0.00)
      .checkFixed(0.00)
      .checkSavings(0.00)
      .checkProjects(0.00)
      .checkTotal(0.00);
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
      .checkIncome(1000.0, 0.0);
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

    double balance = 1500 - (29.9 + 1500 + 60 + 20 + 10 + 23 + 200);

    timeline.selectMonth("2008/07");
    views.selectHome();
    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 23 + 200))
      .checkBalance(balance)
      .checkBalanceGraph(0.81, 1)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkProjects(200)
      .checkUncategorized("-23.00");

    accounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_NAME, 1000, "Air France");
    timeline.checkMonthTooltip("2008/07", balance, 1000.00);    

    timeline.selectAll();
    balanceSummary
      .checkBalance(1000.00)
      .checkIncome(1500.00)
      .checkFixed(-1529.90)
      .checkEnvelope(-80)
      .checkSavings(0.00)
      .checkOccasional(-10.00)
      .checkProjects(0.00)
      .checkTotal(880.10);

  }

  public void testTwoMonths() throws Exception {
    operations.getPreferences().changeFutureMonth(12).validate();
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

    balanceSummary
      .checkTotalLabel(200807)
      .checkTotal(1529.90)
      .checkNothingShown();

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
    balanceSummary.checkEnvelope(-80)
      .checkBalance(0.0)
      .checkFixed(0)
      .checkEnvelope(-80)
      .checkOccasional(-10)
      .checkIncome(1500)
      .checkTotal(1410.00);
    timeline.checkMonthTooltip("2008/08", balanceFor200808, 1410.00);

    timeline.selectMonths("2008/07", "2008/08");
    monthSummary
      .total(1500 + 1500, (29.9 + 1500 + 60 + 20 + 10) + (29.9 + 1500 + 60 + 20 + 10))
      .checkIncome(1500)
      .checkRecurring(1500 + 29.90 + 1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);
    balanceSummary
      .checkFutureTotalLabel(200808)
      .checkEnvelope(-80)
      .checkBalance(0.)
      .checkFixed(0)
      .checkOccasional(-10)
      .checkEnvelope(-80)
      .checkIncome(1500)
      .checkTotal(1410.);

    timeline.selectMonth("2008/09");
    balanceSummary
      .checkFutureTotalLabel(200809)
      .checkEnvelope(-80)
      .checkBalance(1410.)
      .checkFixed(-1529.90)
      .checkIncome(1500)
      .checkOccasional(-10)
      .checkTotal(1420 + 1500 - 1529.90 - 80 - 10 - 10);
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
    balanceSummary
      .checkOccasional(0)
      .checkTotal((500 - 75) - (1000 - 100));

    timeline.selectMonth("2008/08");
    monthSummary
      .total(1000, 100)
      .checkOccasional(25, 100);
    balanceSummary
      .checkOccasional(-75)
      .checkTotal(500 - 75);
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

    balanceSummary.checkTotal(100)
      .checkTotalColor("darkGreen")
      .checkLimit(0);

    balanceSummary
      .setLimit(1000, true)
      .checkLimit(1000)
      .checkTotalColor("red");

    balanceSummary.setLimit(-2000, false)
      .checkLimit(-2000)
      .checkTotalColor("green");

    balanceSummary.setLimit(0, false)
      .checkTotalColor("darkGreen");

    timeline.selectMonth("2008/07");
    balanceSummary
      .checkTotal(-100)
      .checkTotalColor("darkRed");
  }

  public void testMonthTooltipWithNoPositionAvailable() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .load();

    operations.getPreferences().changeFutureMonth(1).validate();

    timeline.checkMonthTooltip("2008/07", 1000, 100);
    timeline.checkMonthTooltip("2008/08", "August 2008");
  }
}
