package org.designup.picsoulicence.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.MonthChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsoulicence.LicenseTestCase;
import org.designup.picsoulicence.model.License;
import org.designup.picsoulicence.model.MailError;
import org.designup.picsoulicence.model.RepoInfo;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public class LicenseTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    start();
    SqlConnection connection = getSqlConnection();
    connection.createTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE);
    connection.emptyTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE);
    connection.commitAndClose();
    startPicsou();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    stop();
    window = null;
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
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    checkRepoIdIsUpdated(connection, 1L);
    String mail = "alfred@free.fr";
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, mail)
      .set(License.ACTIVATION_CODE, "1234")
      .getRequest()
      .run();
    connection.commit();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense(mail, "1234");
    Glob license = getLicense(connection, mail, License.LAST_COUNT, 1L);
    assertEquals(1L, license.get(License.LAST_COUNT).longValue());
    assertTrue(license.get(License.SIGNATURE).length > 1);
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");

    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    license = getLicense(connection, mail, License.LAST_COUNT, 2L);
    assertEquals(2L, license.get(License.LAST_COUNT).longValue());
  }

  public void testMultipleAnonymousConnect() throws Exception {
    SqlConnection connection = getSqlConnection();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    checkRepoIdIsUpdated(connection, 1L);
    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    checkRepoIdIsUpdated(connection, 2L);
  }

  private void checkRepoIdIsUpdated(SqlConnection connection, long repoCount) {
    GlobList globList = connection.getQueryBuilder(RepoInfo.TYPE)
      .selectAll()
      .getQuery().executeAsGlobs();
    assertEquals(1, globList.size());
    Glob repoInfo = globList.get(0);
    Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
    assertTrue(Dates.isNear(new Date(), target, 1000));
    assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
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
      Thread.sleep(10);
    }
    return license;
  }
}
