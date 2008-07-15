package org.designup.picsou.functests;

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
      .load();

    CategorizerChecker checker = new CategorizerChecker(mainWindow);
    checker.setRecurring("free telecom", "internet");
    checker.setRecurring("Loyer", "rental");
    checker.setEnvelope("Auchan", "groceries", MasterCategory.FOOD);
    checker.setEnvelope("ED", "groceries", MasterCategory.FOOD);
    checker.setOccasional("fnac", MasterCategory.MULTIMEDIA);
    checker.setIncome("Salaire");

    views.selectHome();
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10), false)
      .checkRecurring(1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkIncome(1500);
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

    periods.selectCells("2008/07");
    CategorizerChecker checker = new CategorizerChecker(mainWindow);
    checker.setRecurring("free telecom", "internet");
    checker.setRecurring("Loyer", "rental");
    checker.setEnvelope("Auchan", "groceries", MasterCategory.FOOD);
    checker.setEnvelope("ED", "groceries", MasterCategory.FOOD);
    checker.setOccasional("fnac", MasterCategory.MULTIMEDIA);
    checker.setIncome("Salaire");

    periods.selectCells("2008/08");
    checker.setRecurring("free telecom", "internet");
    checker.setRecurring("Loyer", "rental");

    periods.selectCells("2008/07", "2008/08");
    views.selectHome();
    monthSummary.init()
      .total(1500, (29.9 + 1500 + 60 + 20 + 10 + 1500 + 29.90), false)
      .checkRecurring(1500 + 29.90 + 1500 + 29.90)
      .checkEnvelope(80)
      .checkOccasional(10)
      .checkIncome(1500);
  }
}
