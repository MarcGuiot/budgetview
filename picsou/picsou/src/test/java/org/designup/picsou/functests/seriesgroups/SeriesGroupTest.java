package org.designup.picsou.functests.seriesgroups;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesGroupTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/01");
    super.setUp();
  }

  public void testAddAndDeleteGroup() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2008/24/10", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);

    // -- Create a group with 2 envelopes --

    budgetView.variable.checkGroups("Leisures", "New group...");
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.checkGroups("Leisures", "Groceries", "New group...");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201312);
    budgetView.variable.checkContent("| Groceries | 70.00 | 300.00 |\n" +
                                     "| Food      | 70.00 | 200.00 |\n" +
                                     "| Home      | 0.00  | 100.00 |\n" +
                                     "| Leisures  | 0.00  | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-70.00, -500.00);

    timeline.selectMonth(201401);
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-180.00, -500.00);

    // -- Groups can be collapsed / expanded --

    budgetView.variable.collapseGroup("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-180.00, -500.00);

    budgetView.variable.expandGroup("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");

    // -- Groups not shown in Categorization view --

    categorization.selectTransaction("AUCHAN")
      .checkVariablePreSelected()
      .getVariable()
      .checkDoesNotContainSeries("Groceries");

    // -- Delete group --

    budgetView.variable.deleteGroup("Groceries");
    budgetView.variable.checkContent("| Food     | 80.00  | 200.00 |\n" +
                                     "| Leisures | 100.00 | 200.00 |\n" +
                                     "| Home     | 0.00   | 100.00 |\n");
    budgetView.variable.checkGroups("Leisures", "New group...");
  }

  public void testMonthLimits() throws Exception {
    fail("tbd");
  }

  public void testGroupsAreSpecificToBudgetAreas() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2008/24/10", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/11", -100.00, "EDF")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setNewRecurring("EDF", "Electricity");

    budgetView.variable.addToNewGroup("Food", "Groceries");

    budgetView.recurring.checkGroups("Electricity", "New group...");
    budgetView.recurring.addToNewGroup("Electricity", "Home");

    budgetView.variable.checkGroups("Leisures", "Groceries", "New group...");
    budgetView.recurring.checkGroups("Electricity", "Home", "New group...");
  }

  public void testCollapsingAndExpandingGroups() throws Exception {
    fail("tbd");
  }

  public void testMovingASeriesToAnotherGroupOrToNoGroup() throws Exception {
    fail("tbd");
  }

  public void testNavigationToTransactions() throws Exception {
    fail("tbd");
  }

  public void testCannotCreateAGroupWithAnEmptyName() throws Exception {
    fail("tbd");
  }

  public void testATreeNodeIsAddedInTheAnalysisTableForEachGroup() throws Exception {
    fail("tbd: vue analyse + navigation");
  }

  public void testGroupIsDeletedWhenLastSeriesIsRemoved() throws Exception {
    fail("tbd");
  }

  public void testRenameGroup() throws Exception {
    fail("tbd");
  }

  public void testHightlightingCollapsedGroups() throws Exception {
    fail("tbd");
  }

  public void testCommentForSubSeries() throws Exception {
    fail("tbd: dans SED/subseries, reference aux groupes");
  }
}
