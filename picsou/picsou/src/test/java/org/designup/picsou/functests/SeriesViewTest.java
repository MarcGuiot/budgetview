package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesViewTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {
    // TODO: REMOVE THIS TEST
    System.out.println("=======  SeriesViewTest : TO BE REMOVED =========");
  }

//  public void testStandardUsage() throws Exception {
//    OfxBuilder.init(this)
//      .addTransaction("2008/07/12", -95.00, "Auchan")
//      .addTransaction("2008/07/10", -50.00, "Monoprix")
//      .addTransaction("2008/07/05", -29.00, "Free Telecom")
//      .addTransaction("2008/07/04", -55.00, "EDF")
//      .addTransaction("2008/07/03", -15.00, "McDo")
//      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
//      .addTransaction("2008/07/01", 3540.00, "WorldCo")
//      .load();
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    views.selectCategorization();
//    categorization.setNewVariable("Auchan", "Groceries");
//    categorization.setVariable("Monoprix", "Groceries");
//    categorization.setNewRecurring("Free Telecom", "Internet");
//    categorization.setNewRecurring("EDF", "Electricity");
//    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
//    categorization.setNewIncome("WorldCo", "Salary");
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income", "Exceptional Income", "Salary",
//                         "Recurring", "Electricity", "Internet",
//                         "Variable", "Groceries",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.select("Income");
//    transactions.initContent()
//      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00, "Exceptional Income")
//      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
//      .check();
//
//    series.select("Salary");
//    transactions.initContent()
//      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
//      .check();
//
//    series.select("All");
//    transactions.initContent()
//      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
//      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
//      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00, "Internet")
//      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00, "Electricity")
//      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00, "To categorize")
//      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00, "Exceptional Income")
//      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
//      .check();
//
//    series.select("Groceries");
//    transactions.initContent()
//      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
//      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
//      .check();
//
//    series.select("To categorize");
//    transactions.initContent()
//      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00, "To categorize")
//      .check();
//
//    transactions.checkClearFilterButtonShown();
//    transactions.clearFilters();
//    transactions.initContent()
//      .add("12/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -95.00, "Groceries")
//      .add("10/07/2008", TransactionType.PRELEVEMENT, "MONOPRIX", "", -50.00, "Groceries")
//      .add("05/07/2008", TransactionType.PRELEVEMENT, "FREE TELECOM", "", -29.00, "Internet")
//      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00, "Electricity")
//      .add("03/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -15.00)
//      .add("02/07/2008", TransactionType.VIREMENT, "WORLDCO - BONUS", "", 200.00, "Exceptional Income")
//      .add("01/07/2008", TransactionType.VIREMENT, "WORLDCO", "", 3540.00, "Salary")
//      .check();
//    series.checkSelection("All");
//    transactions.checkClearFilterButtonHidden();
//  }
//
//  public void testCreatingAndDeletingSeries() throws Exception {
//    OfxBuilder.init(this)
//      .addTransaction("2008/07/12", -95.00, "Auchan")
//      .load();
//
//    views.selectBudget();
//    budgetView.variable.createSeries()
//      .setName("New envelope")
//      .validate();
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable", "New envelope",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    views.selectBudget();
//    budgetView.variable.editSeriesList()
//      .selectSeries("New envelope")
//      .deleteSelectedSeries()
//      .validate();
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//  }
//
//  public void testExpandingAndCollapsingBudgetAreas() throws Exception {
//    OfxBuilder.init(this)
//      .addTransaction("2008/07/12", -95.00, "Auchan")
//      .load();
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    views.selectBudget();
//    budgetView.income.createSeries("salary");
//    budgetView.variable.createSeries("envelope1");
//    budgetView.variable.createSeries("envelope2");
//    budgetView.recurring.createSeries("mortgage");
//
//    views.selectData();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income", "salary",
//                         "Recurring", "mortgage",
//                         "Variable", "envelope1", "envelope2",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.checkExpansionEnabled("All", false);
//    series.checkExpansionEnabled("To categorize", false);
//    series.checkExpansionEnabled("Income", true);
//    series.checkExpansionEnabled("Extras", false);
//
//    series.checkExpansionEnabled("Variable", true);
//    series.toggleExpansion("Variable");
//
//    series.checkContains("All",
//                         "To categorize",
//                         "Income", "salary",
//                         "Recurring", "mortgage",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.expandAll();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income", "salary",
//                         "Recurring", "mortgage",
//                         "Variable", "envelope1", "envelope2",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.collapseAll();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.toggleExpansion("Income");
//    series.expandAll();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income", "salary",
//                         "Recurring", "mortgage",
//                         "Variable", "envelope1", "envelope2",
//                         "Extras",
//                         "Savings",
//                         "Other");
//
//    series.collapseAll();
//    series.checkContains("All",
//                         "To categorize",
//                         "Income",
//                         "Recurring",
//                         "Variable",
//                         "Extras",
//                         "Savings",
//                         "Other");
//  }
}
