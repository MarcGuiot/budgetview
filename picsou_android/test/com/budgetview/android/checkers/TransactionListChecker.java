package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.TextView;
import com.budgetview.android.R;
import com.budgetview.android.TransactionListActivity;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.utils.TablePrinter;

public class TransactionListChecker extends AndroidTabsChecker<TransactionListActivity> {

  public TransactionListChecker() {
    super(TransactionListActivity.class);
  }

  protected void callOnCreate(TransactionListActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public TransactionRows initContent() {
    return new TransactionRows();
  }

  public class TransactionRows extends TableChecker {

    public TransactionRows() {
      super("TransactionList", getCurrentView());
    }

    public TransactionRows add(String date, String label, double amount) {
      expected.addRow(date, label, AmountFormat.DECIMAL_FORMAT.format(amount));
      return this;
    }

    protected BlockParser getParser(TablePrinter actual) {
      return new TransactionBlockParser(actual);
    }
  }

  public static class TransactionBlockParser implements BlockParser {

    private String date;
    private String label;
    private String amount;
    private TablePrinter tablePrinter;

    public TransactionBlockParser(TablePrinter tablePrinter) {
      this.tablePrinter = tablePrinter;
      resetLabels();
    }

    public void start(int id) {
    }

    public void end(int id) {
      if (id == R.id.transactionBlock) {
        tablePrinter.addRow(date, label, amount);
        resetLabels();
      }
    }

    public void processText(int id, TextView textView) {
      switch (id) {
        case R.id.transactionDate:
          this.date = textView.getText().toString();
          break;
        case R.id.transactionLabel:
          this.label = textView.getText().toString();
          break;
        case R.id.transactionAmount:
          this.amount = textView.getText().toString();
          break;
        default:
          // ignore
      }
    }

    public void complete() {
    }

    private void resetLabels() {
      date = "";
      label = "";
      amount = "";
    }
  }

}
