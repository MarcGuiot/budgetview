package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.LicenseActivationChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.SlaValidationDialogChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.LicenseTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class RegistrationTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;
  private int previousRetry;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    previousRetry = ConfigService.RETRY_PERIOD;
    ConfigService.RETRY_PERIOD = 500;
    licenseServer.init();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    ConfigService.RETRY_PERIOD = previousRetry;
    window.dispose();
    picsouApplication.shutdown();
    window = null;
    picsouApplication = null;
  }

  public void testNoServerAccessForRegistration() throws Exception {
    startPicsou();

    LicenseActivationChecker license = LicenseActivationChecker.open(window);
    license.checkConnectionNotAvailable();
    startServers();
    license.checkConnectionIsAvailable();
  }

  private void startPicsou() {
    Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk = new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);
    triggerSlaOk.run();
    window = triggerSlaOk.getMainWindow();
  }

}
