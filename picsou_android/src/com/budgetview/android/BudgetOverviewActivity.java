package com.budgetview.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;

public class BudgetOverviewActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

  private TabHost tabHost;
  private ViewPager viewPager;

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    App app = (App)getApplication();
    if (app.isLoaded()) {
      showContent(savedInstanceState);
    }
    else {
      setContentView(R.layout.loading_page);
      DataLoader loader = new DataLoader(this) {
        protected void onLoadFinished() {
          showContent(savedInstanceState);
        }
      };
      loader.load();
    }
  }

  private void showContent(Bundle savedInstanceState) {
    setContentView(R.layout.budget_overview_pager);

    initialiseTabHost(savedInstanceState);

    viewPager = (ViewPager)findViewById(R.id.budgetOverviewPager);
    if (viewPager == null) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("No view found")
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
          }
        });
      AlertDialog alert = builder.create();
      alert.show();
    }

    viewPager.setOnPageChangeListener(this);

    FragmentManager supportFragmentManager = getSupportFragmentManager();
    viewPager.setAdapter(new BudgetOverviewPagerAdapter(supportFragmentManager));
    viewPager.setCurrentItem(1);
  }

  private void initialiseTabHost(Bundle args) {
    tabHost = (TabHost)findViewById(android.R.id.tabhost);
    tabHost.setup();
    addTab("Sep");
    addTab("Oct");
    addTab("Nov");
    tabHost.setOnTabChangedListener(this);
  }

  private void addTab(String sep) {
    tabHost.addTab(tabHost.newTabSpec(sep)
                     .setIndicator(sep)
                     .setContent(new TabHost.TabContentFactory() {
                       public View createTabContent(String s) {
                         return new View(getBaseContext());
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

  private class BudgetOverviewPagerAdapter extends FragmentPagerAdapter {

    public static final int VIEW_COUNT = 3;
    private Fragment[] fragments = new Fragment[VIEW_COUNT];

    public BudgetOverviewPagerAdapter(FragmentManager fm) {
      super(fm);
      for (int i = 0; i < VIEW_COUNT; i++) {
        fragments[i] = new BudgetOverviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BudgetOverviewFragment.BUDGET_OVERVIEW_MONTH, getMonthId(i));
        fragments[i].setArguments(bundle);
      }
    }

    public int getCount() {
      return VIEW_COUNT;
    }

    public Fragment getItem(int i) {
      return fragments[i];
    }

    private int getMonthId(int i) {
      switch (i) {
        case 0:
          return 201209;
        case 1:
          return 201210;
        case 2:
          return 201211;
      }
      throw new IndexOutOfBoundsException("Invalid index " + i + " - should be between 0 and 2");
    }
  }
}
