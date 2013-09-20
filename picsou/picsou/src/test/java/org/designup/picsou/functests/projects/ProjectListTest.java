package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.model.ProjectStat;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobComparators;

public class ProjectListTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
    operations.hideSignposts();
  }

  public void testCurrentAndPreviousProjectsAreShownOnSeparateListsAndOrderedDifferently() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/08/01", 1000.00, "Income")
      .addTransaction("2010/09/01", 1000.00, "Income")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Expense 1")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projects.create();
    currentProject
      .setName("Past Aug")
      .addExpenseItem(0, "Item1", 201008, -200.00)
      .addExpenseItem(1, "Item2", 201010, -200.00);

    currentProject
      .create()
      .setName("Current Jan")
      .addExpenseItem(0, "Item1", 201101, -200.00)
      .addExpenseItem(1, "Item2", 201102, -200.00);

    currentProject
      .create()
      .setName("Current Empty");

    currentProject
      .create()
      .setName("Past Oct")
      .addExpenseItem(0, "Item1", 201010, -200.00)
      .addExpenseItem(1, "Item2", 201011, -200.00);

    currentProject
      .create()
      .setName("Current Oct")
      .addExpenseItem(0, "Item1", 201010, -200.00)
      .addExpenseItem(1, "Item2", 201012, -200.00);

    currentProject.backToList();

    projects.checkCurrentProjects(
      "| Current Empty |     | 0.00    | on |\n" +
      "| Current Oct   | Oct | -400.00 | on |\n" +
      "| Current Jan   | Jan | -400.00 | on |"
    );

    projects.checkPastProjects(
      "| Past Oct | Oct | -400.00 | on |\n" +
      "| Past Aug | Aug | -400.00 | on |"
    );

    projects.select("Current Jan");
    timeline.checkSelection("2011/01");
    currentProject.backToList();

    projects.select("Past Oct");
    timeline.checkSelection("2010/10");
    currentProject.backToList();

  }

  public void testShowsOnlyProjectsInDisplayedTimeSpan() throws Exception {

    fail("RM - en cours");

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/01/01", 1000.00, "Income")
      .addTransaction("2010/02/01", 1000.00, "Income")
      .addTransaction("2010/03/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    timeline.selectMonth(201101);

    currentProject
      .create()
      .setName("Past Project")
      .addExpenseItem(0, "Reservation", 201007, -100.00)
      .addExpenseItem(1, "Hotel", 201008, -500.00);

    currentProject
      .create()
      .setName("Current Project")
      .addExpenseItem(0, "Reservation", 201101, -100.00)
      .addExpenseItem(1, "Hotel", 201102, -500.00);

    currentProject
      .create()
      .setName("Next Project")
      .addExpenseItem(0, "Reservation", 201105, -100.00);

    projectChart.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201103);
    projectChart.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201102);
    projectChart.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201106);
    projectChart.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201004);
    projectChart.checkProjectList("Past Project");

    projectChart.select("Past Project");
    currentProject
      .toggleAndEditExpense(0)
      .setMonth(201109)
      .validate();
    projectChart.checkProjectList("Current Project");

    timeline.selectMonths(201006, 201105);
    projectChart.checkProjectList("Past Project", "Current Project", "Next Project");
  }
}
