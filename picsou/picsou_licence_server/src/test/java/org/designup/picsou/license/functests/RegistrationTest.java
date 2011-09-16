package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.LicenseActivationChecker;
import org.designup.picsou.functests.checkers.SlaValidationDialogChecker;
import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.ConnectedTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

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
