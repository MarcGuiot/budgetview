package org.designup.picsoulicence.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.MonthChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsoulicence.LicenceTestCase;
import org.designup.picsoulicence.model.License;
import org.designup.picsoulicence.model.MailError;
import org.designup.picsoulicence.model.RepoInfo;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
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
    connection.createTable(License.TYPE);
    connection.createTable(RepoInfo.TYPE);
    connection.createTable(MailError.TYPE);
    startPicsou();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
  }

  private void startPicsou() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

  public void testConnectAtStartup() throws Exception {
    SqlConnection connection = getSqlConnection();
    String mail = "alfred@free.fr";
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, mail)
      .set(License.ACTIVATION_CODE, "ZE23E-342SC")
      .getRequest()
      .run();
    connection.commit();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense(mail, "ZE23E-342SC");
    Glob license = getLicense(connection, mail, License.SIGNATURE, null);
    assertTrue(license.get(License.SIGNATURE).length > 1);
    assertEquals(1L, license.get(License.LAST_COUNT).longValue());
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");

    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    license = getLicense(connection, mail, License.LAST_COUNT, 2);
    assertEquals(2L, license.get(License.LAST_COUNT).longValue());
  }

  private Glob getLicense(SqlConnection connection, String mail, Field field, Object expected) throws InterruptedException {
    long end = System.currentTimeMillis() + 2000;
    Glob license = null;
    while (end > System.currentTimeMillis()) {
      license = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
        .selectAll()
        .getQuery().executeUnique();
      connection.commit();
      Object actual = license.getValue(field);
      if (actual != null && (expected == null || actual.equals(expected))) {
        break;
      }
      Thread.sleep(50);
    }
    return license;
  }
}
