package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.TimeService;
import org.globsframework.utils.Dates;

public class LicenseTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/07/01"));
    super.setUp();
  }

  public void testAcquireLicenseCreateMonths() throws Exception {
//    periods.assertEquals("2008/07");
    LicenseChecker license = new LicenseChecker(mainWindow);
    license.enterLicense("123123");
    periods.assertSpanEquals("2008/07", "2010/06");

  }
}
