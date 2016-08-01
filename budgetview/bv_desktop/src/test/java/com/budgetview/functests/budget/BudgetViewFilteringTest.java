package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class BudgetViewFilteringTest extends LoggedInFunctionalTestCase {

  @Test
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

    categorization.setNewIncome("WorldCo", "Salary", "Account n. 000111");
    categorization.setNewVariable("Auchan", "Groceries", -400.00, "Account n. 000222");
    categorization.setNewVariable("McDo", "Restaurant", -100.00, "Account n. 000333");
    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");
    categorization.setNewVariable("FNAC", "Leisures 111", -50.00, "Account n. 000111");
    categorization.setNewVariable("Darty", "Leisures 222", -100.00, "Account n. 000222");
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
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00 |\n" +
                                     "| Leisures 111 | 100.00 | 50.00 |");
    budgetView.variable.checkTotalAmounts(-100.00, -50.00);

    mainAccounts.select("Account n. 000333");
    budgetView.income.checkContent("");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.variable.checkContent("| Food       | 20.00 | 100.00 |\n" +
                                     "| Restaurant | 20.00 | 100.00 |");
    budgetView.variable.checkTotalAmounts(-20.00, -100.00);

    mainAccounts.select("Account n. 000222");
    budgetView.income.checkContent("");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.variable.checkContent("| Food         | 200.00 | 400.00 |\n" +
                                     "| Groceries    | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);

    budgetView.variable.collapseGroup("Food");
    budgetView.income.checkContent("");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.variable.checkContent("| Food         | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);

    mainAccounts.select("Account n. 000333");
    budgetView.income.checkContent("");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.variable.checkContent("| Food | 20.00 | 100.00 |");
    budgetView.variable.checkTotalAmounts(-20.00, -100.00);

    mainAccounts.select("Account n. 000111");
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00 |\n" +
                                     "| Leisures 111 | 100.00 | 50.00 |");
    budgetView.variable.checkTotalAmounts(-100.00, -50.00);

    mainAccounts.select("Account n. 000222");
    budgetView.variable.deleteGroup("Food");
    budgetView.income.checkContent("");
    budgetView.income.checkTotalAmounts(0.00, 0.00);
    budgetView.variable.checkContent("| Groceries    | 200.00 | 400.00 |\n" +
                                     "| Leisures     | 300.00 | 100.00 |\n" +
                                     "| Leisures 222 | 300.00 | 100.00 |");
    budgetView.variable.checkTotalAmounts(-500.00, -500.00);

    mainAccounts.openDelete("Account n. 000222").validate();
    mainAccounts.checkNoAccountsSelected();
    budgetView.income.checkContent("| Salary | 1000.00 | 1000.00 |");
    budgetView.income.checkTotalAmounts(1000.00, 1000.00);
    budgetView.variable.checkContent("| Leisures     | 100.00 | 50.00  |\n" +
                                     "| Leisures 111 | 100.00 | 50.00  |\n" +
                                     "| Restaurant   | 20.00  | 100.00 |");
    budgetView.variable.checkTotalAmounts(-120.00, -150.00);
  }

  @Test
  public void testActualForAGivenSeriesIsShownOnlyForSelectedAccount() throws Exception {
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
      .addTransaction("2008/07/15", -50.00, "Carrefour")
      .load();

    categorization.setNewIncome("WorldCo", "Salary", "Account n. 000111");
    categorization.setNewVariable("Auchan", "Groceries", -400.00);
    categorization.setVariable("Carrefour", "Groceries");
    budgetView.variable.addToNewGroup("Groceries", "Food");
    categorization.setNewVariable("McDo", "Restaurant", -50.00);
    budgetView.variable.addToGroup("Restaurant", "Food");
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setVariable("Darty", "Leisures");

    budgetView.variable.checkContent("| Food       | 270.00 | 450.00 |\n" +
                                     "| Groceries  | 250.00 | 400.00 |\n" +
                                     "| Restaurant | 20.00  | 50.00  |\n" +
                                     "| Leisures   | 400.00 | 200.00 |");

    mainAccounts.select("Account n. 000111");
    budgetView.variable.checkContent("| Food       | 0.00   | 450.00 |\n" +
                                     "| Groceries  | 0.00   | 400.00 |\n" +
                                     "| Restaurant | 0.00   | 50.00  |\n" +
                                     "| Leisures   | 100.00 | 200.00 |");

    mainAccounts.select("Account n. 000222");
    budgetView.variable.checkContent("| Food       | 200.00 | 450.00 |\n" +
                                     "| Groceries  | 200.00 | 400.00 |\n" +
                                     "| Restaurant | 0.00   | 50.00  |\n" +
                                     "| Leisures   | 300.00 | 200.00 |");

    mainAccounts.select("Account n. 000333");
    budgetView.variable.checkContent("| Food       | 70.00 | 450.00 |\n" +
                                     "| Groceries  | 50.00 | 400.00 |\n" +
                                     "| Restaurant | 20.00 | 50.00  |\n" +
                                     "| Leisures   | 0.00  | 200.00 |");

    budgetView.variable.collapseGroup("Food");
    budgetView.variable.checkContent("| Food     | 70.00 | 450.00 |\n" +
                                     "| Leisures | 0.00  | 200.00 |");

    mainAccounts.select("Account n. 000222");
    budgetView.variable.checkContent("| Food     | 200.00 | 450.00 |\n" +
                                     "| Leisures | 300.00 | 200.00 |");

    mainAccounts.select("Account n. 000333");
    budgetView.variable.checkContent("| Food     | 70.00 | 450.00 |\n" +
                                     "| Leisures | 0.00  | 200.00 |");

    mainAccounts.unselect("Account n. 000333");
    budgetView.variable.checkContent("| Food     | 270.00 | 450.00 |\n" +
                                     "| Leisures | 400.00 | 200.00 |");
  }

  @Test
  public void testFilteringRemovedWhenEnvelopeIsDeleted() throws Exception {
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
      .addTransaction("2008/07/15", -50.00, "Carrefour")
      .load();

    categorization.setNewIncome("WorldCo", "Salary", "Account n. 000111");
    categorization.setNewVariable("Auchan", "Groceries", -400.00);
    categorization.setVariable("Carrefour", "Groceries");
    budgetView.variable.addToNewGroup("Groceries", "Food");
    categorization.setNewVariable("McDo", "Restaurant", -50.00);
    budgetView.variable.addToGroup("Restaurant", "Food");
    categorization.setNewVariable("FNAC", "Leisures", -200.00);
    categorization.setVariable("Darty", "Leisures");

    budgetView.variable.gotoData("Restaurant");
    transactions.checkFilterMessage("Envelope: Restaurant");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00, "Restaurant")
      .check();

    budgetView.variable.openDeleteSeries("Leisures").uncategorize();
    transactions.checkFilterMessage("Envelope: Restaurant");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00, "Restaurant")
      .check();

    budgetView.variable.openDeleteSeries("Restaurant").uncategorize();
    transactions.checkNoFilterMessageShown();
    transactions.initContent()
      .add("25/07/2008", TransactionType.PRELEVEMENT, "TOTAL", "", -30.00)
      .add("20/07/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00)
      .add("20/07/2008", TransactionType.PRELEVEMENT, "DARTY", "", -300.00)
      .add("15/07/2008", TransactionType.PRELEVEMENT, "CARREFOUR", "", -50.00, "Groceries")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00)
      .add("05/07/2008", TransactionType.VIREMENT, "WORLDCO", "", 1000.00, "Salary")
      .check();
  }
}
