package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.components.TabPageHandler;
import com.budgetview.android.utils.TransactionSet;
import org.globsframework.model.GlobRepository;

public class TransactionListActivity extends FragmentActivity {

  public void onCreate(Bundle state) {
    super.onCreate(state);

    Intent intent = getIntent();
    GlobRepository repository = ((App)getApplication()).getRepository();
    final TransactionSet transactionSet = new TransactionSet(intent, repository);

    TabPage page = new TabPage(this,
                               transactionSet.getSectionLabel(),
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

}