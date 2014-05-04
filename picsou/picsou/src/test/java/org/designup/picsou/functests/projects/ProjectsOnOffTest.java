package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectsOnOffTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
  }

  public void testDisablingProjectElements() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Expense 1")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projectChart.create();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Reservation", 201012, -200.00)
      .addExpenseItem(1, "Equipment", 201012, -100.00)
      .addExpenseItem(2, "Hotel", 201101, -500.00);

    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");

    timeline.selectMonth("2010/12");
    views.selectHome();

    projectChart.select("Trip");
    currentProject.setInactive();
    currentProject.checkProjectGauge(0.00, -800.00);
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsNoSeries();

    views.selectHome();
    currentProject.backToList();
    projects.checkCurrentProjects("| Trip | Dec | 800.00 | off |");
    projects.select("Trip");
    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");

    views.selectHome();
    currentProject.backToList();
    projects.checkCurrentProjects("| Trip | Dec | 800.00 | on |");
    projects.select("Trip");

    currentProject.view(0).setInactive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Equipment", "Hotel")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");

    currentProject.view(1).setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Hotel")
      .checkSeriesIsInactive("Hotel");

    currentProject.view(2).setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupNotShown("Trip");

    currentProject.view(1).setActive();
    currentProject.view(2).setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Equipment", "Hotel")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");

    currentProject.setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsNoSeries();

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Equipment", "Hotel")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");

    currentProject.view(0).setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation")
      .checkSeriesIsActive("Equipment")
      .checkSeriesIsInactive("Hotel");
  }

  public void testDisablingProjectElementsWithAssignedTransactions() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", -200.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projectChart.create();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Reservation", 201012, -200.00)
      .addExpenseItem(1, "Equipment", 201012, -100.00)
      .addExpenseItem(2, "Hotel", 201101, -500.00);

    categorization.setExtra("RESA", "Reservation");

    timeline.selectMonth("2010/12");

    views.selectHome();
    projectChart.select("Trip");
    currentProject.setInactive();
    currentProject.checkProjectGauge(-200.00, -800.00);
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation")
      .checkSeriesIsActive("Reservation");

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -300.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation");

    currentProject.view(0).setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation");

    currentProject.view(1).setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Hotel")
      .checkSeriesIsActive("Reservation");

    currentProject.view(1).setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation");

    currentProject.setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation")
      .checkSeriesIsActive("Reservation");

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkGroupContainsSeries("Trip", "Reservation", "Equipment", "Hotel")
      .checkSeriesIsActive("Reservation");
  }

  public void testDisablingItemsDoesNotConfuseTheCategorizationWarning() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", -200.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projectChart.create();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Reservation", 201012, -200.00)
      .addExpenseItem(1, "Equipment", 201012, -100.00)
      .addExpenseItem(2, "Hotel", 201101, -500.00);

    categorization.setExtra("RESA", "Reservation");

    currentProject
      .view(2)
      .setInactive()
      .checkCategorizationWarningNotShown();
  }
}
