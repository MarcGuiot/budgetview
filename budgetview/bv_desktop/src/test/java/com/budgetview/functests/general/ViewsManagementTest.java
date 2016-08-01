package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import org.junit.Test;

public class ViewsManagementTest extends LoggedInFunctionalTestCase {

  @Test
  public void testHomePage() throws Exception {
    views.selectHome();
    views.checkHomeSelected();
    transactions.checkVisible(false);
  }

  protected void selectInitialView() {
    // default view
  }

  @Test
  public void testDefaultState() throws Exception {

    views.checkHomeSelected();

    views.selectData();
    transactions.checkVisible(true);
  }

  @Test
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

  @Test
  public void testTooltips() throws Exception {
    addOns.activateProjects();
    addOns.activateAnalysis();
    views.checkAllTooltipsPresent();
  }
}
