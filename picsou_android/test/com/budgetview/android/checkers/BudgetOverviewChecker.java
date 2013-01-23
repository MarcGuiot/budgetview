package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.TabHost;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.R;
import com.budgetview.android.components.TabPage;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricShadowOfLevel9;
import junit.framework.Assert;

public class BudgetOverviewChecker extends AndroidChecker<BudgetOverviewActivity> {

  public BudgetOverviewChecker() {
    super(BudgetOverviewActivity.class);
  }

  protected void callOnCreate(BudgetOverviewActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public void checkTabCount(int expectedCount) {
    TabHost tabHost = (TabHost)activity.findViewById(android.R.id.tabhost);
//    Assert.assertEquals(expectedCount, tabHost.getCurrentTab());
  }
}
