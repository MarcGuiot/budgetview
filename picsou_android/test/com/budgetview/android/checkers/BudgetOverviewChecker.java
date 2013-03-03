package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.TextView;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.android.checkers.utils.Views;
import com.budgetview.shared.utils.AmountFormat;
import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TablePrinter;

public class BudgetOverviewChecker extends AndroidTabsChecker<BudgetOverviewActivity> {

  public BudgetOverviewChecker() {
    super(BudgetOverviewActivity.class);
  }

  protected void callOnCreate(BudgetOverviewActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public SeriesListChecker edit(String budgetAreaName) {
    Views.clickBlockWithTextView(getCurrentView(), R.id.budgetAreaBlock, R.id.budgetAreaLabel, budgetAreaName);
    return new SeriesListChecker();
  }

  public TransactionListChecker editUncategorized() {
    Views.clickBlockWithTextView(getCurrentView(), R.id.budgetAreaBlock, R.id.budgetAreaLabel, "Uncategorized");
    return new TransactionListChecker();
  }

  public BudgetAreaRows initContent() {
    return new BudgetAreaRows();
  }

  public class BudgetAreaRows extends TableChecker {
    public BudgetAreaRows() {
      super("BudgetOverview", getCurrentView());
    }

    public BudgetAreaRows add(String budgetArea, double planned, double actual) {
      expected.addRow(budgetArea, AmountFormat.DECIMAL_FORMAT.format(planned), AmountFormat.DECIMAL_FORMAT.format(actual));
      return this;
    }

    protected BlockParser getParser(TablePrinter actual) {
      return new BudgetAreaBlockParser(actual);
    }
  }

  public static class BudgetAreaBlockParser implements BlockParser {

    private String label;
    private String planned;
    private String actual;
    private TablePrinter tablePrinter;

    public BudgetAreaBlockParser(TablePrinter tablePrinter) {
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
