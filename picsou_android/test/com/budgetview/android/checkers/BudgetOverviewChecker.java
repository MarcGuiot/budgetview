package com.budgetview.android.checkers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.ViewParser;
import com.budgetview.android.checkers.utils.ViewParserCallback;
import com.budgetview.shared.utils.AmountFormat;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowFragment;

public class BudgetOverviewChecker extends AndroidChecker<BudgetOverviewActivity> {

  public BudgetOverviewChecker() {
    super(BudgetOverviewActivity.class);
  }

  protected void callOnCreate(BudgetOverviewActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public void checkTabCount(int expectedCount) {
  }

  public BudgetAreaRows initBudgetAreaContent() {
    return new BudgetAreaRows();
  }

  public class BudgetAreaRows {

    private TablePrinter expected = new TablePrinter();

    private BudgetAreaRows() {
    }

    public BudgetAreaRows add(String budgetArea, double planned, double actual) {
      expected.addRow(budgetArea, AmountFormat.DECIMAL_FORMAT.format(planned), AmountFormat.DECIMAL_FORMAT.format(actual));
      return this;
    }

    public void check() {
      ViewPager pager = (ViewPager)activity.findViewById(R.id.viewPager);
      PagerAdapter adapter = Robolectric.shadowOf(pager).getAdapter();
      Fragment fragment = (Fragment)adapter.instantiateItem(pager, 0);

      android.support.v4.app.FragmentManager fragmentManager = new FragmentActivity().getSupportFragmentManager();
      android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.add(fragment, null);
      fragmentTransaction.commit();

      ShadowFragment shadowFragment = Robolectric.shadowOf(fragment);
      shadowFragment.createView();
      View view = shadowFragment.getView();
      TablePrinter actual = new TablePrinter();
      ViewParser.parse(view, new StringParserCallback(actual));
      Assert.assertEquals(expected.toString(), actual.toString());
    }
  }

  public static class StringParserCallback implements ViewParserCallback {

    private String label;
    private String planned;
    private String actual;
    private TablePrinter tablePrinter;

    public StringParserCallback(TablePrinter tablePrinter) {
      this.tablePrinter = tablePrinter;
      resetLabels();
    }

    public void start(int id) {
    }

    public void end(int id) {
      if (id == R.id.budgetAreaBlock) {
        tablePrinter.addRow(label, planned, actual);
        resetLabels();
      }
    }

    public void processText(int id, TextView textView) {
      switch (id) {
        case R.id.budgetAreaLabel:
          this.label = textView.getText().toString();
          break;
        case R.id.budgetAreaPlanned:
          this.planned = textView.getText().toString();
          break;
        case R.id.budgetAreaActual:
          this.actual = textView.getText().toString();
          break;
        default:
          // ignore
      }
    }

    public void complete() {
    }

    private void resetLabels() {
      label = "";
      actual = "";
      planned = "";
    }
  }

}
