package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.checkers.CategorizerChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class MonthSummaryTest extends LoggedInFunctionalTestCase {

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

    CategorizerChecker checker = new CategorizerChecker(mainWindow);
    checker.setRecurring("free telecom", "internet", true);
    checker.setRecurring("Loyer", "rental", true);
    checker.setEnvelope("Auchan", "groceries", MasterCategory.FOOD, true);
    checker.setEnvelope("ED", "groceries", MasterCategory.FOOD, false);
    checker.setOccasional("fnac", MasterCategory.MULTIMEDIA);
    checker.setIncome("Salaire");

    views.selectHome();
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 23), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkUncategorized("-23.00");
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
    views.selectData();
    CategorizerChecker.init(mainWindow)
      .setRecurring("free telecom", "internet", true)
      .setRecurring("Loyer", "rental", true)
      .setEnvelope("Auchan", "groceries", MasterCategory.FOOD, true)
      .setEnvelope("ED", "groceries", MasterCategory.FOOD, false)
      .setOccasional("fnac", MasterCategory.MULTIMEDIA)
      .setIncome("Salaire");
    views.selectHome();
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10), false)
      .checkIncome(1500, 1500)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10);

    timeline.selectMonth("2008/08");
    views.selectData();
    CategorizerChecker.init(mainWindow)
      .setRecurring("free telecom", "internet", false)
      .setRecurring("Loyer", "rental", false);
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

    views.selectHome();
    timeline.selectMonth("2008/08");

    monthSummary.init()
      .total(1000, 10, true)
      .checkIncome(0, 0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(0)
      .checkUncategorized("1000.00 / -10.00");

    CategorizationDialogChecker dialog = monthSummary.init().categorize();
    dialog.checkTable(new Object[][]{
      {"26/08/2008", "FNAC", -10.0},
      {"26/08/2008", "MyCompany", 1000.0},
    });
    dialog.selectTableRow(0);
    dialog.selectOccasional();
    dialog.selectOccasionalSeries(MasterCategory.LEISURES);
    dialog.validate();

    monthSummary.init()
      .total(1000, 10, true)
      .checkIncome(0)
      .checkRecurring(0)
      .checkEnvelope(0)
      .checkOccasional(10)
      .checkUncategorized("1000.00");

    CategorizationDialogChecker secondDialog = monthSummary.init().categorize();
    secondDialog.checkTable(new Object[][]{
      {"26/08/2008", "MyCompany", 1000.0},
    });
    secondDialog.selectTableRow(0);
    secondDialog.selectIncome();
    secondDialog.selectIncomeSeries("Salary", true);
    secondDialog.validate();

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
    views.selectHome();

    monthSummary.init()
      .checkIncome(0, 0)
      .checkOccasional(0, 0);

    monthSummary.init().categorize()
      .checkTable(new Object[][]{
        {"26/08/2008", "Company", 1000.0},
        {"26/08/2008", "FNAC", -10.0},
        {"26/08/2008", "Virgin", -15.0},
      })
      .selectTableRow(0)
      .selectIncome()
      .selectIncomeSeries("Salary", true)
      .selectTableRow(0)
      .selectOccasional()
      .selectOccasionalSeries(MasterCategory.LEISURES)
      .validate();

    monthSummary.init()
      .checkIncome(1000, 1000)
      .checkOccasional(10, 1000);
  }
}
