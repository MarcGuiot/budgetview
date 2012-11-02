package com.budgetview.android.components;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import com.budgetview.android.App;
import com.budgetview.android.R;
import com.budgetview.android.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class TabPage implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

  private FragmentActivity activity;
  private int currentMonthId;
  private TabPageHandler handler;
  private SortedSet<Integer> monthIds;

  private TabHost tabHost;
  private ViewPager viewPager;
  private TabPagerAdapter pagerAdapter;

  public TabPage(FragmentActivity activity, int currentMonthId, TabPageHandler handler) {
    this.activity = activity;
    this.currentMonthId = currentMonthId;
    this.handler = handler;
    this.monthIds = ((App)activity.getApplication()).getAllMonthIds();
  }

  public void initView() {
    activity.setContentView(R.layout.tab_page);

    pagerAdapter = new TabPagerAdapter(activity.getSupportFragmentManager());

    tabHost = (TabHost)activity.findViewById(android.R.id.tabhost);
    tabHost.setup();
    int selectedIndex = -1;
    int index = 0;
    for (Integer monthId : monthIds) {
      if (monthId == currentMonthId) {
        selectedIndex = index;
      }
      addTab(Text.toShortMonthString(monthId, activity.getResources()));
      index++;
    }
    tabHost.setOnTabChangedListener(this);

    viewPager = (ViewPager)activity.findViewById(R.id.viewPager);
    viewPager.setOnPageChangeListener(this);
    viewPager.setAdapter(pagerAdapter);

    if (selectedIndex >= 0) {
      viewPager.setCurrentItem(selectedIndex);
    }
  }

  private void addTab(String sep) {
    tabHost.addTab(tabHost.newTabSpec(sep)
                     .setIndicator(sep)
                     .setContent(new TabHost.TabContentFactory() {
                       public View createTabContent(String s) {
                         return new View(activity.getBaseContext());
                       }
                     }));
  }

  public void onTabChanged(String tag) {
    int pos = this.tabHost.getCurrentTab();
    this.viewPager.setCurrentItem(pos);
  }

  public void onPageScrolled(int i, float v, int i1) {
  }

  public void onPageSelected(int position) {
    this.tabHost.setCurrentTab(position);
  }

  public void onPageScrollStateChanged(int i) {
  }

  private class TabPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<Fragment>();

    public TabPagerAdapter(FragmentManager fm) {
      super(fm);

      for (Integer monthId : monthIds) {
        fragments.add(handler.createFragmentWithArgs(monthId));
      }
    }

    public int getCount() {
      return fragments.size();
    }

    public Fragment getItem(int i) {
      return fragments.get(i);
    }
  }
}
