package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class ProjectSlidingTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
    addOns.activateProjects();
  }

  public void testMovingProjectItemsWhenThereAreAlreadyAssociatedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/11/15", -50.00, "Resa")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201011, -200.00)
      .view(0)
      .checkCategorizationWarningNotShown();

    timeline.selectMonth("2010/11");
    categorization.setExtra("RESA", "Booking");
    budgetView.extras
      .checkTotalAmounts(-50.00, -200.00)
      .checkSeries("Trip", -50.00, -200.00);

    currentProject.view(0)
      .checkCategorizationWarningNotShown();

    projects.select("Trip");
    currentProject.toggleAndEditExpense(0)
      .setMonth(201012)
      .validate();
    currentProject.view(0)
      .checkCategorizationWarningShown()
      .clickCategorizationWarning();

    views.checkDataSelected();
    transactions.initContent()
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA", "", -50.00, "Booking")
      .check();
    timeline.checkSelection("2010/11");

    timeline.selectMonth(201011);
    budgetView.extras
      .checkTotalAmounts(-50.00, 0.00)
      .checkSeries("Trip", -50.00, 0.00);

    timeline.selectMonth(201012);
    budgetView.extras
      .checkTotalAmounts(0.00, -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    currentProject.view(0)
      .checkCategorizationWarningShown()
      .slideToPreviousMonth()
      .checkCategorizationWarningNotShown();

    timeline.selectMonth(201012);
    budgetView.extras.checkSeriesNotPresent("Trip");

    timeline.selectMonth(201011);
    budgetView.extras
      .checkTotalAmounts(-50.00, -200.00)
      .checkSeries("Trip", -50.00, -200.00);
  }

  public void testSlidingTheWholeProject() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/11/15", -100.00, "Resa")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201012, -200.00)
      .addExpenseItem(1, "Travel", 201102, -100.00)
      .addExpenseItem(2, "Hotel", 201102, -400.00);

    // --- Slide to January - March 2011

    views.selectHome();
    projects.select("Trip");
    currentProject
      .setFirstMonth(201101)
      .checkPeriod("January - March 2011")
      .checkProjectGauge(0.00, -700.00)
      .checkItems("| Booking | Jan | 0.00 | 200.00 |\n" +
                  "| Travel  | Mar | 0.00 | 100.00 |\n" +
                  "| Hotel   | Mar | 0.00 | 400.00 |");

    timeline.selectMonth(201012);
    budgetView.extras
      .checkTotalAmounts(0.00,  0.00)
      .checkSeriesNotPresent("Trip");

    timeline.selectMonth(201101);
    budgetView.extras
      .checkTotalAmounts(0.00,  -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    timeline.selectMonth(201103);
    budgetView.extras
      .checkTotalAmounts(0.00,  -500.00)
      .checkSeries("Trip", 0.00, -500.00);

    // --- Slide to October - December 2010

    currentProject
      .setFirstMonth(201010)
      .checkPeriod("October - December 2010")
      .checkProjectGauge(0.00, -700.00)
      .checkItems("| Booking | Oct | 0.00 | 200.00 |\n" +
                  "| Travel  | Dec | 0.00 | 100.00 |\n" +
                  "| Hotel   | Dec | 0.00 | 400.00 |");

    timeline.selectMonth("2010/12");
    budgetView.extras
      .checkTotalAmounts(0.00, -500.00)
      .checkSeries("Trip", 0.00, -500.00);

    timeline.selectMonth("2010/10");
    budgetView.extras
      .checkTotalAmounts(0.00, -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    // --- Slide right to November 2010 - December 2011

    currentProject
      .slideToNextMonth()
      .checkPeriod("November 2010 - January 2011")
      .checkProjectGauge(0.00, -700.00)
      .checkItems("| Booking | Nov | 0.00 | 200.00 |\n" +
                  "| Travel  | Jan | 0.00 | 100.00 |\n" +
                  "| Hotel   | Jan | 0.00 | 400.00 |");

    timeline.selectMonth("2010/12");
    budgetView.extras
      .checkTotalAmounts(0.00, 0.00)
      .checkSeriesNotPresent("Trip");

    timeline.selectMonth("2010/11");
    budgetView.extras
      .checkTotalAmounts(0.00, -200.00)
      .checkSeries("Trip", 0.00, -200.00);
  }

  public void testSlidingTheWholeProjectWhenTransactionsHaveAlreadyBeenAssigned() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -100.00, "Resa")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201012, -200.00)
      .addExpenseItem(1, "Travel", 201102, -100.00)
      .addExpenseItem(2, "Hotel", 201102, -400.00);

    timeline.selectMonth("2010/12");
    categorization.setExtra("RESA", "Booking");

    budgetView.extras
      .checkTotalAmounts(-100.00, -200.00)
      .checkSeries("Trip", -100.00, -200.00);

    // --- Slide to January - March 2011

    views.selectHome();
    projects.select("Trip");
    currentProject
      .setFirstMonth(201101)
      .checkPeriod("January - March 2011")
      .checkProjectGauge(-100.00, -700.00)
      .checkItems("| Booking | Jan | 100.00 | 200.00 |\n" +
                  "| Travel  | Mar | 0.00   | 100.00 |\n" +
                  "| Hotel   | Mar | 0.00   | 400.00 |");
    currentProject.view(0)
      .checkCategorizationWarningShown();
    currentProject.view(1)
      .checkCategorizationWarningNotShown();

    budgetView.extras
      .checkTotalAmounts(-100.00,  0.00)
      .checkSeries("Trip", -100.00, 0.00);

    timeline.selectMonth(201101);
    budgetView.extras
      .checkTotalAmounts(0.00,  -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    timeline.selectMonth(201103);
    budgetView.extras
      .checkTotalAmounts(0.00,  -500.00)
      .checkSeries("Trip", 0.00, -500.00);

    // --- Slide to October - December 2010

    currentProject
      .setFirstMonth(201010)
      .checkPeriod("October - December 2010")
      .checkProjectGauge(-100.00, -700.00)
      .checkItems("| Booking | Oct | 100.00 | 200.00 |\n" +
                  "| Travel  | Dec | 0.00   | 100.00 |\n" +
                  "| Hotel   | Dec | 0.00   | 400.00 |");
    currentProject.view(0)
      .checkCategorizationWarningShown();
    currentProject.view(1)
      .checkCategorizationWarningNotShown();

    timeline.selectMonth("2010/12");
    budgetView.extras
      .checkTotalAmounts(-100.00, -500.00)
      .checkSeries("Trip", -100.00, -500.00);

    timeline.selectMonth("2010/10");
    budgetView.extras
      .checkTotalAmounts(0.00, -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    // --- Slide right to November 2010 - January 2011

    currentProject
      .slideToNextMonth()
      .checkPeriod("November 2010 - January 2011")
      .checkProjectGauge(-100.00, -700.00)
      .checkItems("| Booking | Nov | 100.00 | 200.00 |\n" +
                  "| Travel  | Jan | 0.00   | 100.00 |\n" +
                  "| Hotel   | Jan | 0.00   | 400.00 |");
    currentProject.view(0)
      .checkCategorizationWarningShown();
    currentProject.view(1)
      .checkCategorizationWarningNotShown();

    timeline.selectMonth("2010/12");
    budgetView.extras
      .checkTotalAmounts(-100.00, 0.00)
      .checkSeries("Trip", -100.00, 0.00);

    timeline.selectMonth("2010/11");
    budgetView.extras
      .checkTotalAmounts(0.00, -200.00)
      .checkSeries("Trip", 0.00, -200.00);

    // --- Slide right to December 2010 - February 2011

    currentProject
      .slideToNextMonth()
      .checkPeriod("December 2010 - February 2011")
      .checkProjectGauge(-100.00, -700.00)
      .checkItems("| Booking | Dec | 100.00 | 200.00 |\n" +
                  "| Travel  | Feb | 0.00   | 100.00 |\n" +
                  "| Hotel   | Feb | 0.00   | 400.00 |");
    currentProject.view(0)
      .checkCategorizationWarningNotShown();
    currentProject.view(1)
      .checkCategorizationWarningNotShown();

    timeline.selectMonth("2010/12");
    budgetView.extras
      .checkTotalAmounts(-100.00, -200.00)
      .checkSeries("Trip", -100.00, -200.00);

    timeline.selectMonth("2010/11");
    budgetView.extras
      .checkTotalAmounts(0.00, -0.00)
      .checkSeriesNotPresent("Trip");
  }

  public void testCategorizationWarningShownAndHiddenOnCategorization() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -100.00, "Air France")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201012, -200.00)
      .addExpenseItem(1, "Travel", 201101, -100.00)
      .addExpenseItem(2, "Hotel", 201102, -400.00);
    currentProject.view(0)
      .checkCategorizationWarningNotShown();

    timeline.selectMonth("2010/12");
    categorization.setExtra("AIR FRANCE", "Travel");

    timeline.selectMonth(201012);
    budgetView.extras
      .checkTotalAmounts(-100.00, -200.00)
      .checkSeries("Trip", -100.00, -200.00);

    currentProject
      .checkPeriod("December 2010 - February 2011")
      .checkProjectGauge(-100.00, -700.00)
      .checkItems("| Booking | Dec | 0.00   | 200.00 |\n" +
                  "| Travel  | Jan | 100.00 | 100.00 |\n" +
                  "| Hotel   | Feb | 0.00   | 400.00 |");
    currentProject.view(0)
      .checkCategorizationWarningNotShown();
    currentProject.view(1)
      .checkCategorizationWarningShown()
      .clickCategorizationWarning();

    transactions.initContent()
      .add("15/12/2010", TransactionType.PRELEVEMENT, "AIR FRANCE", "", -100.00, "Travel")
      .check();
    timeline.checkSelection("2010/12");

    categorization.selectTransaction("AIR FRANCE").setUncategorized();

    currentProject
      .checkPeriod("December 2010 - February 2011")
      .checkProjectGauge(0.00, -700.00)
      .checkItems("| Booking | Dec | 0.00 | 200.00 |\n" +
                  "| Travel  | Jan | 0.00 | 100.00 |\n" +
                  "| Hotel   | Feb | 0.00 | 400.00 |");
    currentProject.view(1)
      .checkCategorizationWarningNotShown();
  }

  public void testCategorizationWarningTakesMultiMonthItemsIntoAccountDuringSliding() throws Exception {
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
      .addExpenseItem(0, "Camera Body", 201011, -300.00, 3)
      .addExpenseItem(1, "Lens", 201012, -100.00);
    categorization.setExtra("FNAC", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -400.00);
    currentProject.view(0).checkCategorizationWarningNotShown();

    currentProject.view(0)
      .slideToNextMonth()
      .checkCategorizationWarningNotShown();

    currentProject.view(0)
      .slideToNextMonth()
      .checkCategorizationWarningShown();

    currentProject.view(0)
      .slideToPreviousMonth()
      .checkCategorizationWarningNotShown();
  }
}
