package com.budgetview.android;

import com.budgetview.android.checkers.AndroidAppChecker;
import com.budgetview.android.checkers.BudgetOverviewChecker;
import com.budgetview.android.checkers.DataSyncChecker;
import com.budgetview.android.checkers.HomeChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppLoginTest {

  private DataSyncChecker dataSync;
  private AndroidAppChecker app;

  private static final String EMAIL = "test@mybudgetview.fr";
  private static final String PASSWORD = "pwd";

  @Before public void setUp() throws Exception {
    dataSync = new DataSyncChecker(false);
    app = new AndroidAppChecker();
  }

  @Test public void test() throws Exception {

    dataSync.acceptLogin(EMAIL);
    dataSync.prepareLoad()
      .addMainAccount("account1", 201301, 31, 10.0)
      .addRecurringSeries("Salary ", 201301, 1500.00)
      .addTransactionToSeries("My income", 2, 1500.00)
      .addVariableSeries("Groceries ", 201301, -300.00)
      .addTransactionToSeries("Auchan ", 5, -50.00);

    HomeChecker home = app.start();

    BudgetOverviewChecker budgetOverview = home.login()
      .setEmail(EMAIL)
      .setPassword(PASSWORD)
      .enter();

    budgetOverview.initBudgetAreaContent()
      .add("Recurring", 1500.00, 1500.00)
      .add("Variable", -300.00, -50.00)
      .check();
  }
}
