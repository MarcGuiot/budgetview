package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;
import com.budgetview.shared.model.AccountEntity;
import com.budgetview.shared.model.SeriesEntity;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidParameter;

public class TransactionListActivity extends FragmentActivity {

  public static String MONTH_PARAMETER = "transactionListActivity.parameters.month";
  public static String SERIES_ENTITY_PARAMETER = "transactionListActivity.parameters.series";
  public static String ACCOUNT_PARAMETER = "transactionListActivity.parameters.account";

  private Integer accountId;
  private Integer seriesEntityId;

  protected void onCreate(Bundle state) {
    super.onCreate(state);

    Intent intent = getIntent();
    int monthId = intent.getIntExtra(MONTH_PARAMETER, -1);

    GlobRepository repository = ((App)getApplication()).getRepository();
    String sectionLabel = "";
    if (intent.hasExtra(SERIES_ENTITY_PARAMETER)) {
      seriesEntityId = intent.getIntExtra(SERIES_ENTITY_PARAMETER, -1);
      Glob seriesEntity = repository.get(Key.create(SeriesEntity.TYPE, seriesEntityId));
      sectionLabel = seriesEntity.get(SeriesEntity.NAME);
    }
    else if (intent.hasExtra(ACCOUNT_PARAMETER)) {
      accountId = intent.getIntExtra(ACCOUNT_PARAMETER, -1);
      Glob accountEntity = repository.get(Key.create(AccountEntity.TYPE, accountId));
      sectionLabel = accountEntity.get(AccountEntity.LABEL);
    }
    else {
      throw new InvalidParameter("Missing filtering parameter");
    }

    TabPage page = new TabPage(this, sectionLabel,
                               monthId, new TabPageHandler() {
      public Fragment createFragmentWithArgs(int monthId) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TransactionListFragment.MONTH_PARAMETER, monthId);
        if (accountId != null) {
          bundle.putInt(TransactionListFragment.ACCOUNT_PARAMETER, accountId);
        }
        if (seriesEntityId != null) {
          bundle.putInt(TransactionListFragment.SERIES_VALUES_PARAMETER, seriesEntityId);
        }
        fragment.setArguments(bundle);
        return fragment;
      }
    }

    );
    page.initView();
  }

}