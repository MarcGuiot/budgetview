package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetViewFilteringTest extends LoggedInFunctionalTestCase {

  public void testPlannedAmountsAreHightlightedWhenAnAccountIsSelected() throws Exception {

    addOns.activateGroups();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000111", 1000.00, "2008/07/10")
      .addTransaction("2008/07/05", 1000.00, "WorldCo")
      .addTransaction("2008/07/10", -100.00, "FNAC")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000222", -2000.00, "2008/07/20")
      .addTransaction("2008/07/15", -200.00, "Auchan")
      .addTransaction("2008/07/20", -300.00, "Darty")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "000333", 3000.00, "2008/07/30")
      .addTransaction("2008/07/25", -30.00, "Total")
      .addTransaction("2008/07/20", -20.00, "McDo")
      .load();

    views.selectBudget();
    accounts.checkShowsAccounts("Account n. 000111", "Account n. 000222", "Account n. 000333");

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewVariable("Auchan", "Groceries", -400.00);
    categorization.setNewVariable("McDo", "Restaurant", -100.00);
    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");
    categorization.setNewVariable("FNAC", "Leisures 111", -50.00);
    categorization.setNewVariable("Darty", "Leisures 222", -100.00);
    budgetView.variable.addToNewGroup("Leisures 111", "Leisures");
    budgetView.variable.addToGroup("Leisures 222", "Leisures");

    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.variable.checkTotalAmounts(-620.00, -650.00);
    budgetView.variable.checkContent("| Food         | 220.00 | 500.00 |\n" +
                                     "| Groceries    | 200.00 | 400.00 |\n" +
                                     "| Restaurant   | 20.00  | 100.00 |\n" +
                                     "| Leisures     | 400.00 | 150.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |\n" +
                                     "| Leisures 111 | 100.00 | 50.00  |");

    mainAccounts.select("Account n. 000111");
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.variable.checkTotalAmounts(-100.00, -50.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00 |\n" +
                                     "| Leisures 111 | 100.00 | 50.00 |");

    mainAccounts.select("Account n. 000333");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.income.checkContent("");
    budgetView.variable.checkTotalAmounts(-20.00, -100.00);
    budgetView.variable.checkContent("| Food       | 20.00 | 100.00 |\n" +
                                     "| Restaurant | 20.00 | 100.00 |");

    mainAccounts.select("Account n. 000222");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.income.checkContent("");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);
    budgetView.variable.checkContent("| Food         | 200.00 | 400.00 |\n" +
                                     "| Groceries    | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");

    budgetView.variable.collapseGroup("Food");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.income.checkContent("");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);
    budgetView.variable.checkContent("| Food         | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");

    mainAccounts.select("Account n. 000333");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.income.checkContent("");
    budgetView.variable.checkTotalAmounts(-20.00, -100.00);
    budgetView.variable.checkContent("| Food | 20.00 | 100.00 |");

    mainAccounts.select("Account n. 000111");
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.variable.checkTotalAmounts(-100.00, -50.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00 |\n" +
                                     "| Leisures 111 | 100.00 | 50.00 |");

    mainAccounts.select("Account n. 000222");
    budgetView.variable.deleteGroup("Food");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.income.checkContent("");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);
    budgetView.variable.checkContent("| Groceries    | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");

    mainAccounts.openDelete("Account n. 000222").validate();
    mainAccounts.checkNoAccountsSelected();
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.variable.checkTotalAmounts(-120.00, -150.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00  |\n" +
                                     "| Leisures 111 | 100.00 | 50.00  |\n" +
                                     "| Restaurant   | 20.00  | 100.00 |");
  }
}
