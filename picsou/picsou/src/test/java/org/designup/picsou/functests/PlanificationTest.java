package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class PlanificationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/07/01"));
    super.setUp();
  }

  public void testFirstSeriesInitialization() throws Exception {
    LicenseChecker license = new LicenseChecker(mainWindow);
    license.enterLicense("");
    OfxBuilder.init(this)
      .addTransaction("2008/07/08", -29.9, "free telecom")
      .load();
    periods.assertSpanEquals("2008/07", "2010/07");
    transactions.setRecurring(0, "Internet", true);
    periods.selectCells("2008/07");
    transactions.initContent()
      .add("08/07/2008", TransactionType.PLANNED, "", "", 0.00, MasterCategory.TELECOMS)
      .add("08/07/2008", TransactionType.PRELEVEMENT, "free telecom", "", -29.90, MasterCategory.TELECOMS)
      .check();
    views.selectHome();
    periods.selectCells("2008/07");
    monthSummary.init()
      .checkRecurring(29.9)
      .checkPlannedRecurring(29.9);
    periods.selectCells("2008/08");
    transactions.initContent()
      .add("08/08/2008", TransactionType.PLANNED, "", "", -29.90, MasterCategory.TELECOMS)
      .check();
    monthSummary.init()
      .checkRecurring(0)
      .checkPlannedRecurring(29.9);
  }
}
