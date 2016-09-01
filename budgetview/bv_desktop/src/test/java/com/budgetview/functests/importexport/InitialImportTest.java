package com.budgetview.functests.importexport;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import org.junit.Test;

public class InitialImportTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    super.setUp();
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  @Test
  public void testInitialImport() throws Exception {
    newVersion.checkNoNewVersionShown();

    views.selectBudget();
    timeline.checkMonthTooltip("2008/08", "August 2008");

    views.selectHome();
    String file = OfxBuilder.init(this)
      .addBankAccount(12345, 456456, "120901111", 125.00, "2008/08/26")
      .addTransaction("2008/08/26", 1000, "Company")
      .save();
    importPanel.openImport()
      .selectFiles(file)
      .importFileAndPreview()
      .selectBank("Other")
      .setMainAccount()
      .importAccountAndComplete();

    timeline.checkSelection("2008/08");

    views.selectHome();
    mainAccounts.checkEndOfMonthPosition("Account n. 120901111", 125.00);

    timeline.checkYearTooltip(2008, "2008");
  }

  @Test
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
