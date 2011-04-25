package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class HelpTest extends LoggedInFunctionalTestCase {

  public void testHelpMenu() throws Exception {
    operations.openHelp().checkTitle("Index").close();

    operations.checkGotoSupport("http://support.mybudgetview.fr");
  }

  public void testHelpForCards() throws Exception {
    views.selectHome();
    operations.openHelp("Dashboard View").checkTitle("Dashboard View").close();

    views.selectCategorization();
    operations.openHelp("Categorization View").checkTitle("Categorization View").close();

    views.selectBudget();
    operations.openHelp("Budget View").checkTitle("Budget View").close();

    views.selectData();
    operations.openHelp("Operations View").checkTitle("Operations View").close();
  }
}