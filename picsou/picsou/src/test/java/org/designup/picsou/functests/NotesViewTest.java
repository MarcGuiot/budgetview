package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class NotesViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentDate("2008/08/27");
  }

  public void testNoData() throws Exception {
    views.selectHome();
    versionInfo.checkNoNewVersion();
    notes.checkNoDataMessage();
    notes.openImportHelp().checkContains("import").close();

    mainAccounts.checkNoEstimatedPosition();
    timeline.checkMonthTooltip("2008/08", "August 2008");

    String file = OfxBuilder.init(this)
      .addBankAccount(12345, 456456, "120901111", 125.00, "2008/08/26")
      .addTransaction("2008/08/26", 1000, "Company")
      .save();

    notes
      .openImport()
      .selectFiles(file)
      .acceptFile()
      .selectOfxAccountBank("Autre")
      .doImport();

    timeline.checkSelection("2008/08");

    notes.checkNoSeriesMessage();
    mainAccounts.checkEstimatedPosition(125.00);
    timeline.checkYearTooltip(2008, "2008");
  }

  public void testNoSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .addTransaction("2008/05/15", -100, "FNAC")
      .load();

    timeline.selectMonth("2008/06");

    views.selectHome();
    notes
      .checkNoSeriesMessage()
      .openSeriesWizard()
      .validate();

    views.checkCategorizationSelected();
    categorization
      .checkTable(new Object[][]{
        {"15/06/2008", "", "Company", 1000.0},
        {"15/05/2008", "", "FNAC", -100.0},
      })
      .setNewIncome("Company", "Salary");

    views.selectHome();
    notes.checkNoHelpMessageDisplayed();
  }

  public void testNotes() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .addTransaction("2008/05/15", -100, "FNAC")
      .load();

    views.selectHome();
    notes
      .checkNoSeriesMessage()
      .openSeriesWizard()
      .validate();

    views.selectHome();
    notes.checkText("Click here to enter your own notes");

    notes.setText("One note");
    notes.checkText("One note");

  }

  public void testMonthTooltipWithNoPositionAvailable() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/15")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .load();

    operations.openPreferences().setFutureMonthsCount(1).validate();

    timeline.checkMonthTooltip("2008/07", 1000, 100);
    timeline.checkMonthTooltip("2008/08", 0, 100);
  }
}
