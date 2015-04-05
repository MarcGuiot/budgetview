package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetViewDeltaGaugeTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    super.setUp();
  }

  public void testDeltaGauge() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", 1000.00, "WorldCo")
      .addTransaction("2008/07/10", 1000.00, "WorldCo")
      .addTransaction("2008/08/10", 1200.00, "WorldCo")
      .addTransaction("2008/06/10", -55.00, "EDF")
      .addTransaction("2008/07/10", -55.00, "EDF")
      .addTransaction("2008/08/10", -55.00, "EDF")
      .addTransaction("2008/07/10", -50.00, "Auchan")
      .addTransaction("2008/08/01", -150.00, "Auchan")
      .addTransaction("2008/08/20", -50.00, "FNAC")
      .addTransaction("2008/06/05", -150.00, "Zara")
      .addTransaction("2008/07/05", -100.00, "Zara")
      .addTransaction("2008/08/10", -50.00, "Zara")
      .load();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewExtra("FNAC", "Leisures");
    categorization.setNewVariable("Zara", "Clothes", -50.00);
    budgetView.extras.editSeries("Leisures").setStartDate(200808).validate();

    timeline.selectMonth("2008/08");
    budgetView.income.checkDeltaGauge("Salary", 1000.00, 1200.00, 0.20,
                                      "The amount is increasing - it was 1000.00 in july 2008");
    budgetView.recurring.checkDeltaGauge("Electricity", -55.00, -55.00, 0.00,
                                         "The amount is the same as in july 2008");
    budgetView.variable.editPlannedAmount("Groceries").setPropagationDisabled().setAmount(250.00).validate();
    budgetView.variable.checkDeltaGauge("Groceries", -50.00, -250.00, 1.00,
                                        "The amount is increasing - it was 50.00 in july 2008");
    budgetView.extras.checkDeltaGauge("Leisures", null, -50.00, 1.00,
                                      "This envelope was not used in july 2008");
    budgetView.variable.checkDeltaGauge("Clothes", -100.00, -50.00, -0.5,
                                        "The amount is decreasing - it was 100.00 in july 2008");

    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    budgetView.recurring.checkDeltaGauge("Electricity", -55.00, -55.00, 0.00,
                                         "The amount is the same as in june 2008");
    budgetView.variable.checkDeltaGauge("Clothes", -150.00, -50.00, -0.67,
                                        "The amount is decreasing - it was 150.00 in june 2008");

    timeline.selectMonth("2008/09");
    budgetView.variable.editSeries("Clothes").setAmount(0.00).validate();
    budgetView.variable.checkDeltaGauge("Clothes", -50.00, 0.00, -1.00,
                                        "The amount was 50.00 in august 2008, and it is set to zero in september 2008");
    budgetView.income.editSeries("Salary").setAmount(0.00).validate();
    budgetView.income.checkDeltaGauge("Salary", 1200.00, 0.00, -1.00,
                                      "The amount was 1200.00 in august 2008, and it is set to zero in september 2008");
  }

}
