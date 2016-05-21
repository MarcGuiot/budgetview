package com.budgetview.license.functests;

import com.budgetview.functests.checkers.license.LicenseActivationChecker;
import com.budgetview.gui.config.ConfigService;
import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.gui.time.TimeService;
import com.budgetview.license.ConnectedTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Window;

public class RegistrationTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private int previousRetry;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    previousRetry = ConfigService.RETRY_PERIOD;
    ConfigService.RETRY_PERIOD = 500;
    licenseServer.init();
    application = new ApplicationChecker();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    ConfigService.RETRY_PERIOD = previousRetry;
    application.dispose();
    application = null;
  }

  public void testNoServerAccessForRegistration() throws Exception {
    Window window = application.start();

    LicenseActivationChecker license = LicenseActivationChecker.open(window);
    license.checkConnectionNotAvailable();
    startServersWithoutLicence();
    license.checkConnectionIsAvailable();
  }
}
