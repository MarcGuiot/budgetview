package org.designup.picsou.functests.seriesgroups;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesGroupTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/01");
    super.setUp();
    getOperations().openPreferences().setFutureMonthsCount(6).validate();
    addOns.activateGroups();
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

    budgetView.variable.checkAddToGroupOptions("Leisures", "New group...");

    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 200.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n");

    budgetView.variable.checkSeriesGauge("Food", -80.00, -200.00);
    budgetView.variable.checkSeriesGauge("Groceries", -80.00, -200.00);
    budgetView.variable.checkGroupItems("Food");
    budgetView.variable.checkGroupToggleNotShown("Home");

    budgetView.variable.checkAddToGroupOptions("Leisures", "Groceries", "New group...");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201312);
    budgetView.variable.checkContent("| Groceries | 70.00 | 300.00 |\n" +
                                     "| Food      | 70.00 | 200.00 |\n" +
                                     "| Home      | 0.00  | 100.00 |\n" +
                                     "| Leisures  | 0.00  | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-70.00, -500.00);
    budgetView.variable.checkGroupItems("Food", "Home");

    timeline.selectMonth(201401);
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-180.00, -500.00);
    budgetView.variable.checkGroupItems("Food", "Home");

    // -- Groups can be collapsed / expanded --

    budgetView.variable.collapseGroup("Groceries").checkGroupToggleCollapsed("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
    budgetView.variable.checkTotalAmounts(-180.00, -500.00);
    budgetView.variable.checkGroupItems();

    budgetView.variable.expandGroup("Groceries").checkGroupToggleExpanded("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
    budgetView.variable.checkGroupItems("Food", "Home");

    // -- Delete group --

    budgetView.variable.deleteGroup("Groceries");
    budgetView.variable.checkContent("| Food     | 80.00  | 200.00 |\n" +
                                     "| Leisures | 100.00 | 200.00 |\n" +
                                     "| Home     | 0.00   | 100.00 |\n");
    budgetView.variable.checkAddToGroupOptions("Leisures", "New group...");
  }

  public void testGroupsAreShownInCategorizationView() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/15", -50.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/11", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);

    categorization.getVariable()
      .checkContainsSeries("Food", "Home", "Leisures")
      .checkContainsNoGroup();

    budgetView.variable.addToNewGroup("Food", "Groceries");
    categorization.getVariable()
      .checkSeriesListEquals("Home", "Leisures", "Food")
      .checkNoGroupSeriesListEquals("Home", "Leisures")
      .checkGroupContainsSeries("Groceries", "Food")
      .checkSelectedSeries("Leisures");

    categorization
      .selectTransaction("AUCHAN")
      .getVariable()
      .checkSelectedSeries("Food");

    budgetView.variable.addToGroup("Leisures", "Groceries");
    categorization.getVariable()
      .checkSeriesListEquals("Home", "Food", "Leisures")
      .checkNoGroupSeriesListEquals("Home")
      .checkGroupContainsSeries("Groceries", "Food", "Leisures")
      .checkSelectedSeries("Food");

    budgetView.variable.removeFromGroup("Food");
    categorization.getVariable()
      .checkSeriesListEquals("Food", "Home", "Leisures")
      .checkNoGroupSeriesListEquals("Food", "Home")
      .checkGroupContainsSeries("Groceries", "Leisures")
      .checkSelectedSeries("Food");

    operations.undo();
    categorization.getVariable()
      .checkSeriesListEquals("Home", "Food", "Leisures")
      .checkNoGroupSeriesListEquals("Home")
      .checkGroupContainsSeries("Groceries", "Food", "Leisures")
      .checkSelectedSeries("Food");

    budgetView.variable.deleteGroup("Groceries");
    categorization.getVariable()
      .checkSeriesListEquals("Food", "Home", "Leisures")
      .checkContainsNoGroup()
      .checkSelectedSeries("Food");
  }

  public void testMonthLimits() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/11/11", -60.00, "FNAC")
      .addTransaction("2013/12/12", -70.00, "Auchan")
      .addTransaction("2013/12/10", -50.00, "Monoprix")
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
    budgetView.variable.editSeries("Food").setStartDate(201312).validate();
    budgetView.variable.editSeries("Home").setStartDate(201312).validate();

    timeline.selectMonth(201311);
    budgetView.variable.checkContent("| Leisures | 60.00 | 200.00 |\n");

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

    budgetView.recurring.checkAddToGroupOptions("Electricity", "New group...");
    budgetView.recurring.addToNewGroup("Electricity", "Home");

    budgetView.variable.checkAddToGroupOptions("Leisures", "Groceries", "New group...");
    budgetView.recurring.checkAddToGroupOptions("Electricity", "Home", "New group...");
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

  public void testNavigatingToTransactions() throws Exception {
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
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");

    budgetView.variable.clickPlanned("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");

    budgetView.variable.clickPlanned("Groceries");
    budgetView.variable.checkContent("| Groceries | 80.00  | 300.00 |\n" +
                                     "| Food      | 80.00  | 200.00 |\n" +
                                     "| Home      | 0.00   | 100.00 |\n" +
                                     "| Leisures  | 100.00 | 200.00 |\n");
  }

  public void testHightlightingCollapsedGroups() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2014/01/09", -40.00, "Monoprix")
      .addTransaction("2014/01/10", -80.00, "Auchan")
      .addTransaction("2014/01/11", -30.00, "Lidl")
      .addTransaction("2014/01/12", -100.00, "FNAC")
      .addTransaction("2014/01/13", 1000.00, "WorldCo")
      .load();

    categorization.setNewIncome("WORLDCO", "Income");
    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);

    views.selectBudget();
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    mainAccounts.rollover(OfxBuilder.DEFAULT_ACCOUNT_NAME, 201401, 10);
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.variable.checkHighlighted("Food");

    budgetView.variable.collapseGroup("Groceries");

    mainAccounts.rollover(OfxBuilder.DEFAULT_ACCOUNT_NAME, 201401, 9);
    mainAccounts.rollover(OfxBuilder.DEFAULT_ACCOUNT_NAME, 201401, 10);
    budgetView.variable.checkHighlighted("Groceries");

    budgetView.variable.expandGroup("Groceries");

    mainAccounts.rollover(OfxBuilder.DEFAULT_ACCOUNT_NAME, 201401, 9);
    mainAccounts.rollover(OfxBuilder.DEFAULT_ACCOUNT_NAME, 201401, 10);
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.variable.checkHighlighted("Food");
  }

  public void testDeltaIndicatorInBudgetView() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2014/01/09", -100.00, "Monoprix")
      .addTransaction("2013/12/10", -150.00, "Auchan")
      .addTransaction("2014/01/10", -200.00, "Auchan")
      .load();

    categorization.setNewVariable("AUCHAN", "Food", -200.00);
    categorization.setNewVariable("MONOPRIX", "Home", -300.00);

    views.selectBudget();
    budgetView.variable.addToNewGroup("Food", "Groceries");
    budgetView.variable.addToGroup("Home", "Groceries");

    timeline.selectMonth(201312);
    budgetView.variable.checkDeltaGauge("Groceries", null, -150.00, 1.00, "This envelope was not used in november 2013");

    timeline.selectMonth(201401);
    budgetView.variable.checkDeltaGauge("Groceries", -150.00, -500.00, 1.00, "The amount is increasing - it was 150.00 in december 2013");
  }
}
