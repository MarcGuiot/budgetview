package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectMultiMonthPaymentsTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
  }

  public void testMultiMonthPayments() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projectChart.create();
    currentProject
      .setName("Camera")
      .addExpenseItem(0, "Camera Body", 201012, -80.00, 10)
      .addExpenseItem(1, "Lens", 201012, -200.00);
    projectChart.checkProject("Camera", 201012, 201109, 1000.00);
    currentProject
      .checkPeriod("December 2010 - September 2011")
      .checkProjectGauge(0.00, -1000.00)
      .checkItems("Camera Body | Dec | 0.00 | -800.00\n" +
                  "Lens | Dec | 0.00 | -200.00");

    categorization.setExtra("FNAC", "Camera", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -280.00);

    currentProject.toggleAndEditExpense(0)
      .setAmount(-150.00)
      .setMonthCount(5)
      .validate();
    projectChart.checkProject("Camera", 201012, 201104, 950.00);
    currentProject
      .checkPeriod("December 2010 - April 2011")
      .checkProjectGauge(-200.00, -950.00)
      .checkItems("Camera Body | Dec | -200.00 | -750.00\n" +
                  "Lens | Dec | 0.00 | -200.00");
    categorization.setExtra("FNAC", "Camera", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -350.00);
  }

  public void testCannotEnterZeroOrNegativeNumbersAsMonthCount() throws Exception {
    projectChart.create();
    currentProject
      .setName("Camera")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Body")
      .setAmount(-100.00)
      .setMonthCount(0);
    currentProject
      .editExpense(0)
      .validateAndCheckMonthCountTip("You must set a duration of at least one month")
      .setMonthCount(5)
      .checkNoTipShown()
      .validate();

    currentProject
      .checkPeriod("December 2010 - April 2011")
      .checkProjectGauge(0.00, -500.00)
      .checkItems("Body | Dec | 0.00 | -500.00");

    currentProject
      .toggleAndEditExpense(0)
      .setMonthCount(-8)
      .validateAndCheckMonthCountTip("You must set a duration of at least one month")
      .setMonthCount(8)
      .checkNoTipShown()
      .validate();

    currentProject
      .checkPeriod("December 2010 - July 2011")
      .checkProjectGauge(0.00, -800.00)
      .checkItem(0, "Body", "Dec", 0.00, -800.00)
      .checkItems("Body | Dec | 0.00 | -800.00");
  }

}
