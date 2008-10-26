package org.designup.picsou.licence.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.licence.LicenseTestCase;
import org.designup.picsou.licence.model.License;
import org.designup.picsou.licence.model.RepoInfo;
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

import java.util.Calendar;
import java.util.Date;

public class LicenseTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;
  private static final String MAIL = "alfred@free.fr";
  private static final String SECOND_PATH = "tmp/otherprevayler";

  protected void setUp() throws Exception {
    super.setUp();
    Calendar month = Calendar.getInstance();
    month.set(2000, 1, 1);
    TimeService.setCurrentDate(month.getTime());
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    start();
    startPicsou();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "true");
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
    String mail = "alfred@free.fr";
    register(connection, mail);
    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");

    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
  }

  private void register(SqlConnection connection, String mail) throws InterruptedException {
    checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, mail)
      .set(License.ACTIVATION_CODE, "1234")
      .getRequest()
      .run();
    connection.commit();
    LicenseChecker.enterLicense(window, mail, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.getPreferences().changeFutureMonth(24).validate();
    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 1L);
    assertEquals(1L, license.get(License.ACCESS_COUNT).longValue());
    assertTrue(license.get(License.SIGNATURE).length > 1);
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
  }

  public void testMultipleAnonymousConnect() throws Exception {
    SqlConnection connection = getSqlConnection();
    checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    checkRepoIdIsUpdated(connection, 2L, null);
    loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenceExpiration();
  }

  public void testResendActivationKeyIfCountDecrease() throws Exception {
    String repoId = startFirstPicsou();
    window.dispose();
    restartPicsouToIncrementCount();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startPicsou();

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    checkRepoIdIsUpdated(getSqlConnection(), 1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));

    LicenseChecker.enterBadLicense(window, MAIL, "1234", 24);
    String mailcontent = checkReceive(MAIL);
    assertTrue(mailcontent, mailcontent.contains(": "));

    int startCode = mailcontent.indexOf(": ") + 2;
    String newActivationCode = mailcontent.substring(startCode, startCode + 4);
    window.dispose();

    checkPreviousVersionValidity("2010/07");
    activateNewLicenseInNewVersion(newActivationCode);
    checkPreviousVersionValidity("2010/07");
  }

  public void testRegisterAndBadlyReRegister() throws Exception {
    SqlConnection connection = getSqlConnection();
    String mail = "alfred@free.fr";
    register(connection, mail);

    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "passw@rd");
    LicenseChecker.enterBadLicense(window, "titi@foo.org", "4321", 24);

    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
    OperationChecker operation = new OperationChecker(window);
    operation.openImportDialog()
      .close();
  }

  public void testRegistrationWithBadKey() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker.open(window)
      .enterLicenseAndValidate("titi@foo.org", "az", 24)
      .checkErrorMessage("Activation failed")
      .cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenceExpiration();
  }

  private void checkLicenceExpiration() {
    OperationChecker operation = new OperationChecker(window);
    LicenceExpirationChecker licenceExpiration = new LicenceExpirationChecker(operation.getImportTrigger());
    licenceExpiration.close();
  }

  public void testStartRegistrationAndStopServer() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker license = LicenseChecker.open(window)
      .enterLicense("titi@foo.org", "az", 24);

    stop();
    license.validate();
    license.checkErrorMessage("Activation failed")
      .cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenceExpiration();
  }

  public void testEmptyActivationCode() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker license = LicenseChecker.open(window)
      .enterLicense("titi@foo.org", "az", 24);
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.enterLicense("titi@foo.org", "", 24);
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenceExpiration();
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
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startPicsou();
    LoginChecker login = new LoginChecker(window);
    login.logUser("user", "passw@rd");
    MonthChecker monthChecker = new MonthChecker(window);
    monthChecker.assertSpanEquals("2008/07", "2010/07");
    LicenseChecker license = new LicenseChecker(window);
    license.enterLicense(window, MAIL, code);
    OperationChecker operation = new OperationChecker(window);
    operation.getPreferences().changeFutureMonth(24).validate();

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
    if (lastMonth.equals("2008/07")) {
      monthChecker.assertDisplays("2008/07");
    }
    else {
      monthChecker.assertSpanEquals("2008/07", lastMonth);
    }
    window.dispose();
  }

  private String startFirstPicsou() throws InterruptedException {
    SqlConnection connection = getSqlConnection();
    String repoId = checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, MAIL)
      .set(License.ACTIVATION_CODE, "1234")
      .getRequest()
      .run();
    connection.commit();
    LicenseChecker checker = new LicenseChecker(window);
    checker.enterLicense(window, MAIL, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.getPreferences().changeFutureMonth(24).validate();

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
