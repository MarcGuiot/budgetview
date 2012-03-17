package org.designup.picsou.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.ProjectEditionChecker;
import org.designup.picsou.functests.checkers.components.TipChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
  }

  public void testCreatingAProject() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2011/01/01", 1000.00, "Income")
      .addTransaction("2011/01/01", -100.00, "Resa Travel Plus")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.checkHintMessageDisplayed();

    projects.create()
      .checkTitle("Create a project")
      .checkGauge(0.00, 0.00)
      .setName("My project")
      .setItemName(0, "Reservation")
      .setItemDate(0, 201101)
      .setItemAmount(0, -200.00)
      .checkGauge(0.00, -200.00)
      .validate();

    projects.checkHintMessageHidden();

    projects.checkProjectList("My project");
    projects.checkProject("My project", 201101, 201101, 200.00);

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(1700.00);

    projects.edit("My project")
      .addItem(1, "Travel", 201102, -100.00)
      .checkGauge(0.00, -300.00)
      .addItem(2, "Hotel", 201102, -500.00)
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Travel | February 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .checkGauge(0.00, -800.00)
      .validate();

    projects.checkProjectList("My project");
    projects.checkProject("My project", 201101, 201102, 800.00);
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(1700.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeries("My project", 0, -600.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    projects.edit("My project")
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Travel | February 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .deleteItem(1)
      .checkGauge(0.00, -700.00)
      .validate();

    categorization.setExtra("Resa Travel Plus", "My project");

    timeline.selectMonth("2011/02");
    projects.checkProject("My project", 201101, 201102, 700.00);
    budgetView.extras.checkSeries("My project", 0, -500.00);
    budgetView.getSummary().checkEndPosition(1300.00);

    projects.edit("My project")
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Hotel | February 2011 | -500.00")
      .setItemAmount(1, -50.00)
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Hotel | February 2011 | -50.00")
      .deleteItem(1)
      .checkItems("Reservation | January 2011 | -200.00")
      .checkGauge(-100.00, -200.00)
      .validate();

    timeline.selectMonth("2011/01");
    projects.checkProject("My project", 201101, 201101, 200.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeriesNotPresent("My project");

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", -100.00, -200.00);
    budgetView.getSummary().checkEndPosition(1800.00);

    projects.edit("My project").deleteWithConfirmation("Existing operations",
                                                       "Some operations have been assigned to this project");
    projects.checkNoProjectShown();
    budgetView.getSummary().checkEndPosition(1900.00);
  }

  public void testProjectDeletionWithNoAssignedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.create()
      .checkTitle("Create a project")
      .setName("My project")
      .setItem(0, "Reservation", 201101, -200.00)
      .addItem(1, "Hotel", 201101, -300.00)
      .validate();

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("My project", 0.00, -500.00);
    budgetView.getSummary().checkEndPosition(500.00);

    projects.edit("My project")
      .delete();

    projects.checkNoProjectShown();
    budgetView.extras.checkNoSeriesShown();
    budgetView.getSummary().checkEndPosition(1000.00);
  }

  public void testProjectDeletionWithAssignedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.checkHintMessageDisplayed();

    projects.create()
      .checkTitle("Create a project")
      .setName("My project")
      .setItem(0, "Reservation", 201101, -200.00)
      .addItem(1, "Hotel", 201101, -500.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 800.00, "2011/01/10")
      .addTransaction("2011/01/01", -200.00, "Resa")
      .addTransaction("2011/01/01", -500.00, "Hotel")
      .addTransaction("2011/01/01", -10.00, "Something else")
      .load();

    projects.checkHintMessageHidden();

    categorization.setExtra("Resa", "My project");
    categorization.setExtra("Hotel", "My project");

    projects.edit("My project")
      .openDeleteAndNavigate();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "My project", "HOTEL", -500.0},
      {"01/01/2011", "My project", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);
    categorization.unselectAllTransactions();
    categorization.showAllTransactions();

    projects.edit("My project")
      .deleteWithConfirmation("Existing operations",
                              "Some operations have been assigned to this project");
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "", "HOTEL", -500.0},
      {"01/01/2011", "", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);

    projects.checkNoProjectShown();
    budgetView.getSummary().checkEndPosition(800.00);

    projects.checkHintMessageDisplayed();
  }

  public void testCannotHaveEmptyProjectOrProjectItemNames() {

    operations.hideSignposts();

    projects.create()
      .validateAndCheckOpen()
      .checkProjectNameMessage("You must provide a name for this project")
      .setName("My project")
      .checkNoErrorTipDisplayed()
      .validateAndCheckOpen()
      .checkProjectItemMessage(0, "You must provide a name for this item")
      .setItemName(0, "Item 1")
      .checkNoErrorTipDisplayed()
      .setItemDate(0, 201101)
      .setItemAmount(0, -200.00)
      .validate();

    projects.checkProject("My project", 201101, 201101, 200.00);
  }
  
  public void testDeletingAnItemWithTipDisablesTip() throws Exception {
    operations.hideSignposts();

    ProjectEditionChecker dialog = projects.create()
      .validateAndCheckOpen()
      .setName("My project")
      .setItem(0, "Item 1", 201101, 100.00)
      .addItem();

    TipChecker tip = dialog.validateAndCheckOpen().getProjectItemTip(1);
    tip.checkVisible();

    dialog.deleteItem(1);
    tip.checkHidden();

    dialog.cancel();
  }

  public void testCannotHaveProjectWithNoItems() throws Exception {

    operations.hideSignposts();

    projects.create()
      .checkTitle("Create a project")
      .setName("My project")
      .setItemName(0, "Reservation")
      .setItemDate(0, 201101)
      .setItemAmount(0, -200.00)
      .deleteItem(0)
      .checkItems(" | December 2010 | 0.00")
      .cancel();
  }

  public void testProjectDialogIsAlwaysShownWhenEditingTheSeries() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    budgetView.extras.createProject()
      .setName("Past Project")
      .setItem(0, "Reservation", 201101, -100.00)
      .addItem(1, "Hotel", 201102, -500.00)
      .validate();

    budgetView.extras.editProjectSeries("Past Project")
      .checkItems("Reservation | January 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .cancel();

    budgetView.extras.editPlannedAmountForProject("Past Project")
      .checkItems("Reservation | January 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .cancel();

    timeline.selectAll();
    categorization.showAllTransactions();
    categorization.selectTransaction("Resa");

    categorization.selectExtras().selectSeries("Past Project");
    categorization.selectExtras().editProjectSeries("Past Project")
      .checkItems("Reservation | January 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .cancel();
  }

  public void testShowsOnlyProjectsInDisplayedTimeSpan() throws Exception {

    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/01/01", 1000.00, "Income")
      .addTransaction("2010/02/01", 1000.00, "Income")
      .addTransaction("2010/03/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    timeline.selectMonth(201101);

    projects.create()
      .setName("Past Project")
      .setItem(0, "Reservation", 201001, -100.00)
      .addItem(1, "Hotel", 201002, -500.00)
      .validate();

    projects.create()
      .setName("Current Project")
      .setItem(0, "Reservation", 201101, -100.00)
      .addItem(1, "Hotel", 201102, -500.00)
      .validate();

    projects.create()
      .setName("Next Project")
      .setItem(0, "Reservation", 201105, -100.00)
      .addItem(1, "Hotel", 201105, -500.00)
      .validate();

    projects.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201103);
    projects.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201102);
    projects.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201106);
    projects.checkProjectList("Current Project", "Next Project");

    timeline.selectMonth(201002);
    projects.checkProjectList("Current Project", "Past Project");

    projects.edit("Past Project")
      .setItemDate(1, 201010)
      .setItemDate(1, 201010)
      .validate();
    projects.checkProjectList("Current Project", "Past Project");

    timeline.selectMonths(201101, 201105);
    projects.checkProjectList("Past Project", "Current Project", "Next Project");
  }

  public void testCanCreateProjectsFromTheCategorizationView() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    categorization.selectTransaction("Resa");
    categorization.selectExtras()
      .createProject()
      .setName("My project")
      .setItem(0, "Trip", 201101, -150.00)
      .validate();

    projects.checkProjectList("My project");
    projects.edit("My project")
      .checkItems("Trip | January 2011 | -150.00")
      .validate();
  }

  public void testManagesProjectsWithPositiveAmounts() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    projects
      .create()
      .setName("My project")
      .setItem(0, "Bonus", 201101, 1000.00)
      .validate();

    projects.checkProjectList("My project");
    projects.edit("My project")
      .checkItems("Bonus | January 2011 | +1000.00")
      .validate();

    budgetView.extras.checkSeries("My project", 0, 1000.00);
  }
}
