package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class MonthSummaryTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    LicenseChecker.enterLicense(mainWindow, "admin", "zz", 0);
    setCurrentMonth("2008/08");
  }

  public void testNoData() throws Exception {
    views.selectHome();
    infochecker.checkNoNewVersion();
    monthSummary
      .checkNoBudgetAreasDisplayed()
      .checkNoDataMessage();
    monthSummary.openHelp().checkContains("import").close();
    balanceSummary
      .checkNoTotal()
      .checkNothingShown();

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

    views.selectHome();
    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 23 + 200), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkProjects(200)
      .checkUncategorized("-23.00");

    accounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_ID, 1000, "Air France");

    timeline.selectAll();
    balanceSummary
      .checkBalance(1000.00)
      .checkIncome(1500.00)
      .checkFixed(-1529.90)
      .checkEnvelope(-80)
      .checkSavings(0.00)
      .checkProjects(0.00)
      .checkTotal(890.10);
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

    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);


    balanceSummary
      .checkMessage("End of month balance")
      .checkTotal(1529.90)
      .checkNothingShown();

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("free telecom", "internet", MasterCategory.TELECOMS, false)
      .setRecurring("Loyer", "rental", MasterCategory.HOUSE, false);

    views.selectHome();
    monthSummary
      .total(0, (1500 + 29.90), false)
      .checkIncome(0)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(0)
      .checkOccasional(0);

    balanceSummary.checkEnvelope(-80)
      .checkMessage("Estimated end of month balance")
      .checkBalance(0.)
      .checkFixed(0)
      .checkEnvelope(-80)
      .checkIncome(1500)
      .checkTotal(1420.);

    timeline.selectMonths("2008/07", "2008/08");
    monthSummary
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 1500 + 29.90), false)
      .checkIncome(1500)
      .checkRecurring(1500 + 29.90 + 1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);
    balanceSummary.checkEnvelope(-80)
      .checkBalance(0.)
      .checkFixed(0)
      .checkEnvelope(-80)
      .checkIncome(1500)
      .checkTotal(1420.);

    timeline.selectMonth("2008/09");
    balanceSummary.checkEnvelope(-80)
      .checkBalance(1410.)
      .checkFixed(-1529.90)
      .checkIncome(1500)
      .checkTotal(1420 + 1500 - 1529.90 - 80 - 10);
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
      .total(1000, 10, true)
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
      .total(1000, 10, true)
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
      .total(1000, 10, true)
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
}
