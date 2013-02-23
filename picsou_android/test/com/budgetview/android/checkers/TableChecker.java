package com.budgetview.android.checkers;

import android.view.View;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.checkers.utils.BlockParser;
import com.budgetview.android.checkers.utils.Views;
import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TablePrinter;

public abstract class TableChecker {

  private final String title;
  private final View view;
  protected TablePrinter expected = new TablePrinter();

  public TableChecker(String title, View view) {
    this.title = title;
    this.view = view;
  }

  public void check() {
    TablePrinter actual = new TablePrinter();
    Views.parse(view, getParser(actual));
    String actualText = actual.toString();
    if (Strings.isNullOrEmpty(actualText)) {
      Assert.fail(title + " is unexpectedly empty");
    }
    Assert.assertEquals("Unexpected " + title + " content", expected.toString(), actual.toString());
  }

  protected abstract BlockParser getParser(TablePrinter actual);
}
