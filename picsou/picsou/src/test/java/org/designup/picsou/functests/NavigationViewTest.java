package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class NavigationViewTest extends LoggedInFunctionalTestCase {
  public void testNavigation() throws Exception {
    views.selectHome();

    navigation.gotoBudget();
    views.checkBudgetSelected();

    views.selectHome();
    actions.openHelp().checkTitle("Index").close();

    String path = OfxBuilder.init(this)
      .addTransaction("2008/07/29", -19.00, "DVD")
      .save();
    actions.openImport().selectFiles(path).doImport().completeImport();

    views.selectHome();
    navigation.gotoCategorization();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "DVD", -19.0}
    });
  }

  public void testImportHighlightedWhenNoData() throws Exception {

    views.selectHome();
    versionInfo.checkNoNewVersion();
    actions.checkImportHightlighted("You must first import your bank operations");
    mainAccounts.checkNoEstimatedPosition();

    views.selectBudget();
    budgetView.getSummary()
      .checkNoEstimatedPosition();
    timeline.checkMonthTooltip("2008/08", "August 2008");

    views.selectHome();
    String file = OfxBuilder.init(this)
      .addBankAccount(12345, 456456, "120901111", 125.00, "2008/08/26")
      .addTransaction("2008/08/26", 1000, "Company")
      .save();
    actions
      .openImport()
      .selectFiles(file)
      .acceptFile()
      .selectOfxAccountBank("Autre")
      .doImport();

    timeline.checkSelection("2008/08");

    views.selectHome();
    actions.checkImportMessage("Import your operations");
    mainAccounts.checkEstimatedPosition(125.00);

    timeline.checkYearTooltip(2008, "2008");
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
