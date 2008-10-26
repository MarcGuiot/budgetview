package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.CategorizationGaugeChecker;
import org.designup.picsou.model.MasterCategory;

public class CategorizationGaugeTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    super.setUp();
  }

  protected void selectInitialView() {
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
    views.selectCategorization();

    gauge.checkLevel(1, "100%");
    gauge.checkProgressMessageHidden();

    categorization.setOccasional("WorldCo", MasterCategory.INCOME);
    gauge.checkLevel(0.5, "50%");
    gauge.checkProgressMessageHidden();

    categorization.setOccasional("Auchan", MasterCategory.FOOD);
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    timeline.selectMonth("2008/06");
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    timeline.selectAll();
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    categorization.setOccasional("FNAC", MasterCategory.INCOME);
    gauge.checkLevel(0.001, "1%");
    gauge.checkQuasiCompleteProgressMessageShown();

    categorization.setOccasional("SAPN", MasterCategory.TRANSPORTS);
    gauge.checkHidden();
    gauge.checkCompleteProgressMessageShown();
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
    gauge.checkProgressMessageHidden();

    categorization.setIncome("WorldCo", "Salaire", true);
    gauge.checkLevel(0.5, "50%");
    gauge.checkProgressMessageHidden();
  }

  public void testNothingIsShownIfThereAreNoTransactionsToCategorize() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    categorization.setOccasional("WorldCo", MasterCategory.INCOME);

    categorization.getGauge().checkHidden();
    categorization.getGauge().checkCompleteProgressMessageShown();
  }

  public void testHidingProgressMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    categorization.setOccasional("WorldCo", MasterCategory.INCOME);

    categorization.getGauge().checkCompleteProgressMessageShown();
    categorization.getGauge().hideProgressMessage();
    categorization.getGauge().checkProgressMessageHidden();
  }

  public void testMessageNavigation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .addTransaction("2008/05/10", 10.0, "McDo")
      .load();
    CategorizationGaugeChecker gauge = categorization.getGauge();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary", true);
    gauge.checkQuasiCompleteProgressMessageShown();

    gauge.clickOnProgressMessageLink();
    views.checkBudgetSelected();

    views.back();
    categorization.setOccasional("McDo", MasterCategory.FOOD);
    gauge.checkCompleteProgressMessageShown();

    gauge.clickOnProgressMessageLink();
    views.checkBudgetSelected();
    
    views.back();
    views.checkCategorizationSelected();
  }
}
