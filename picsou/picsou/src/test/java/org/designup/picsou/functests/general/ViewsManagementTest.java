package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class ViewsManagementTest extends LoggedInFunctionalTestCase {

  public void testHomePage() throws Exception {
    views.selectHome();
    views.checkHomeSelected();
    transactions.checkVisible(false);
  }

  protected void selectInitialView() {
    // default view
  }

  public void testDefaultState() throws Exception {

    views.checkHomeSelected();

    views.selectData();
    transactions.checkVisible(true);
  }

  public void testBackForward() throws Exception {
    views.checkHomeSelected();
    views.checkBackForward(false, false);

    views.selectData();
    views.checkBackForward(true, false);

    views.selectBudget();
    views.checkBackForward(true, false);

    views.back();
    views.checkDataSelected();
    views.checkBackForward(true, true);

    views.forward();
    views.checkBudgetSelected();
    views.checkBackForward(true, false);

    views.back();
    views.checkDataSelected();
    views.checkBackForward(true, true);

    views.back();
    views.checkHomeSelected();
    views.checkBackForward(false, true);

    views.forward();
    views.checkDataSelected();
    views.checkBackForward(true, true);

    views.selectCategorization();
    views.checkBackForward(true, false);

    views.back();
    views.checkDataSelected();
    views.checkBackForward(true, true);

    views.back();
    views.checkHomeSelected();
    views.checkBackForward(false, true);
  }

  public void testTooltips() throws Exception {
    views.checkAllTooltipsPresent();
  }
}
