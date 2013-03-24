package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.Button;
import com.budgetview.android.HomeActivity;
import com.budgetview.android.R;
import org.robolectric.Robolectric;

public class HomeChecker extends AndroidChecker<HomeActivity> {

  public HomeChecker() {
    super(new HomeActivity());
  }

  protected void callOnCreate(HomeActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public LoginChecker login() throws Exception {
    Button connectButton = (Button)activity.findViewById(R.id.home_connect);
    Robolectric.shadowOf(connectButton).performClick();
    return new LoginChecker();
  }

  public BudgetOverviewChecker openDemo() throws Exception {
    Button demoButton = (Button)activity.findViewById(R.id.home_demo);
    Robolectric.shadowOf(demoButton).performClick();
    return new BudgetOverviewChecker();
  }
}
