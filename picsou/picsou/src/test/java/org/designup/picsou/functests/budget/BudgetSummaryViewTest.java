package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetSummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
  }

  public void testUncategorized() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/06/30", "2008/07/01", -20.00, "ED")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .load();

    views.selectBudget();
    timeline.checkSelection("2008/07");
    uncategorized
      .checkAmount(50.00)
      .gotoUncategorized();

    mainAccounts.getChart("Account n. 0001212")
      .checkRange(200807, 200807)
      .checkCurrentDay(200807, 5)
      .checkValue(200807, 1, 1550.00)
      .checkValue(200807, 5, 1500.00);

    categorization.initContent()
      .add("05/07/2008", "", "FNAC", -50)
      .check();
  }

  public void test() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectBudget();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .load();

    timeline.checkSelection("2008/07");
    timeline.selectAll();
    uncategorized
      .checkAmount(1000.00 + 200.00 + 50.00)
      .gotoUncategorized();

    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.checkNoSelectedTableRows();
    categorization.selectTransactions("WorldCo").selectIncome().createSeries("Salary");
    categorization.setNewVariable("Auchan", "Groceries", -200.);

    timeline.selectMonth("2008/07");
    views.selectBudget();
    accounts.checkContent("| ok | Account n. 0001212 | 1500.00 on 2008/07/05 |");
    mainAccounts.checkEndOfMonthPosition("Account n. 0001212", 2300.00);
    uncategorized.checkAmount(50.00);

    mainAccounts.getChart("Account n. 0001212")
      .checkRange(200807, 200808)
      .checkCurrentDay(200807, 5, "Jul 5")
      .checkValue(200807, 1, 1550.00)
      .checkValue(200807, 5, 2500.00)
      .checkValue(200807, 6, 2500.00)
      .checkValue(200807, 11, 2300.00)
      .checkValue(200808, 1, 2300.00)
      .checkValue(200808, 4, 3300.00)
      .checkValue(200808, 11, 3100.00);

    timeline.selectAll();
    mainAccounts.checkEndOfMonthPosition("Account n. 0001212", 3900.00);
    uncategorized
      .checkAmount(50.00);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 0001212", 1550.00);
    uncategorized.checkNotShown();

    views.selectCategorization();
    categorization.showAllTransactions();
    categorization.setNewVariable("FNAC", "Leisures", -50.);

    timeline.selectMonth("2008/07");
    views.selectBudget();
    mainAccounts
      .checkContent("| ok | Account n. 0001212 | 1500.00 on 2008/07/05 |")
      .checkEndOfMonthPosition("Account n. 0001212", 2300.00);
    uncategorized
      .checkNotShown();

    timeline.selectMonth("2008/09");
    views.selectBudget();
    mainAccounts
      .checkContent("| ok | Account n. 0001212 | 1500.00 on 2008/07/05 |")
      .checkEndOfMonthPosition("Account n. 0001212", 3800.00);
    uncategorized
      .checkNotShown();
  }

  public void testChartRolloverHighlightsCorrespondingSeries() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000111", 1500.00, "2008/06/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/06/10", -500.00, "FNAC")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "00222", 1000.00, "2008/06/10")
      .addTransaction("2008/06/10", -200.00, "Esso")
      .load();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewVariable("Esso", "Fuel", -60.00);
    categorization.setNewExtra("FNAC", "TV");

    accounts.checkContent(
      "| ok | Account n. 000111 | 1500.00 on 2008/06/10 |\n" +
      "| ok | Account n. 00222  | 1000.00 on 2008/06/10 |"
    );

    timeline.selectMonth("2008/06");

    views.selectBudget();

    mainAccounts.rollover("Account n. 000111", 200806, 3);
    budgetView.income.checkNotHighlighted("Salary");
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.extras.checkNotHighlighted("TV");
    budgetView.variable.checkNotHighlighted("Fuel");

    mainAccounts.rollover("Account n. 000111", 200806, 5);
    budgetView.income.checkHighlighted("Salary");
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.extras.checkNotHighlighted("TV");
    budgetView.variable.checkNotHighlighted("Fuel");

    mainAccounts.rollover("Account n. 000111", 200806, 10);
    budgetView.income.checkNotHighlighted("Salary");
    budgetView.variable.checkHighlighted("Groceries");
    budgetView.extras.checkHighlighted("TV");
    budgetView.variable.checkNotHighlighted("Fuel");

    mainAccounts.rollover("Account n. 000111", 200806, 15);
    budgetView.income.checkNotHighlighted("Salary");
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.extras.checkNotHighlighted("TV");

    mainAccounts.select("Account n. 00222");
    mainAccounts.rollover("Account n. 00222", 200806, 10);
    budgetView.income.checkNotHighlighted("Salary");
    budgetView.variable.checkNotHighlighted("Groceries");
    budgetView.extras.checkNotHighlighted("TV");
    budgetView.variable.checkHighlighted("Fuel");
  }

  public void testMultipleMainAccounts() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000111", 1000.00, "2008/07/10")
      .addTransaction("2008/07/05", 1000.00, "WorldCo")
      .addTransaction("2008/07/10", -100.00, "FNAC")
      .load();

    views.selectBudget();
    mainAccounts.checkContent(
      "| ok | Account n. 000111 | 1000.00 on 2008/07/10 |\n"
    );

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000222", -2000.00, "2008/07/20")
      .addTransaction("2008/07/15", -200.00, "Auchan")
      .addTransaction("2008/07/20", -20.00, "McDo")
      .load();
    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/07/15 |\n"
    );

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000333", 3000.00, "2008/07/30")
      .addTransaction("2008/07/20", -300.00, "Darty")
      .addTransaction("2008/07/25", -30.00, "Total")
      .load();

    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/07/15 |\n" +
      "| ok  | Account n. 000333 | 3000.00 on 2008/07/01  |"
    );
    mainAccounts.getChart("Account n. 000111")
      .checkValue(200807, 1, 100.00)
      .checkValue(200807, 5, 1100.00)
      .checkValue(200807, 10, 1000.00);

    mainAccounts.select("Account n. 000333");
    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/07/15 |\n" +
      "| ok  | Account n. 000333 | 3000.00 on 2008/07/01  |"
    );
    mainAccounts.getChart("Account n. 000333")
      .checkValue(200807, 1, 3000.00)
      .checkValue(200807, 20, 2700.00)
      .checkValue(200807, 25, 2670.00);

    // -- Uses global account sorting --

    views.selectBudget();
    projects.moveAccountUp("Account n. 000333");
    mainAccounts.checkContent(
      "| ok  | Account n. 000333 | 3000.00 on 2008/07/01  |\n" +
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/07/15 |"
    );

    // -- Reacts to deletions --

    views.selectBudget();
    mainAccounts.openDelete("000333").validate();
    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/07/15 |"
    );
    mainAccounts.getChart("Account n. 000111")
      .checkValue(200807, 1, 100.00)
      .checkValue(200807, 5, 1100.00)
      .checkValue(200807, 10, 1000.00);

    mainAccounts.openDelete("000222").validate();
    mainAccounts.checkContent(
      "| ok | Account n. 000111 | 1000.00 on 2008/07/10 |\n"
    );
    mainAccounts.getChart("Account n. 000111")
      .checkValue(200807, 1, 100.00)
      .checkValue(200807, 5, 1100.00)
      .checkValue(200807, 10, 1000.00);
  }

  public void testExcludesClosedAccounts() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000111", 1000.00, "2008/07/10")
      .addTransaction("2008/07/05", 1000.00, "WorldCo")
      .addTransaction("2008/07/10", -100.00, "FNAC")
      .addTransaction("2008/06/06", 1000.00, "WorldCo")
      .addTransaction("2008/06/12", -110.00, "Darty")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000222", -2000.00, "2008/06/20")
      .addTransaction("2008/06/15", -200.00, "Auchan")
      .addTransaction("2008/06/20", -20.00, "McDo")
      .load();

    timeline.selectMonth(200807);
    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/06/20 |"
    );
    mainAccounts.select("Account n. 000222");
    mainAccounts.checkContent(
      "| ok  | Account n. 000111 | 1000.00 on 2008/07/10  |\n" +
      "| nok | Account n. 000222 | -2000.00 on 2008/06/20 |"
    );
    mainAccounts.getChart("Account n. 000222")
      .checkValue(200807, 1, -2000.00);

    mainAccounts.edit("Account n. 000222")
      .setEndDate("2008/06/20")
      .validate();
    views.selectBudget();
    mainAccounts.checkContent(
      "| ok | Account n. 000111 | 1000.00 on 2008/07/10 |");

    timeline.selectMonth(200806);
    mainAccounts.checkContent(
      "| nok | Account n. 000111 | 1000.00 on 2008/07/10 |\n" +
      "| nok | Account n. 000222 | 0.00 on 2008/06/20    |");
    mainAccounts.getChart("Account n. 000111")
      .checkValue(200806, 1, -790.00)
      .checkValue(200806, 6, 210.00)
      .checkValue(200806, 12, 100.00)
      .checkValue(200807, 5, 1100.00)
      .checkValue(200807, 10, 1000.00);

    timeline.selectMonth(200807);
    mainAccounts.checkContent(
      "| ok | Account n. 000111 | 1000.00 on 2008/07/10 |");
  }
}
