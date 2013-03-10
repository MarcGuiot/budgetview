package com.budgetview.android.checkers;

import android.view.View;
import com.budgetview.android.AccountSummaryBlockView;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.ViewParser;
import junit.framework.Assert;

public class AccountSummaryBlockChecker {

  public static void check(View view, int blockId, double position, String date) {
    AccountSummaryBlockView block = (AccountSummaryBlockView)view.findViewById(blockId);
    if (block == null) {
      Assert.fail("No AccountSummaryBlockView found with id " + blockId + " in:\n " + ViewParser.toString(view));
    }
    ViewParser.checkText(block, R.id.positionAmount, position);
    ViewParser.checkText(block, R.id.positionDate, date);
  }
}
