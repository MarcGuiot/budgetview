package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;

public class BudgetOverviewActivity extends FragmentActivity {

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    App app = (App)getApplication();
    TabPage page = new TabPage(this, app.getCurrentMonthId(), new TabPageHandler() {
      public Fragment createFragmentWithArgs(int monthId) {
        BudgetOverviewFragment fragment = new BudgetOverviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BudgetOverviewFragment.BUDGET_OVERVIEW_MONTH, monthId);
        fragment.setArguments(bundle);
        return fragment;
      }
    });
    page.initView();
  }
}
