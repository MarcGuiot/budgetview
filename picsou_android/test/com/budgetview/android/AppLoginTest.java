package com.budgetview.android;

import com.budgetview.android.checkers.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppLoginTest extends AndroidAppTestCase {

  @Test public void test() throws Exception {

    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201212, 31, 10.0)
      .addRecurringSeries("Mortgage", 201212, -1500.00)
      .addTransactionToSeries("Credit XYZ", 3, -1490.00)
      .addVariableSeries("Groceries ", 201212, -500.00)
      .addTransactionToSeries("Carrefour ", 5, -50.00)
      .addRecurringSeries("Mortgage", 201301, -1500.00)
      .addTransactionToSeries("Credit XYZ", 2, -1495.00)
      .addVariableSeries("Groceries ", 201301, -600.00)
      .addTransactionToSeries("Auchan ", 5, -50.00)
      .addRecurringSeries("Mortgage", 201302, -1600.00)
      .addVariableSeries("Groceries ", 201302, -200.00);

    HomeChecker home = app.start();

    BudgetOverviewChecker budgetOverview = home.login()
      .setEmail(EMAIL)
      .setPassword(PASSWORD)
      .enter();

    budgetOverview.checkTabNames("Dec", "Jan", "Feb");

    // --- January

    budgetOverview.checkSelectedTab("Jan");

    budgetOverview.initContent()
      .add("Recurring", -1500.00, -1495.00)
      .add("Variable", -600.00, -50.00)
      .check();

    SeriesListChecker recurring201301 = budgetOverview.edit("Recurring");
    recurring201301.initContent()
      .add("Mortgage", -1500.00, -1495.00)
      .check();

    TransactionListChecker mortgageTransactions201301 = recurring201301.edit("Mortgage");
    mortgageTransactions201301
      .checkSeriesSummary(-1500.00, -1495.00)
      .initContent()
      .add("on 2/1", "Credit XYZ", -1495.00)
      .check();

    // --- February

    budgetOverview.selectTab(2, "Feb");

    budgetOverview.initContent()
      .add("Recurring", -1600.00, 0.00)
      .add("Variable", -200.00, 0.00)
      .check();

    SeriesListChecker recurring201302 = budgetOverview.edit("Recurring");
    recurring201302.initContent()
      .add("Mortgage", -1600.00, 0.00)
      .check();

    TransactionListChecker mortgage201302 = recurring201302.edit("Mortgage");
    mortgage201302.checkEmpty();

    // --- January

    mortgage201302.selectTab(1, "Jan");

    mortgage201302.initContent()
      .add("on 2/1", "Credit XYZ", -1495.00)
      .check();

    TransactionPageChecker creditXYZ203101=  mortgage201302.edit("Credit XYZ");
    creditXYZ203101.checkDisplay("Credit XYZ", "on 2/1", -1495.00);
  }

  @Test public void testAccountOperations() throws Exception {
    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201212, 31, 10.0)
      .addRecurringSeries("Mortgage", 201212, -1500.00)
      .addTransactionToSeries("Credit XYZ", 3, -1490.00)
      .addVariableSeries("Groceries ", 201212, -500.00)
      .addTransactionToSeries("Carrefour ", 5, -50.00)
      .addRecurringSeries("Mortgage", 201301, -1500.00)
      .addTransactionToSeries("Credit XYZ", 2, -1495.00)
      .addVariableSeries("Groceries ", 201301, -600.00)
      .addTransactionToSeries("Auchan ", 5, -50.00)
      .addRecurringSeries("Mortgage", 201302, -1600.00)
      .addVariableSeries("Groceries ", 201302, -200.00);

    HomeChecker home = app.start();

    BudgetOverviewChecker budgetOverview = home.login()
      .setEmail(EMAIL)
      .setPassword(PASSWORD)
      .enter();

    budgetOverview.editAccount("account1")
      .checkAccountSummary(10.00, "on 31/12");
  }

  @Test public void testUncategorizedTransactions() throws Exception {
    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201212, 31, 10.0)
      .addRecurringSeries("Mortgage", 201301, -1500.00)
      .addTransactionToSeries("Credit XYZ", 2, -1495.00)
      .addVariableSeries("Groceries ", 201301, -600.00)
      .addTransactionToSeries("Auchan ", 5, -50.00)
      .addUncategorizedSeries(201301, -50.00)
      .addTransactionToSeries("Blah", 15, -50.00);

    HomeChecker home = app.start();

    BudgetOverviewChecker budgetOverview = home.login()
      .setEmail(EMAIL)
      .setPassword(PASSWORD)
      .enter();

    budgetOverview.checkSelectedTab("Jan");

    budgetOverview.initContent()
      .add("Recurring", -1500.00, -1495.00)
      .add("Variable", -600.00, -50.00)
      .add("Uncategorized", -50.00, -50.00)
      .check();

    TransactionListChecker uncategorized = budgetOverview.editUncategorized();
    uncategorized.initContent()
      .add("on 15/1", "Blah", -50.00)
      .check();

    TransactionPageChecker blah = uncategorized.edit("Blah");
    blah
      .checkDisplay("Blah", "on 15/1", -50.00)
      .checkUncategorized();
  }
}
