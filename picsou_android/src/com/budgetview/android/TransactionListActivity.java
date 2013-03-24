package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;
import com.budgetview.android.components.UpHandler;
import com.budgetview.android.utils.TransactionSet;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.SeriesValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidState;

public class TransactionListActivity extends FragmentActivity {

  public void onCreate(Bundle state) {
    super.onCreate(state);

    Intent intent = getIntent();
    GlobRepository repository = ((App)getApplication()).getRepository();
    final TransactionSet transactionSet = new TransactionSet(intent, repository);

    TabPage page = new TabPage(this,
                               new TransactionListUpActivity(transactionSet),
                               transactionSet.getMonthId(), new TabPageHandler() {
      public Fragment createFragmentWithArgs(int monthId) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle bundle = new Bundle();
        transactionSet.copy(monthId).save(bundle);
        fragment.setArguments(bundle);
        return fragment;
      }
    }
    );
    page.initView();
  }

  private class TransactionListUpActivity implements UpHandler {
    private final TransactionSet transactionSet;

    public TransactionListUpActivity(TransactionSet transactionSet) {
      this.transactionSet = transactionSet;
    }

    public String getLabel() {
      return transactionSet.getSectionLabel();
    }

    public void processUp() {
      if (transactionSet.isSeriesList()) {
        Intent intent = new Intent(TransactionListActivity.this, SeriesListActivity.class);
        intent.putExtra(SeriesListActivity.MONTH_PARAMETER, transactionSet.getMonthId());
        intent.putExtra(SeriesListActivity.BUDGET_AREA_PARAMETER, transactionSet.getSeriesValues().get(SeriesValues.BUDGET_AREA));
        TabPage.copyDemoMode(TransactionListActivity.this, intent);
        startActivity(intent);
      }
      else if (transactionSet.isAccountEntity()) {
        Intent intent = new Intent(TransactionListActivity.this, BudgetOverviewActivity.class);
        intent.putExtra(BudgetOverviewActivity.MONTH_PARAMETER, transactionSet.getMonthId());
        TabPage.copyDemoMode(TransactionListActivity.this, intent);
        startActivity(intent);
      }
      else {
        throw new InvalidState("TransactionSet should be in Series or Account mode");
      }
    }
  }
}