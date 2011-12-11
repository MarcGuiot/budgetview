package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.utils.Lang;

public class HelpTest extends LoggedInFunctionalTestCase {

  public void testHelpMenu() throws Exception {
    operations.openHelp().checkTitle("Index").close();

    operations.checkGotoSupport(Lang.get("site.support.url"));
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

  public void testAccessToWebsite() throws Exception {
    String url = Lang.get("site.url");
    operations.checkGotoWebsite(url);
    feedbackView.checkWebsiteLinksTo(url);
  }
}