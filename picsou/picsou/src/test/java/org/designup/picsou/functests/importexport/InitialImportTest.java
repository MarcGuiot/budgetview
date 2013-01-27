package org.designup.picsou.functests.importexport;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class InitialImportTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    super.setUp();
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testInitialImport() throws Exception {
    newVersion.checkNoNewVersionShown();

    views.checkDataSignpostVisible();
    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
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
    importPanel.openImport()
      .selectFiles(file)
      .acceptFile()
      .selectBank("Other")
      .setMainAccount()
      .doImport()
      .completeLastStep();

    timeline.checkSelection("2008/08");

    views.selectHome();
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
}
