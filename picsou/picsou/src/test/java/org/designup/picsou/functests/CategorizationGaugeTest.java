package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.CategorizationGaugeChecker;
import org.designup.picsou.model.MasterCategory;

public class CategorizationGaugeTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    super.setUp();
    views.selectCategorization();
  }

  public void test() throws Exception {

    CategorizationGaugeChecker gauge = categorization.getGauge();
    gauge.checkHidden();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/06/10", -900.0, "Auchan")
      .addTransaction("2008/07/10", -99.9, "FNAC")
      .addTransaction("2008/07/10", -0.1, "SAPN")
      .load();

    timeline.selectAll();

    gauge.checkLevel(1, "100%");

    categorization.setOccasional("WorldCo", MasterCategory.INCOME);
    gauge.checkLevel(0.5, "50%");

    categorization.setOccasional("Auchan", MasterCategory.FOOD);
    gauge.checkLevel(0.05, "5%");

    timeline.selectMonth("2008/06");
    gauge.checkHidden();

    timeline.selectMonth("2008/07");
    gauge.checkLevel(1, "100%");

    timeline.selectAll();
    gauge.checkLevel(0.05, "5%");

    categorization.setOccasional("FNAC", MasterCategory.INCOME);
    gauge.checkLevel(0.001, "1%");

    categorization.setOccasional("SAPN", MasterCategory.TRANSPORTS);
    gauge.checkHidden();

    timeline.selectMonth("2008/07");
    gauge.checkHidden();
  }

  public void testIgnoresPlannedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/07/10", -1000.0, "FNAC")
      .load();

    timeline.selectAll();

    CategorizationGaugeChecker gauge = categorization.getGauge();
    gauge.checkLevel(1, "100%");

    categorization.setIncome("WorldCo", "Salaire", true);
    gauge.checkLevel(0.5, "50%");
  }

  public void testNothingIsShownIfThereAreNoTransactionsToCategorize() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    timeline.selectMonth("2008/06");
    categorization.checkTableIsEmpty();

    categorization.getGauge().checkHidden();
  }
}
