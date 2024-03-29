package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;
import com.budgetview.android.components.UpHandler;
import com.budgetview.shared.model.BudgetAreaEntity;

import org.globsframework.model.Glob;
import org.globsframework.model.Key;

public class SeriesListActivity extends FragmentActivity {

    public static String MONTH_PARAMETER = "com.budgetview.seriesListActivity.parameters.month";
    public static String BUDGET_AREA_PARAMETER = "com.budgetview.seriesListActivity.parameters.series";

    public void onCreate(Bundle state) {
        super.onCreate(state);

        Intent intent = getIntent();
        int monthId = intent.getIntExtra(MONTH_PARAMETER, -1);
        final int budgetAreaId = intent.getIntExtra(BUDGET_AREA_PARAMETER, -1);

        App app = (App) getApplication();
        Glob budgetAreaEntity = app.getRepository().get(Key.create(BudgetAreaEntity.TYPE, budgetAreaId));
        String budgetAreaLabel = budgetAreaEntity.get(BudgetAreaEntity.LABEL);

        TabPage page = new TabPage(this, new SeriesListUpHandler(budgetAreaLabel, monthId),
                monthId, new TabPageHandler() {
            public Fragment createFragmentWithArgs(int monthId) {
                SeriesListFragment fragment = new SeriesListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SeriesListFragment.MONTH_PARAMETER, monthId);
                bundle.putInt(SeriesListFragment.BUDGET_AREA_PARAMETER, budgetAreaId);
                TabPage.copyDemoMode(SeriesListActivity.this, bundle);
                fragment.setArguments(bundle);
                return fragment;
            }
        });
        page.initView();
    }

    private class SeriesListUpHandler implements UpHandler {

        private final String budgetAreaLabel;
        private final int monthId;

        public SeriesListUpHandler(String budgetAreaLabel, int monthId) {
            this.budgetAreaLabel = budgetAreaLabel;
            this.monthId = monthId;
        }

        public String getLabel() {
            return budgetAreaLabel;
        }

        public void processUp() {
            Intent intent = new Intent(SeriesListActivity.this, BudgetOverviewActivity.class);
            intent.putExtra(BudgetOverviewActivity.MONTH_PARAMETER, monthId);
            TabPage.copyDemoMode(SeriesListActivity.this, intent);
            startActivity(intent);
        }
    }
}