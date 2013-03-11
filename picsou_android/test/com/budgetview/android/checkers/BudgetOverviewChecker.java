package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.TextView;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.android.checkers.utils.ViewParser;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.utils.TablePrinter;

public class BudgetOverviewChecker extends AndroidTabsChecker<BudgetOverviewActivity> {

  public BudgetOverviewChecker() {
    super(BudgetOverviewActivity.class);
  }

  protected void callOnCreate(BudgetOverviewActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public SeriesListChecker edit(String budgetAreaName) {
    ViewParser.clickBlockWithTextView(getCurrentView(), R.id.budgetAreaBlock, R.id.budgetAreaLabel, budgetAreaName);
    return new SeriesListChecker();
  }

  public TransactionListChecker editUncategorized() {
    ViewParser.clickBlockWithTextView(getCurrentView(), R.id.budgetAreaBlock, R.id.budgetAreaLabel, "Uncategorized");
    return new TransactionListChecker();
  }

  public TransactionListChecker editAccount(String accountName) {
    ViewParser.clickBlockWithTextView(getCurrentView(), R.id.accountBlock, R.id.accountLabel, accountName);
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

  public AccountRows initAccountContent() {
    return new AccountRows();
  }

  public class AccountRows extends TableChecker {
    public AccountRows() {
      super("Account", getCurrentView());
    }

    public AccountRows add(String accountName, double position, String date) {
      expected.addRow(accountName, AmountFormat.DECIMAL_FORMAT.format(position), date);
      return this;
    }

    protected BlockParser getParser(TablePrinter actual) {
      return new AccountBlockParser(actual);
    }
  }

  public static class AccountBlockParser implements BlockParser {

    private String accountName;
    private String position;
    private String positionDate;
    private TablePrinter tablePrinter;

    public AccountBlockParser(TablePrinter tablePrinter) {
      this.tablePrinter = tablePrinter;
      resetLabels();
    }

    public void start(int id) {
    }

    public void end(int id) {
      if (id == R.id.accountBlock) {
        tablePrinter.addRow(accountName, position, positionDate);
        resetLabels();
      }
    }

    public void processText(int id, TextView textView) {
      switch (id) {
        case R.id.accountLabel:
          this.accountName = textView.getText().toString();
          break;
        case R.id.accountPosition:
          this.position = textView.getText().toString();
          break;
        case R.id.accountPositionDate:
          this.positionDate = textView.getText().toString();
          break;
        default:
          // ignore
      }
    }

    public void complete() {
    }

    private void resetLabels() {
      accountName = "";
      position = "";
      positionDate = "";
    }
  }
}
