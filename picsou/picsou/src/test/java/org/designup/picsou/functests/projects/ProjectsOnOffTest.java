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

    projects.create();
    currentProject
      .setName("Trip")
      .addExpenseItem(0, "Reservation", 201012, -200.00)
      .addExpenseItem(1, "Equipment", 201012, -100.00)
      .addExpenseItem(2, "Hotel", 201101, -500.00);

    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    timeline.selectMonth("2010/12");
    views.selectHome();

    projectChart.select("Trip");
    currentProject.setInactive();
    currentProject.checkProjectGauge(0.00, -800.00);
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsInactive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    views.selectHome();
    currentProject.backToList();
    projects.checkCurrentProjects("| Trip | Dec | -800.00 | off |");
    projects.select("Trip");
    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    views.selectHome();
    currentProject.backToList();
    projects.checkCurrentProjects("| Trip | Dec | -800.00 | on |");
    projects.select("Trip");

    currentProject.view(0).setInactive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesContainsSubSeries("Equipment", "Hotel");

    currentProject.view(1).setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesContainsSubSeries("Hotel");

    currentProject.view(2).setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsInactive("Trip")
      .checkSeriesContainsNoSubSeries("Trip");

    currentProject.view(1).setActive();
    currentProject.view(2).setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Equipment", "Hotel");

    currentProject.setInactive();
    budgetView.extras.checkNoSeriesShown();
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsInactive("Trip")
      .checkSeriesContainsSubSeries("Equipment", "Hotel");

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -100.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Equipment", "Hotel");

    currentProject.view(0).setActive();
    budgetView.extras.checkSeries("Trip", 0.00, -300.00);
    categorization.selectTransaction("Expense 1").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");
  }

  public void testDisablingProjectElementsWithAssignedTransactions() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", -200.00, "Resa")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projects.create();
    currentProject
      .setName("Trip")
      .addExpenseItem(0, "Reservation", 201012, -200.00)
      .addExpenseItem(1, "Equipment", 201012, -100.00)
      .addExpenseItem(2, "Hotel", 201101, -500.00);

    categorization.setExtra("RESA", "Trip", "Reservation");

    timeline.selectMonth("2010/12");

    views.selectHome();
    projectChart.select("Trip");
    currentProject.setInactive();
    currentProject.checkProjectGauge(-200.00, -800.00);
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -300.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    currentProject.view(0).setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    currentProject.view(1).setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Hotel")
      .checkSeriesDoesNotContainSubSeries("Trip", "Equipment");

    currentProject.view(1).setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    currentProject.setInactive();
    budgetView.extras.checkSeries("Trip", -200.00, 0.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");

    currentProject.setActive();
    budgetView.extras.checkSeries("Trip", -200.00, -100.00);
    categorization.selectTransaction("RESA").selectExtras()
      .checkContainsSeries("Trip")
      .checkSeriesIsActive("Trip")
      .checkSeriesContainsSubSeries("Reservation", "Equipment", "Hotel");
  }
}
