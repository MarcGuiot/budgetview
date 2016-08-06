package com.budgetview.server.license.functests;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.functests.checkers.license.LicenseActivationChecker;
import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.server.license.ConnectedTestCase;
import com.budgetview.shared.license.LicenseConstants;
import org.globsframework.utils.Dates;
import org.uispec4j.Window;

public class RegistrationTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private int previousRetry;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(Application.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    previousRetry = LicenseConstants.RETRY_PERIOD;
    LicenseConstants.RETRY_PERIOD = 500;
    licenseServer.init();
    application = new ApplicationChecker();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    LicenseConstants.RETRY_PERIOD = previousRetry;
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
