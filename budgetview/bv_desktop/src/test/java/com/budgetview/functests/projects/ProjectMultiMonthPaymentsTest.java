package com.budgetview.functests.projects;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import org.junit.Test;

public class ProjectMultiMonthPaymentsTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
    addOns.activateProjects();
  }

  @Test
  public void testMultiMonthPaymentsWithSameAmounts() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Camera")
      .addExpenseItem(0, "Camera Body", 201012, -80.00, 10)
      .addExpenseItem(1, "Lens", 201012, -200.00);
    projects.checkProject("Camera", 201012, 201109, 1000.00);
    currentProject
      .checkPeriod("December 2010 - September 2011")
      .checkProjectGauge(0.00, -1000.00)
      .checkItems("| Camera Body | Dec | 0.00 | 800.00 |\n" +
                  "| Lens        | Dec | 0.00 | 200.00 |");

    categorization.setExtra("FNAC", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -280.00);

    currentProject.toggleAndEditExpense(0)
      .setAmount(-150.00)
      .setMonthCount(5)
      .validate();
    projects.checkProject("Camera", 201012, 201104, 950.00);
    currentProject
      .checkPeriod("December 2010 - April 2011")
      .checkProjectGauge(-200.00, -950.00)
      .checkItems("| Camera Body | Dec | 200.00 | 750.00 |\n" +
                  "| Lens        | Dec | 0.00   | 200.00 |");
    categorization.setExtra("FNAC", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -350.00);

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("Camera", 0.00, -150.00);

    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("Camera", 0.00, -150.00);

    timeline.selectMonth(201103);
    budgetView.extras.checkSeries("Camera", 0.00, -150.00);

    timeline.selectMonth(201104);
    budgetView.extras.checkSeries("Camera", 0.00, -150.00);

    timeline.selectMonth(201105);
    budgetView.extras.checkNoSeriesShown();
  }

  @Test
  public void testCannotEnterZeroOrNegativeNumbersAsMonthCount() throws Exception {

    accounts.createMainAccount("Main account", "4321", 1000.00);

    projects.createFirst();
    currentProject
      .setNameAndValidate("Camera")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Body")
      .setAmount(-100.00)
      .switchToSeveralMonths()
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
      .checkItems("| Body | Dec | 0.00 | 500.00 |");

    currentProject
      .toggleAndEditExpense(0)
      .enterMonthCount("-8")
      .checkMonthCount(8)
      .validateMonthCount()
      .checkNoTipShown()
      .validate();

    currentProject
      .checkPeriod("December 2010 - July 2011")
      .checkProjectGauge(0.00, -800.00)
      .checkItem(0, "Body", "Dec", 0.00, -800.00)
      .checkItems("| Body | Dec | 0.00 | 800.00 |");
  }
}
