package com.budgetview.android.checkers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import com.budgetview.android.R;
import com.budgetview.android.TransactionPageActivity;
import com.budgetview.android.checkers.utils.ViewParser;
import com.budgetview.shared.utils.AmountFormat;
import junit.framework.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowFragment;

public class TransactionPageChecker extends AndroidChecker<TransactionPageActivity> {

  private View currentView;

  public TransactionPageChecker() {
    super(TransactionPageActivity.class);
  }

  protected void callOnCreate(TransactionPageActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  protected View getCurrentView() {
    if (currentView == null) {
      loadView();
    }
    return currentView;
  }

  protected void loadView() {
    ViewPager pager = (ViewPager)activity.findViewById(com.budgetview.android.R.id.viewPager);
    PagerAdapter adapter = Robolectric.shadowOf(pager).getAdapter();
    Fragment fragment = (Fragment)adapter.instantiateItem(pager, pager.getCurrentItem());

    FragmentActivity fragmentActivity = (FragmentActivity)activity;
    android.support.v4.app.FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.add(fragment, null);
    fragmentTransaction.commit();

    ShadowFragment shadowFragment = Robolectric.shadowOf(fragment);
    shadowFragment.setActivity(fragmentActivity);
    shadowFragment.createView();
    currentView = shadowFragment.getView();
  }

  public TransactionPageChecker checkDisplay(String label, String date, double amount) {
    checkText(R.id.transaction_label, label);
    checkText(R.id.transaction_date, date);
    checkText(R.id.transaction_amount, AmountFormat.DECIMAL_FORMAT.format(amount));
    return this;
  }

  protected void checkText(int textViewId, String expected) {
    View view = getCurrentView().findViewById(textViewId);
    if (view  == null) {
      Assert.fail("textView with id " + textViewId + " not found");
    }
    TextView textView = (TextView)view;
    Assert.assertEquals(expected, textView.getText());
  }

  public void checkUncategorized() {

  }

  public TransactionListChecker up() {
    ViewParser.click(activity.findViewById(R.id.header), R.id.header_logo);
    return new TransactionListChecker();
  }
}
