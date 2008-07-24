package org.designup.picsoulicence.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.MonthChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsoulicence.LicenceTestCase;
import org.designup.picsoulicence.model.Licence;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class LicenceTest extends LicenceTestCase {
  private PicsouApplication picsouApplication;
  private Window window;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    start();
    SqlConnection connection = getSqlConnection();
    connection.createTable(Licence.TYPE);
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

  public void testConnectAtStartup() throws Exception {
    SqlConnection connection = getSqlConnection();
    connection.getCreateBuilder(Licence.TYPE)
      .set(Licence.MAIL, "alfred@free.fr")
      .set(Licence.ACTIVATION_CODE, "ZE23E-342SC")
      .getRequest()
      .run();
    connection.commit();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense("alfred@free.fr", "ZE23E-342SC");
    Thread.sleep(1000);
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
  }
}
