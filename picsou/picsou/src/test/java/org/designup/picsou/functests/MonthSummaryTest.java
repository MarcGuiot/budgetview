package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class MonthSummaryTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentMonth("2008/08");
  }

  public void testNoData() throws Exception {
    views.selectHome();
    monthSummary.init()
      .checkNoBudgetAreasDisplayed()
      .checkNoDataMessage("You must import your financial operations");

    String file = OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .save();

    monthSummary.init().openImport()
      .selectFiles(file)
      .startImport()
      .doImport();

    monthSummary.init()
      .checkNoBudgetAreasDisplayed()
      .checkNoSeriesMessage("You must categorize your operations");
  }

  public void testNoSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .load();

    views.selectHome();
    monthSummary.init()
      .checkNoBudgetAreasDisplayed()
      .checkNoSeriesMessage("You must categorize your operations")
      .categorizeAll();

    timeline.checkSelection("2008/06", "2008/07", "2008/08");

    categorization
      .checkTable(new Object[][]{
        {"15/06/2008", "", "Company", 1000.0},
      })
      .setIncome("Company", "Salary", true);

    views.selectHome();
    monthSummary.init()
      .checkNoHelpMessageDisplayed()
      .checkIncome(1000.0, 3000.0);
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
      .load();

    views.selectCategorization();
    categorization.setRecurring("free telecom", "internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("Loyer", "rental", MasterCategory.HOUSE, true);
    categorization.setEnvelope("Auchan", "groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("ED", "groceries", MasterCategory.FOOD, false);
    categorization.setOccasional("fnac", MasterCategory.EQUIPMENT);
    categorization.setIncome("Salaire", "Salaire", true);

    views.selectHome();
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 23), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkUncategorized("-23.00");

    accounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_ID, 1000, "cheque");
    timeline.selectAll();
    balanceSummary.initDetails()
      .balance(1000.)
      .income(1500.)
      .fixe(-1529.90)
      .saving(0.)
      .total(970.10);
  }

  public void testTwoMonths() throws Exception {
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

    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);

    timeline.selectMonth("2008/08");

    views.selectCategorization();
    categorization
      .setRecurring("free telecom", "internet", MasterCategory.TELECOMS, false)
      .setRecurring("Loyer", "rental", MasterCategory.HOUSE, false);

    views.selectHome();
    monthSummary.init()
      .total(0, (1500 + 29.90), false)
      .checkIncome(0)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(0)
      .checkOccasional(0);

    timeline.selectMonths("2008/07", "2008/08");
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 1500 + 29.90), false)
      .checkIncome(1500)
      .checkRecurring(1500 + 29.90 + 1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);
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
    monthSummary.init()
      .total(1000, 10, true)
      .checkIncome(0, 0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(0)
      .checkUncategorized("1000.00 / -10.00");

    monthSummary.init().categorize();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"26/08/2008", "", "FNAC", -10.0},
      {"26/08/2008", "", "MyCompany", 1000.0},
    });
    categorization.selectTableRow(0);
    categorization.selectOccasional();
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    views.selectHome();
    monthSummary.init()
      .total(1000, 10, true)
      .checkIncome(0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(10)
      .checkUncategorized("1000.00");

    monthSummary.init().categorize();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"26/08/2008", "Leisures", "FNAC", -10.0},
      {"26/08/2008", "", "MyCompany", 1000.0},
    });
    categorization.selectTableRow(1);
    categorization.selectIncome();
    categorization.selectIncomeSeries("Salary", true);

    views.selectHome();
    monthSummary.init()
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
    monthSummary.init()
      .checkIncome(1000, 1000)
      .checkOccasional(10, 1000);
  }
}
