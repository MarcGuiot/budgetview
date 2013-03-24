package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.TextView;
import com.budgetview.android.R;
import com.budgetview.android.SeriesListActivity;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.android.checkers.utils.ViewParser;
import org.globsframework.utils.TablePrinter;

public class SeriesListChecker extends AndroidTabsChecker<SeriesListActivity> {

  public SeriesListChecker() {
    super(SeriesListActivity.class);
  }

  protected void callOnCreate(SeriesListActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public SeriesRows initContent() {
    return new SeriesRows();
  }

  public TransactionListChecker edit(String seriesName) {
    ViewParser.clickBlockWithTextView(getCurrentView(), R.id.seriesBlock, R.id.seriesLabel, seriesName);
    return new TransactionListChecker();
  }

  public class SeriesRows extends TableChecker {

    public SeriesRows() {
      super("SeriesList", getCurrentView());
    }

    public SeriesRows add(String seriesName, String planned, String actual) {
      expected.addRow(seriesName, planned, actual);
      return this;
    }

    protected BlockParser getParser(TablePrinter actual) {
      return new SeriesBlockParser(actual);
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
