package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.checkers.ProjectItemEditionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectAmountsTest  extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
    operations.hideSignposts();
  }

  public void testSwitchingBackAndForthBetweenAmountModes() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projects.create();
    ProjectItemEditionChecker itemEdition = currentProject
      .setName("MyProject")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Item 1")
      .setMonth(201012)
      .checkShowsSingleMonth()
      .setAmount(-50.00)
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .checkShowsMonthEditor();
    itemEdition
      .checkMonthAmounts("| Dec 2010 | -50.00 |")
      .setMonthAmount(0, -70.00)
      .checkTableMonthCount(1);
    itemEdition
      .setTableMonthCount(3)
      .checkMonthAmounts("| Dec 2010 | -70.00 |\n" +
                         "| Jan 2011 | 0.00   |\n" +
                         "| Feb 2011 | 0.00   |");
    itemEdition
      .selectRows(1,2)
      .setMonthAmount(-10.00)
      .checkMonthAmounts("| Dec 2010 | -70.00 |\n" +
                         "| Jan 2011 | -10.00 |\n" +
                         "| Feb 2011 | -10.00 |");
    itemEdition
      .selectRow(1)
      .setMonthAmount(-20.00)
      .checkMonthAmounts("| Dec 2010 | -70.00 |\n" +
                         "| Jan 2011 | -20.00 |\n" +
                         "| Feb 2011 | -10.00 |")
      .validate();

    currentProject.checkProjectGauge(0.00, -100.00);
    currentProject.view(0)
      .checkGauge(0.00, -100.00);

    timeline.selectMonth(201012);
    budgetView.extras.checkSeries("MyProject", 0.00, -70.00);

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("MyProject", 0.00, -20.00);

    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("MyProject", 0.00, -10.00);

    currentProject.toggleAndEditExpense(0)
      .checkShowsMonthEditor()
      .revertToSingleAmount()
      .checkShowsSingleAmount()
      .checkAmount(-70.00)
      .checkMonthCount(3)
      .validate();

    currentProject.checkProjectGauge(0.00, -210.00);
    currentProject.view(0)
      .checkGauge(0.00, -210.00);

  }

  public void testChangingTheMonthRangePreservesTheValues() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projects.create();
    currentProject
      .setName("MyProject")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Item 1")
      .setMonth(201012)
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .setTableMonthCount(3)
      .setMonthAmount(0, -70.00)
      .setMonthAmount(1, -20.00)
      .setMonthAmount(2, -10.00)
      .checkMonthAmounts("| Dec 2010 | -70.00 |\n" +
                         "| Jan 2011 | -20.00 |\n" +
                         "| Feb 2011 | -10.00 |")
      .validate();

    currentProject
      .checkProjectGauge(0.00, -100.00)
      .checkPeriod("December 2010 - February 2011");
    currentProject.view(0)
      .checkGauge(0.00, -100.00);

    currentProject.toggleAndEditExpense(0)
      .setMonth(201101)
      .checkMonthAmounts("| Jan 2011 | -70.00 |\n" +
                         "| Feb 2011 | -20.00 |\n" +
                         "| Mar 2011 | -10.00 |")
      .validate();

    currentProject
      .checkProjectGauge(0.00, -100.00)
      .checkPeriod("January - March 2011");
    currentProject.view(0)
      .checkGauge(0.00, -100.00);

    timeline.selectMonth(201012);
    budgetView.extras.checkSeriesNotPresent("MyProject");

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("MyProject", 0.00, -70.00);

    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("MyProject", 0.00, -20.00);

    timeline.selectMonth(201103);
    budgetView.extras.checkSeries("MyProject", 0.00, -10.00);

    currentProject.toggleAndEditExpense(0)
      .setTableMonthCount(5)
      .setMonthAmount(3, -25.00)
      .checkMonthAmounts("| Jan 2011 | -70.00 |\n" +
                         "| Feb 2011 | -20.00 |\n" +
                         "| Mar 2011 | -10.00 |\n" +
                         "| Apr 2011 | -25.00 |\n" +
                         "| May 2011 | 0.00   |")
      .validate();

    currentProject
      .checkProjectGauge(0.00, -125.00)
      .checkPeriod("January - May 2011");
    currentProject.view(0)
      .checkGauge(0.00, -125.00);

    timeline.selectMonth(201012);
    budgetView.extras.checkSeriesNotPresent("MyProject");

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("MyProject", 0.00, -70.00);

    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("MyProject", 0.00, -20.00);

    timeline.selectMonth(201103);
    budgetView.extras.checkSeries("MyProject", 0.00, -10.00);

    timeline.selectMonth(201104);
    budgetView.extras.checkSeries("MyProject", 0.00, -25.00);

    timeline.selectMonth(201105);
    budgetView.extras.checkSeries("MyProject", 0.00, 0.00);

    timeline.selectMonth(201106);
    budgetView.extras.checkSeriesNotPresent("MyProject");
  }

  public void testChangingTheStartingMonthFromTheItemView() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projects.create();
    currentProject
      .setName("MyProject")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Item 1")
      .setMonth(201012)
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .setTableMonthCount(3)
      .setMonthAmount(0, -70.00)
      .setMonthAmount(1, -20.00)
      .setMonthAmount(2, -10.00)
      .checkMonthAmounts("| Dec 2010 | -70.00 |\n" +
                         "| Jan 2011 | -20.00 |\n" +
                         "| Feb 2011 | -10.00 |")
      .validate();

    currentProject
      .checkProjectGauge(0.00, -100.00)
      .checkPeriod("December 2010 - February 2011");
    currentProject.view(0)
      .checkGauge(0.00, -100.00);

    currentProject.view(0)
      .slideToNextMonth();

    currentProject
      .checkProjectGauge(0.00, -100.00)
      .checkPeriod("January - March 2011");
    currentProject.view(0)
      .checkGauge(0.00, -100.00);

    timeline.selectMonth(201012);
    budgetView.extras.checkSeriesNotPresent("MyProject");

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("MyProject", 0.00, -70.00);

    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("MyProject", 0.00, -20.00);

    timeline.selectMonth(201103);
    budgetView.extras.checkSeries("MyProject", 0.00, -10.00);
  }
}
