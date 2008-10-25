package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class HelpTest extends LoggedInFunctionalTestCase {

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testHelpMenu() throws Exception {
    operations.openHelp().checkTitle("Index");
  }

  public void testHelpForCards() throws Exception {
    views.openHelp().checkTitle("Dashboard View").close();

    views.selectCategorization();
    views.openHelp().checkTitle("Categorization View").close();

    views.selectBudget();
    views.openHelp().checkTitle("Budget View").close();

    views.selectData();
    views.openHelp().checkTitle("Operations View").close();
  }
}