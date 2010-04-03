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

    views.selectHome();
    navigation.gotoCategorization();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "DVD", -19.0}
    });
  }

  public void testNavigationChangeCategorizationFilter() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/25", -50.0, "Auchan_1")
      .load();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "Auchan_2")
      .load();

    views.selectCategorization();
    categorization.showLastImportedFileOnly();

    views.selectData();
    timeline.selectMonth("2008/05");
    transactions.categorize("AUCHAN_1");
    categorization.initContent()
    .add("25/05/2008", "AUCHAN_1", -50.0)
    .check();
  }
}
