package com.budgetview.android.checkers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import com.budgetview.android.R;
import com.budgetview.android.shadow.CustomShadowTabHost;
import junit.framework.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowFragment;
import org.robolectric.shadows.ShadowTabSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AndroidTabsChecker<T extends FragmentActivity> extends AndroidChecker<T> {

  private View currentView;

  public AndroidTabsChecker(Class<T> activityClass) {
    super(activityClass);
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
    Fragment fragment = (Fragment)adapter.instantiateItem(pager, getTabHost().getCurrentTab());

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

  public void checkTabNames(String... names) {
    CustomShadowTabHost tabHost = getTabHost();
    List<String> actual = new ArrayList<String>();
    for (int i = 0; i < tabHost.getTabSpecCount(); i++) {
      ShadowTabSpec tabSpec = Robolectric.shadowOf(tabHost.getTabSpecAt(i));
      actual.add(Robolectric.shadowOf(tabSpec.getIndicatorAsView()).innerText().trim());
    }
    Assert.assertEquals(Arrays.toString(names), actual.toString());
  }

  private CustomShadowTabHost getTabHost() {
    TabHost original = (TabHost)activity.findViewById(android.R.id.tabhost);
    if (original == null) {
      Assert.fail("No tab host found");
    }
    Assert.assertNotNull(original);
    return CustomShadowTabHost.create(original);
  }

  public void checkSelectedTab(String name) {
    CustomShadowTabHost tabHost = getTabHost();
    ShadowTabSpec tabSpec = Robolectric.shadowOf(tabHost.getCurrentTabSpec());
    Assert.assertEquals(name, Robolectric.shadowOf(tabSpec.getIndicatorAsView()).innerText().trim());
  }

  public void selectTab(int index, String name) {
    CustomShadowTabHost tabHost = getTabHost();
    tabHost.setCurrentTab(index);
    ShadowTabSpec tabSpec = Robolectric.shadowOf(tabHost.getCurrentTabSpec());
    Assert.assertEquals(name, Robolectric.shadowOf(tabSpec.getIndicatorAsView()).innerText().trim());
    loadView();
  }

  protected View getCurrentTabContentView() {
    CustomShadowTabHost tabHost = getTabHost();
    ShadowTabSpec tabSpec = Robolectric.shadowOf(tabHost.getCurrentTabSpec());
    return tabSpec.getContentView();
  }

  public HeaderChecker header() {
    return new HeaderChecker(activity, R.id.header);
  }
}
