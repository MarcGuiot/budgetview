package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
  }

  public void testCreatingAProject() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();

    projects.create()
      .checkTitle("Create a project")
      .setName("My project")
      .setItemName(0, "Reservation")
      .setItemDate(0, 201101)
      .setItemAmount(0, -200.00)
      .validate();

    projects.checkProjectList("My project");
    projects.checkProject("My project", "Jan 2011", 200.00);

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(800.00);

    projects.edit("My project")
      .addItem(1, "Travel", 201102, -100.00)
      .addItem(2, "Hotel", 201102, -500.00)
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Travel | February 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .validate();

    projects.checkProjectList("My project");
    projects.checkProject("My project", "Jan-Feb 2011", 800.00);
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(800.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeries("My project", 0, -600.00);
    budgetView.getSummary().checkEndPosition(200.00);

    projects.edit("My project")
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Travel | February 2011 | -100.00\n" +
                  "Hotel | February 2011 | -500.00")
      .deleteItem(1)
      .validate();

    timeline.selectMonth("2011/02");
    projects.checkProject("My project", "Jan-Feb 2011", 700.00);
    budgetView.extras.checkSeries("My project", 0, -500.00);
    budgetView.getSummary().checkEndPosition(300.00);

    projects.edit("My project")
      .checkItems("Reservation | January 2011 | -200.00\n" +
                  "Hotel | February 2011 | -500.00")
      .deleteItem(1)
      .validate();

    projects.checkProject("My project", "Jan 2011", 200.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeriesNotPresent("My project");

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(800.00);
  }

  public void testCannotHaveEmptyProjectOrProjectItemNames() {
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

    projects.checkProject("My project", "Jan 2011", 200.00);
  }

  public void testCannotHaveProjectWithNoItems() throws Exception {
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
//
//  public void testSeriesIsUpdatedWhenProjectIsUpdated() throws Exception {
//    fail("tbd");
//  }
//
//  public void testCannotEditOrDeleteProjectSeries() throws Exception {
//    fail("tbd");
//  }
}
