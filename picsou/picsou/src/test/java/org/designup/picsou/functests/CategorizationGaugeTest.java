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

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
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

    categorization.setNewEnvelope("WorldCo", "Income");
    gauge.checkLevel(0.5, "50%");
    gauge.checkProgressMessageHidden();

    categorization.setNewEnvelope("Auchan", "Food");
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    timeline.selectMonth("2008/06");
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    timeline.selectAll();
    gauge.checkLevel(0.05, "5%");
    gauge.checkQuasiCompleteProgressMessageShown();

    categorization.setNewEnvelope("FNAC", "Leisures");
    gauge.checkLevel(0.001, "1%");
    gauge.checkQuasiCompleteProgressMessageShown();

    categorization.setEnvelope("SAPN", "Leisures");
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

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
    gauge.checkLevel(1, "100%");
    gauge.checkProgressMessageHidden();

    categorization.setNewIncome("WorldCo", "Salaire");
    gauge.checkLevel(0.5, "50%");
    gauge.checkProgressMessageHidden();
  }

  public void testNothingIsShownIfThereAreNoTransactionsToCategorize() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    categorization.setEnvelope("WorldCo", "Income", MasterCategory.INCOME, true);

    categorization.getCompletionGauge().checkHidden();
    categorization.getCompletionGauge().checkCompleteProgressMessageShown();
  }

  public void testHidingProgressMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    categorization.setEnvelope("WorldCo", "Income", MasterCategory.INCOME, true);

    categorization.getCompletionGauge().checkCompleteProgressMessageShown();
    categorization.getCompletionGauge().hideProgressMessage();
    categorization.getCompletionGauge().checkProgressMessageHidden();
  }

  public void testMessageNavigation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .addTransaction("2008/05/10", 10.0, "McDo")
      .load();
    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");
    gauge.checkQuasiCompleteProgressMessageShown();

    gauge.clickOnProgressMessageLink();
    views.checkBudgetSelected();

    views.back();
    categorization.setEnvelope("McDo", "Food", MasterCategory.FOOD, true);
    gauge.checkCompleteProgressMessageShown();

    gauge.clickOnProgressMessageLink();
    views.checkBudgetSelected();
    
    views.back();
    views.checkCategorizationSelected();
  }
}
