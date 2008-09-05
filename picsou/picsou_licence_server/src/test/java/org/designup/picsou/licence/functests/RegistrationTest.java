package org.designup.picsou.licence.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.licence.LicenseTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class RegistrationTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
  }

  public void testNoServerAccessForRegistration() throws Exception {
    startPicsou();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    LicenseChecker license = LicenseChecker.open(window);
    license.checkConnectionNotAvailable();
    start();
    Thread.sleep(2000);
    license.checkConnectionIsAvailable();
  }

  private void startPicsou() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

}
