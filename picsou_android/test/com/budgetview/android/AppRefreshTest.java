package com.budgetview.android;

import com.budgetview.android.checkers.BudgetOverviewChecker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppRefreshTest extends AndroidAppTestCase {

  @Test public void testRefresh() throws Exception {

    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201303, 10, 1000.0)
      .addVariableSeries("Groceries", 201303, -500.00)
      .addTransactionToSeries("Carrefour", 5, -50.00);

    BudgetOverviewChecker budgetOverview = login();

    budgetOverview.checkSelectedTab("Mar");

    budgetOverview.initContent()
      .add("Variable", -500.00, -50.00)
      .check();

    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201303, 11, 900.0)
      .addVariableSeries("Groceries", 201303, -500.00)
      .addTransactionToSeries("Carrefour", 5, -50.00)
      .addTransactionToSeries("Auchan", 11, -100.00);

    budgetOverview = budgetOverview.header().refresh();

    budgetOverview.initAccountContent()
      .add("account1", 900.00, "on 11/3")
      .check();

    budgetOverview.initContent()
      .add("Variable", -500.00, -150.00)
      .check();

    dataSync.checkAllCallsProcessed();
  }

  @Test public void testRefreshWithNoConnection() throws Exception {

    dataSync.prepareLoad(EMAIL, PASSWORD)
      .addMainAccount("account1", 201303, 10, 1000.0)
      .addVariableSeries("Groceries", 201303, -500.00)
      .addTransactionToSeries("Carrefour", 5, -50.00);

    BudgetOverviewChecker budgetOverview = login();

    budgetOverview.checkSelectedTab("Mar");

    budgetOverview.initContent()
      .add("Variable", -500.00, -50.00)
      .check();

    dataSync.setConnectionAvailable(false);

    budgetOverview.header().refreshAndCheckError("You need an internet access to login");

    budgetOverview.initContent()
      .add("Variable", -500.00, -50.00)
      .check();

    budgetOverview.initAccountContent()
      .add("account1", 1000.00, "on 10/3")
      .check();

    dataSync.checkAllCallsProcessed();
  }

}
