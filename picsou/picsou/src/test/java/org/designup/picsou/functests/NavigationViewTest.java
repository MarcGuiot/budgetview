package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class NavigationViewTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    views.selectHome();

    navigation.gotoBudget();
    views.checkBudgetSelected();

    views.selectHome();
    navigation.openHelp().checkTitle("Index").close();

    String path = OfxBuilder.init(this)
      .addTransaction("2008/07/29", -19.00, "DVD")
      .save();
    navigation.openImport().selectFiles(path).doImport().completeImport();
    navigation.gotoCategorization();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "DVD", -19.0}
    });
  }
}
