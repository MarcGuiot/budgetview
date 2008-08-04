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
import org.globsframework.model.EmptyGlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public class LicenseTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;
  private static final String MAIL = "alfred@free.fr";

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
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
    if (window != null) {
      window.dispose();
    }
    window = null;
    picsouApplication.shutdown();
    picsouApplication = null;
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
    checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    String mail = "alfred@free.fr";
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, mail)
      .set(License.ACTIVATION_CODE, "1234")
      .getRequest()
      .run();
    connection.commit();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense(mail, "1234");
    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 1L);
    assertEquals(1L, license.get(License.ACCESS_COUNT).longValue());
    assertTrue(license.get(License.SIGNATURE).length > 1);
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");

    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");
    license = getLicense(connection, mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
  }

  public void testMultipleAnonymousConnect() throws Exception {
    SqlConnection connection = getSqlConnection();
    checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    checkRepoIdIsUpdated(connection, 2L, null);
    loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");
  }

  public void testResendActivationKeyIfCountDecrease() throws Exception {
    String repoId = startFirstPicsou();
    window.dispose();
    restartPicsouToIncrementCount();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "tmp/otherprevayler");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startPicsou();
    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    login.skipImport();
    checkRepoIdIsUpdated(getSqlConnection(), 1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));
    LicenseChecker license = new LicenseChecker(window);
    license.enterLicense(MAIL, "1234");
    String mailcontent = checkReceive(MAIL);
    assertTrue(mailcontent, mailcontent.contains(": "));
    int startCode = mailcontent.indexOf(": ") + 2;
    String newActivationCode = mailcontent.substring(startCode, startCode + 4);
    window.dispose();
    checkPreviousVersionValidity("2010/07");
    activateNewLicenseInNewVersion(newActivationCode);
    checkPreviousVersionValidity("2008/08");
  }

  private void restartPicsouToIncrementCount() {
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    LoginChecker login = new LoginChecker(window);
    login.logUser("user", "passw@rd");
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
    window.dispose();
  }

  private void activateNewLicenseInNewVersion(String code) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "tmp/otherprevayler");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    LoginChecker login = new LoginChecker(window);
    login.logUser("user", "passw@rd");
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertEquals("2008/07");
    LicenseChecker license = new LicenseChecker(window);
    license.enterLicense(MAIL, code);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
    window.dispose();
  }

  private void checkPreviousVersionValidity(String lastMonth) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", lastMonth);
    window.dispose();
  }

  private String startFirstPicsou() throws InterruptedException {
    SqlConnection connection = getSqlConnection();
    String repoId = checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    loginChecker.skipImport();
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, MAIL)
      .set(License.ACTIVATION_CODE, "1234")
      .getRequest()
      .run();
    connection.commit();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense(MAIL, "1234");
    Glob license = getLicense(connection, MAIL, License.ACCESS_COUNT, 1L);
    assertEquals(1L, license.get(License.ACCESS_COUNT).longValue());
    connection.commitAndClose();
    return repoId;
  }

  private String checkRepoIdIsUpdated(SqlConnection connection, long repoCount, Constraint constraint) throws InterruptedException {
    Glob repoInfo = getGlob(connection, RepoInfo.COUNT, repoCount, constraint);
    Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
    assertTrue(Dates.isNear(new Date(), target, 2000));
    assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
    return repoInfo.get(RepoInfo.REPO_ID);
  }

  private Glob getLicense(SqlConnection connection, String mail, Field field, Object expected) throws InterruptedException {
    return getGlob(connection, field, expected, Constraints.equal(License.MAIL, mail));
  }

  private Glob getGlob(SqlConnection connection, Field field, Object expected,
                       Constraint constraint) throws InterruptedException {
    long end = System.currentTimeMillis() + 3000;
    GlobList glob = new EmptyGlobList();
    while (end > System.currentTimeMillis()) {
      glob = connection.getQueryBuilder(field.getGlobType(), constraint)
        .selectAll()
        .getQuery().executeAsGlobs();
      connection.commit();
      if (glob.size() == 1) {
        Object actual = glob.get(0).getValue(field);
        if (actual != null && (expected == null || actual.equals(expected))) {
          break;
        }
      }
      Thread.sleep(50);
    }
    assertEquals(1, glob.size());
    return glob.get(0);
  }
}
