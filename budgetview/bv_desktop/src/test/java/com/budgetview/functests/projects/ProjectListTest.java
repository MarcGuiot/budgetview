package com.budgetview.functests.projects;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import org.junit.Test;

public class ProjectListTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
    operations.hideSignposts();
    addOns.activateProjects();
  }

  @Test
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

    projectList.checkCreationPageShown();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Past Aug")
      .addExpenseItem(0, "Item1", 201008, -200.00)
      .addExpenseItem(1, "Item2", 201010, -200.00)
      .backToList();

    projectList
      .checkListPageShown()
      .checkCurrentProjectsSectionHidden()
      .checkPastProjectsSectionCollapsed()
      .expandPastProjectsSection()
      .checkPastProjectsSectionExpanded();

    projectList.create();
    currentProject
      .setNameAndValidate("Current Jan")
      .addExpenseItem(0, "Item1", 201101, -200.00)
      .addExpenseItem(1, "Item2", 201102, -200.00)
      .backToList();

    projectList.checkCurrentProjectsSectionShown();
    projectList.checkPastProjectsSectionExpanded();

    projectList.create();
    currentProject
      .setNameAndValidate("Current Empty")
      .backToList();

    projectList.checkCurrentProjectsSectionShown();
    projectList.checkPastProjectsSectionExpanded();

    projectList.create();
    currentProject
      .setNameAndValidate("Past Oct")
      .addExpenseItem(0, "Item1", 201010, -200.00)
      .addExpenseItem(1, "Item2", 201011, -200.00)
      .backToList();

    projectList
      .checkPastProjectsSectionExpanded()
      .collapsePastProjectsSection()
      .checkPastProjectsSectionCollapsed()
      .expandPastProjectsSection()
      .checkPastProjectsSectionExpanded();

    projectList.create();
    currentProject
      .setNameAndValidate("Current Oct")
      .addExpenseItem(0, "Item1", 201010, -200.00)
      .addExpenseItem(1, "Item2", 201012, -200.00)
      .backToList();

    projectList.checkCurrentProjects(
      "| Current Empty |     | 0.00   | on |\n" +
      "| Current Oct   | Oct | 400.00 | on |\n" +
      "| Current Jan   | Jan | 400.00 | on |"
    );

    projectList.checkPastProjects(
      "| Past Oct | Oct | 400.00 | on |\n" +
      "| Past Aug | Aug | 400.00 | on |"
    );

    projectList.select("Current Jan");
    timeline.checkSelection("2011/01");
    currentProject.backToList();

    projectList.select("Past Oct");
    timeline.checkSelection("2010/10");
    currentProject.backToList();

  }

  @Test
  public void testCurrentAndPastSectionsAreHiddenWhenProjectsAreDeleted() throws Exception {
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

    views.selectHome();

    operations.openPreferences().setFutureMonthsCount(3).validate();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Past Aug")
      .addExpenseItem(0, "Item1", 201008, -200.00)
      .addExpenseItem(1, "Item2", 201010, -200.00)
      .backToList();

    projectList.expandPastProjectsSection();

    projectList.create();
    currentProject
      .setNameAndValidate("Current Jan")
      .addExpenseItem(0, "Item1", 201101, -200.00)
      .addExpenseItem(1, "Item2", 201102, -200.00)
      .backToList();

    projectList.checkCurrentProjectsSectionShown();
    projectList.checkPastProjectsSectionExpanded();

    projectList.delete("Current Jan");
    projectList.checkCurrentProjectsSectionHidden();
    projectList.checkPastProjectsSectionExpanded();

    projectList.checkShown();
    projectList.delete("Past Aug");
    projectList.checkCurrentProjectsSectionHidden();
    projectList.checkPastProjectsSectionHidden();
  }

  @Test
  public void testShowsOnlyProjectsInDisplayedTimeSpan() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/01/01", 1000.00, "Income")
      .addTransaction("2010/02/01", 1000.00, "Income")
      .addTransaction("2010/03/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    timeline.selectMonth(201101);

    projects.createFirst();
    currentProject
      .setNameAndValidate("Past Project")
      .addExpenseItem(0, "Reservation", 201007, -100.00)
      .addExpenseItem(1, "Hotel", 201008, -500.00);

    projects.create();
    currentProject
      .setNameAndValidate("Current Project")
      .addExpenseItem(0, "Reservation", 201101, -100.00)
      .addExpenseItem(1, "Hotel", 201102, -500.00);

    projects.create();
    currentProject
      .setNameAndValidate("Next Project")
      .addExpenseItem(0, "Reservation", 201105, -100.00);

    projects.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201103);
    projects
      .checkRange(201009, 201106)
      .checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201102);
    projects
      .checkRange(201009, 201106)
      .checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201106);
    projects
      .checkRange(201009, 201106)
      .checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201004);
    projects
      .checkRange(201004, 201101)
      .checkProjectList("Past Project", "Current Project");

    projects.select("Past Project");
    currentProject
      .toggleAndEditExpense(0)
      .setMonth(201001)
      .validate();
    currentProject
      .toggleAndEditExpense(1)
      .setMonth(201001)
      .validate();
    projects
      .checkRange(201004, 201101)
      .checkProjectList("Current Project");

    timeline.selectMonths(201006, 201105);
    projects.checkProjectList("Current Project", "Next Project");
  }
}
