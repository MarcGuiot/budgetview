package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
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
    periods.assertSpanEquals("2008/07", "2010/06");
    transactions.setRecurring(0, "Internet");
    periods.selectLast();
    transactions.initContent()
      .dumpCode();
    views.selectHome();
    periods.selectCells("2008/07");
    monthSummary.on("july 2008")
      .checkReccuring(29.9)
      .checkPlannedRecurring(29.9);
    periods.selectCells("2008/08");
    monthSummary.on("August 2008")
      .checkReccuring(0)
      .checkPlannedRecurring(29.9);
  }
}
