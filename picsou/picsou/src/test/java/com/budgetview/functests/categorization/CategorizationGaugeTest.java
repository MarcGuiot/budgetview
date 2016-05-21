package com.budgetview.functests.categorization;

import com.budgetview.functests.checkers.CategorizationGaugeChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class CategorizationGaugeTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    setInitialGuidesShown(true);
    resetWindow();
    super.setUp();
  }

  protected void selectInitialView() {
    views.selectCategorization();
  }

  public void test() throws Exception {

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
    gauge.checkHidden();

    views.selectData();
    
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/06/10", -900.0, "Auchan")
      .addTransaction("2008/07/10", -99.9, "FNAC")
      .addTransaction("2008/07/10", -0.1, "SAPN")
      .load();

    timeline.selectAll();
    views.selectCategorization();

    gauge.checkLevel(1);

    categorization.setNewIncome("WorldCo", "Income");
    categorization.selectTransaction("Auchan");
    checkNoSignpostVisible();

    gauge.checkLevel(0.5);
    checkNoSignpostVisible();

    categorization.setNewVariable("Auchan", "Food");
    gauge.checkLevel(0.05);
    categorization.checkQuasiCompleteProgressMessageShown();

    timeline.selectMonth("2008/06");
    gauge.checkLevel(0.05);
    categorization.checkQuasiCompleteProgressMessageShown();

    timeline.selectAll();
    gauge.checkLevel(0.05);
    categorization.checkQuasiCompleteProgressMessageShown();

    categorization.setNewVariable("FNAC", "Leisures");
    gauge.checkLevel(0.001);
    categorization.checkQuasiCompleteProgressMessageShown();

    categorization.setVariable("SAPN", "Leisures");
    gauge.checkHidden();
    categorization.checkGotoBudgetSignpostShown();
  }

  public void testIgnoresPlannedTransactions() throws Exception {
    views.selectData();
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", 1000.0, "WorldCo")
      .addTransaction("2008/07/10", -1000.0, "FNAC")
      .load();

    timeline.selectAll();

    CategorizationGaugeChecker gauge = categorization.getCompletionGauge();
    gauge.checkLevel(1);

    categorization.setNewIncome("WorldCo", "Salaire");
    gauge.checkLevel(0.5);
    categorization.selectTransaction("FNAC");
    checkNoSignpostVisible();    
  }

  public void testNothingIsShownIfThereAreNoTransactionsToCategorize() throws Exception {
    views.selectData();
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/10", 1000.0, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("WorldCo", "Income");

    categorization.getCompletionGauge().checkHidden();
    categorization.checkGotoBudgetSignpostShown();
  }
}
