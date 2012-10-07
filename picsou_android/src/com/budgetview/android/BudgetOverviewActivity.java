package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.budgetview.shared.model.MobileModel;
import org.globsframework.model.GlobRepository;
import org.globsframework.xml.XmlGlobParser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class BudgetOverviewActivity extends FragmentActivity {

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    loadRepository();

    setContentView(R.layout.budget_overview_pager);

    ViewPager view = (ViewPager)findViewById(R.id.budgetOverviewPager);
    view.setAdapter(new BudgetOverviewPagerAdapter(getSupportFragmentManager()));
    view.setCurrentItem(1);
  }

  private void loadRepository() {
    GlobRepository repository = App.getRepository();
    InputStream inputStream = getResources().openRawResource(R.raw.globsdata);
    XmlGlobParser.parse(MobileModel.get(), repository, new InputStreamReader(inputStream), "globs");
  }

  private class BudgetOverviewPagerAdapter extends FragmentPagerAdapter {

    public static final int VIEW_COUNT = 3;
    private Fragment[] fragments = new Fragment[VIEW_COUNT];

    public BudgetOverviewPagerAdapter(FragmentManager fm) {
      super(fm);
      for (int i = 0; i < VIEW_COUNT; i++) {
        fragments[i] = new BudgetOverviewFragment(getMonthId(i));
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
          return 201206;
        case 1:
          return 201207;
        case 2:
          return 201208;
      }
      throw new IndexOutOfBoundsException("Invalid index " + i + " - should be between 0 and 2");
    }
  }
}
