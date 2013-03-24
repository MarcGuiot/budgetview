package com.budgetview.android.checkers;

import android.view.View;
import com.budgetview.android.AmountsBlockView;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.ViewParser;
import junit.framework.Assert;

public class AmountBlockChecker {
  public static void check(View view, int blockId, String planned, String actual) {
    AmountsBlockView block = (AmountsBlockView)view.findViewById(blockId);
    if (block == null) {
      Assert.fail("No AmountBlockView found with id " + blockId + " in:\n " + ViewParser.toString(view));
    }
    ViewParser.checkText(block, R.id.actualAmount, actual);
    ViewParser.checkText(block, R.id.plannedAmount, planned);
  }
}
