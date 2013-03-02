package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import com.budgetview.android.components.Header;
import com.budgetview.android.utils.TransactionSet;
import com.budgetview.shared.model.TransactionValues;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;

public class TransactionPageActivity extends FragmentActivity {

  public static final String TRANSACTION_ID_PARAMETER = "transactionPageActivity.parameters.transactionId";

  public void onCreate(Bundle state) {
    super.onCreate(state);

    setContentView(R.layout.transaction_pager);

    ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);

    Intent intent = getIntent();

    GlobRepository repository = ((App)getApplication()).getRepository();
    TransactionSet transactionSet = new TransactionSet(intent, repository);

    GlobList transactionValuesList =
      ((App)getApplication()).getRepository()
        .getAll(TransactionValues.TYPE, transactionSet.getMatcher())
        .sort(GlobComparators.descending(TransactionValues.SEQUENCE_NUMBER));
    Integer[] transactionIds = transactionValuesList.getValues(TransactionValues.ID);

    Adapter pagerAdapter = new Adapter(transactionIds, getSupportFragmentManager());
    viewPager.setAdapter(pagerAdapter);

    if (!intent.hasExtra(TRANSACTION_ID_PARAMETER)) {
      throw new InvalidParameter("Missing parameter " + TRANSACTION_ID_PARAMETER);
    }
    Integer transactionId = intent.getIntExtra(TRANSACTION_ID_PARAMETER, -1);
    int currentIndex = -1;
    for (int i = 0; i < transactionIds.length; i++) {
      if (transactionId.equals(transactionIds[i])) {
        currentIndex = i;
      }
    }
    if (currentIndex >= 0) {
      viewPager.setCurrentItem(currentIndex);
    }
    else {
      throw new InvalidParameter("Could not find transactionId " + transactionId + "in " + Arrays.toString(transactionIds));
    }

    Header header = (Header)findViewById(R.id.header);
    header.setActivity(this);
    header.setTitle(transactionSet.getSectionLabel());

    Button demoButton = (Button)findViewById(R.id.demoFooter);
    DemoActivity.install(demoButton, this);
  }

  private class Adapter extends FragmentPagerAdapter {

    private Integer[] transactionIds;
    private Fragment[] fragments;

    public Adapter(Integer[] transactionIds, FragmentManager fm) {
      super(fm);
      this.transactionIds = transactionIds;
      this.fragments = new Fragment[transactionIds.length];
    }

    public int getCount() {
      return fragments.length;
    }

    public Fragment getItem(int i) {
      if (fragments[i] == null) {
        fragments[i] = new TransactionPageFragment();
        Bundle args = new Bundle();
        args.putInt(TransactionPageFragment.TRANSACTION_ID_PARAMETER, transactionIds[i]);
        fragments[i].setArguments(args);
      }
      return fragments[i];
    }
  }

}
