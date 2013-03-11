package com.budgetview.android;

import com.budgetview.android.checkers.AndroidAppChecker;
import com.budgetview.android.checkers.BudgetOverviewChecker;
import com.budgetview.android.checkers.DataSyncChecker;
import com.budgetview.android.checkers.HomeChecker;
import com.budgetview.android.shadow.CustomShadowTabHost;
import org.junit.Before;
import org.robolectric.Robolectric;

public class AndroidAppTestCase {

  protected DataSyncChecker dataSync;
  protected AndroidAppChecker app;

  protected static final String EMAIL = "test@mybudgetview.fr";
  protected static final String PASSWORD = "pwd";

  @Before public void setUp() throws Exception {
    Robolectric.bindShadowClass(CustomShadowTabHost.class);
    dataSync = new DataSyncChecker(false);
    app = new AndroidAppChecker();
  }

  protected BudgetOverviewChecker login() throws Exception {
    HomeChecker home = app.start();

    return home.login()
      .setEmail(EMAIL)
      .setPassword(PASSWORD)
      .enter();
  }
}
