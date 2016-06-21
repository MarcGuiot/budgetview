package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;
import com.budgetview.android.components.UpHandler;

public class BudgetOverviewActivity extends FragmentActivity {

    public static String MONTH_PARAMETER = "com.budgetview.budgetOverviewActivity.parameters.month";

    private TabPage page;

    public void onCreate(final Bundle state) {
        super.onCreate(state);

        App app = (App) getApplication();
        page = new TabPage(this,
                new BudgetOverviewUpHandler(),
                getIntent().getIntExtra(MONTH_PARAMETER, app.getCurrentMonthId()),
                new TabPageHandler() {
                    public Fragment createFragmentWithArgs(int monthId) {
                        BudgetOverviewFragment fragment = new BudgetOverviewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(BudgetOverviewFragment.BUDGET_OVERVIEW_MONTH, monthId);
                        TabPage.copyDemoMode(BudgetOverviewActivity.this, bundle);
                        fragment.setArguments(bundle);
                        return fragment;
                    }
                });
        page.initView();
    }

    private class BudgetOverviewUpHandler implements UpHandler {
        public String getLabel() {
            return getResources().getString(R.string.app_name);
        }

        public void processUp() {
            if (DemoActivity.isInDemoMode(BudgetOverviewActivity.this)) {
                Intent intent = new Intent(BudgetOverviewActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            }

            LogoutBlockView.logout(BudgetOverviewActivity.this);
        }
    }
}