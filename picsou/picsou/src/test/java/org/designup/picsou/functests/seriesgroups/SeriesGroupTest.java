package org.designup.picsou.functests.seriesgroups;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesGroupTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/01");
    super.setUp();
    getOperations().openPreferences().setFutureMonthsCount(6).validate();
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
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201404);
    budgetView.variable.editSeries("Food").setEndDate(201401).validate();
    budgetView.variable.checkContent("| Leisures  | 0.00 | 200.00 |\n" +
                                     "| Groceries | 0.00 | 100.00 |\n" +
                                     "| Home      | 0.00 | 100.00 |\n");

    budgetView.variable.editSeries("Home").setEndDate(201402).validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 200.00 |\n");

    timeline.selectMonth(201402);
    budgetView.variable.checkContent("| Leisures  | 0.00 | 200.00 |\n" +
                                     "| Groceries | 0.00 | 100.00 |\n" +
                                     "| Home      | 0.00 | 100.00 |\n");

    timeline.selectMonth(201401);
    budgetView.variable.editSeries("Home")
      .clearEndDate()
      .setPropagationEnabled()
      .validate();

    timeline.selectMonth(201404);
    budgetView.variable.checkContent("| Leisures  | 0.00 | 200.00 |\n" +
                                     "| Groceries | 0.00 | 100.00 |\n" +
                                     "| Home      | 0.00 | 100.00 |\n");
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

  public void testMovingASeriesToAnotherGroupOrToNoGroup() throws Exception {
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
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201401);
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");

    budgetView.variable.addToNewGroup("Home", "Other");
    budgetView.variable.checkContent("| Groceries | 80.00  | 200.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n" +
                                     "| Other     | 0.00   | 100.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n");

    budgetView.variable.removeFromGroup("Home");
    budgetView.variable.checkContent("| Groceries | 80.00  | 200.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n");

    budgetView.variable.openDeleteSeries("Food").uncategorize();
    budgetView.variable.checkContent("| Leisures | 100.00 | 200.00 |\n" +
                                     "| Home     | 0.00   | 100.00 |\n");
  }

  public void testNavigationToTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/20", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);

    timeline.selectMonths(201312, 201401);
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    views.selectBudget();
    budgetView.variable.gotoData("Groceries");
    views.checkDataSelected();
    transactions.initContent()
      .add("10/01/2014", TransactionType.PRELEVEMENT, "AUCHAN", "", -80.00, "Food")
      .add("20/12/2013", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Home")
      .add("12/12/2013", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Food")
      .check();

    transactions.clearCurrentFilter();
    transactions.initContent()
      .add("11/01/2014", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Leisures")
      .add("11/01/2014", TransactionType.PRELEVEMENT, "LIDL", "", -30.00)
      .add("10/01/2014", TransactionType.PRELEVEMENT, "AUCHAN", "", -80.00, "Food")
      .add("20/12/2013", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Home")
      .add("12/12/2013", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Food")
      .check();

    views.selectBudget();
    budgetView.variable.gotoDataThroughMenu("Groceries");
    views.checkDataSelected();
    transactions.initContent()
      .add("10/01/2014", TransactionType.PRELEVEMENT, "AUCHAN", "", -80.00, "Food")
      .add("20/12/2013", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Home")
      .add("12/12/2013", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Food")
      .check();
  }

  public void testNamingAndRenamingGroups() throws Exception {
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

    views.selectBudget();
    budgetView.variable.addToNewGroup("Food")
      .checkNameError("You must enter a name")
      .setName("Groceries")
      .checkNoError()
      .validate();
    budgetView.variable.checkContent("| Groceries | 80.00  | 200.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n");

    budgetView.variable.addToNewGroup("Food")
      .checkNameError("You must enter a name")
      .setName("Groceries")
      .checkNoError()
      .validate();
    budgetView.variable.checkContent("| Groceries | 80.00  | 200.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n");

    budgetView.variable.renameGroup("Groceries")
      .checkName("Groceries")
      .setName("")
      .checkNameError("You must enter a name")
      .setName("Misc")
      .checkNoError()
      .validate();
    budgetView.variable.checkContent("| Misc     | 80.00  | 200.00 |\n" +
                                     "| Food     | 80.00  | 200.00 |\n" +
                                     "| Leisures | 100.00 | 200.00 |\n" +
                                     "| Home     | 0.00   | 100.00 |\n");
  }

  public void testPlannedButtonForGroupTriggersExpand() throws Exception {
    fail("tbd");
  }

  public void testATreeNodeIsAddedInTheAnalysisTableForEachGroup() throws Exception {
    fail("tbd: vue analyse + navigation");
  }

  public void testHightlightingCollapsedGroups() throws Exception {
    fail("tbd");
  }

  public void testCommentForSubSeries() throws Exception {
    fail("tbd: dans SED/subseries, reference aux groupes");
  }

  public void testDeltaIndicatorInBudgetView() throws Exception {
    fail("tbd");
  }

  public void testPrinting() throws Exception {
    fail("tbd");
  }
}
