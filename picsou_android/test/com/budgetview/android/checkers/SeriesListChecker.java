package com.budgetview.android.checkers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.budgetview.android.R;
import com.budgetview.android.SeriesListActivity;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.android.checkers.utils.Views;
import com.budgetview.shared.utils.AmountFormat;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;

public class SeriesListChecker extends AndroidChecker<SeriesListActivity> {

  public SeriesListChecker() {
    super(SeriesListActivity.class);
  }

  protected void callOnCreate(SeriesListActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public SeriesRows initContent() {
    return new SeriesRows();
  }

  public class SeriesRows {

    private TablePrinter expected = new TablePrinter();

    public SeriesRows add(String seriesName, double planned, double actual) {
      expected.addRow(seriesName, AmountFormat.DECIMAL_FORMAT.format(planned), AmountFormat.DECIMAL_FORMAT.format(actual));
      return this;
    }

    public void check() {
      View view = getCurrentPagerFragmentView(R.id.viewPager);
      TablePrinter actual = new TablePrinter();
      Views.parse(view, new SeriesBlockParser(actual));
      Assert.assertEquals("", expected.toString(), actual.toString());
    }
  }

  public static class SeriesBlockParser implements BlockParser {

    private String label;
    private String planned;
    private String actual;
    private TablePrinter tablePrinter;

    public SeriesBlockParser(TablePrinter tablePrinter) {
      this.tablePrinter = tablePrinter;
      resetLabels();
    }

    public void start(int id) {
    }

    public void end(int id) {
      if (id == R.id.seriesBlock) {
        tablePrinter.addRow(label, planned, actual);
        resetLabels();
      }
    }

    public void processText(int id, TextView textView) {
      switch (id) {
        case R.id.seriesLabel:
          this.label = textView.getText().toString();
          break;
        case R.id.seriesPlanned:
          this.planned = textView.getText().toString();
          break;
        case R.id.seriesActual:
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
