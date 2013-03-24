package com.budgetview.android.checkers;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import com.budgetview.android.R;
import com.budgetview.android.checkers.utils.ViewParser;
import junit.framework.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowImageView;

public class HeaderChecker {
  private View header;

  HeaderChecker(Activity activity, int id) {
    this.header = activity.findViewById(id);
    if (header == null) {
      Assert.fail("No header view found with id " + id + " found in activity:\n" + activity);
    }
  }

  public BudgetOverviewChecker refresh() {
    clickRefresh();
    return new BudgetOverviewChecker();
  }

  private void clickRefresh() {
    View refreshView = header.findViewById(R.id.header_refresh);
    if (refreshView == null) {
      Assert.fail("No refresh button found in \n" + ViewParser.toString(header));
    }
    if (refreshView.getVisibility() != View.VISIBLE) {
      Assert.fail("Refresh button hidden in \n" + ViewParser.toString(header));
    }
    ShadowImageView refresh = Robolectric.shadowOf((ImageView)refreshView);
    refresh.performClick();
  }

  public void refreshAndCheckError(String message) {
    clickRefresh();
    ShadowAlertDialog dialog = Robolectric.shadowOf(ShadowAlertDialog.getLatestAlertDialog());
    Assert.assertEquals(message, dialog.getMessage());
  }
}
