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
    actions.openImport().selectFiles(path).doImport()
      .setMainAccount()
      .completeImport();

    views.selectHome();
    navigation.gotoCategorization();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "DVD", -19.0}
    });
  }

  public void testInitialImport() throws Exception {
    views.selectHome();
    versionInfo.checkNoNewVersion();
    actions.checkImportSignpostDisplayed("Click here to import your operations");
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
      .setMainAccount()
      .doImport();

    timeline.checkSelection("2008/08");

    views.selectHome();
    actions.checkImportMessage("Import your operations");
    mainAccounts.checkEstimatedPosition(125.00);

    timeline.checkYearTooltip(2008, "2008");
  }

  public void testImportChangesCategorizationFilter() throws Exception {
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
    .add("25/05/2008", "", "AUCHAN_1", -50.0)
    .check();
  }

  public void testCategorizationWidget() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectHome();
    navigation.checkCategorizationLabel("You must first load your bank operations");
    navigation.checkCategorizationGaugeHidden();

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/15", -25.0, "Auchan1")
      .addTransaction("2008/05/15", -75.0, "Auchan2")
      .addTransaction("2008/06/15", -50.0, "Auchan3")
      .addTransaction("2008/06/15", -50.0, "Auchan4")
      .load();

    timeline.checkSelection("2008/06");

    views.selectHome();
    navigation.checkCategorizationLabel("To categorize");
    navigation.checkCategorizationLevel(1.0);

    navigation.gotoCategorization();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.setNewVariable("Auchan3", "Groceries");

    views.selectHome();
    navigation.checkCategorizationLabel("To categorize");
    navigation.checkCategorizationLevel(0.5);

    navigation.gotoCategorization();
    categorization.setVariable("Auchan4", "Groceries");

    views.selectHome();
    navigation.checkCategorizationLabel("All operations are categorized");
    navigation.checkCategorizationGaugeHidden();

    timeline.selectMonth("2008/05");
    navigation.checkCategorizationLabel("To categorize");
    navigation.checkCategorizationLevel(1.0);

    navigation.gotoCategorization();
    categorization.setVariable("Auchan1", "Groceries");

    views.selectHome();
    navigation.checkCategorizationLabel("To categorize");
    navigation.checkCategorizationLevel(0.75);

    navigation.gotoCategorization();
    categorization.setVariable("Auchan2", "Groceries");

    views.selectHome();
    navigation.checkCategorizationLabel("All operations are categorized");
    navigation.checkCategorizationGaugeHidden();

    timeline.selectMonth("2008/07");
    navigation.checkCategorizationLabel("No operations to categorize");
    navigation.checkCategorizationGaugeHidden();
  }
}
