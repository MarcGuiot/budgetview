package org.designup.picsou.license.functests;

import com.dumbster.smtp.SimpleSmtpServer;
import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.license.LicenseTestCase;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.RepoInfo;
import org.globsframework.metamodel.Field;
import org.globsframework.model.EmptyGlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Dates;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public class LicenseTest extends LicenseTestCase {
  private PicsouApplication picsouApplication;
  private Window window;
  private static final String MAIL = "alfred@free.fr";
  private static final String SECOND_PATH = "tmp/otherprevayler";


  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    startServers();
    startApp();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
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

  private void startApp() {
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
    startApp();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logExistingUser("user", "passw@rd");

    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    checkValidLicense(false);
  }

  public void testMultipleAnonymousConnect() throws Exception {
    SqlConnection connection = getSqlConnection();
    checkRepoIdIsUpdated(connection, 1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    window.dispose();
    startApp();
    checkRepoIdIsUpdated(connection, 2L, null);
    loginChecker = new LoginChecker(window);
    loginChecker.logExistingUser("user", "passw@rd");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    OperationChecker operations = new OperationChecker(window);
    operations.openPreferences().setFutureMonthsCount(3);
    checkLicenseExpiration();
  }

  public void testResendsActivationKeyIfCountDecreases() throws Exception {
    String repoId = loggingAndRegisterFirstPicsou();
    window.dispose();
    restartPicsouAndLogAndDispose();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApp();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    checkRepoIdIsUpdated(getSqlConnection(), 1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));
    checkStillDayWithAppMessage();

    LicenseChecker.enterBadLicense(window, MAIL, "1234", "Activation failed a mail was sent at alfred@free.fr");
    checkStillDayWithAppMessage();
    String mailcontent = checkMailReceive(MAIL);
    assertTrue(mailcontent, mailcontent.contains("Your new activation code"));

    int startCode = mailcontent.indexOf("is ") + 3;
    String newActivationCode = mailcontent.substring(startCode, startCode + 4);
    window.dispose();

    checkVersionValidity(false, PATH_TO_DATA);
    activateNewLicenseInNewVersion(newActivationCode);
    checkVersionValidity(false, SECOND_PATH);
    TimeService.setCurrentDate(Dates.parse("2008/12/10"));
    checkVersionValidity(false, SECOND_PATH);
    TimeService.setCurrentDate(Dates.parse("2009/05/10"));
    checkVersionValidity(false, SECOND_PATH);
    checkKilledVersion(PATH_TO_DATA);
    stop();
    TimeService.setCurrentDate(Dates.parse("2009/07/10"));
    checkVersionValidity(false, SECOND_PATH);
  }

  private void checkKilledVersion(String pathToData) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApp();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logExistingUser("user", "passw@rd");
    checkMessageOver();
    window.dispose();
  }

  public void testRegisterAndBadlyReRegister() throws Exception {
    SqlConnection connection = getSqlConnection();
    String mail = "alfred@free.fr";
    register(connection, mail);

    window.dispose();
    startApp();

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logExistingUser("user", "passw@rd");
    LicenseChecker.enterBadLicense(window, "titi@foo.org", "4321", "Activation failed");

    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    checkValidLicense(false);
  }

  public void testRegistrationWithBadKey() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker.open(window)
      .enterLicenseAndValidate("titi@foo.org", "az")
      .checkErrorMessage("Activation failed")
      .cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenseExpiration();
  }

  public void testStartRegistrationAndStopServer() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker license = LicenseChecker.open(window)
      .enterLicense("titi@foo.org", "az");

    stop();
    license.validate();
    license.checkErrorMessage("Activation failed")
      .cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenseExpiration();
  }

  public void testEmptyActivationCode() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseChecker license = LicenseChecker.open(window)
      .enterLicense("titi@foo.org", "az");
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.enterLicense("titi@foo.org", "");
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.cancel();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    checkLicenseExpiration();
  }

  public void testRegisterAndReRegisterToOtherFailedAndSendAMail() throws Exception {
    SqlConnection connection = getSqlConnection();
    String mail = "alfred@free.fr";
    register(connection, mail);
    window.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    restartPicsouAndLogAndDispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    TimeService.setCurrentDate(Dates.parse("2008/07/10"));
    startApp();
    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    window.dispose();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApp();
    login = new LoginChecker(window);
    login.logExistingUser("user", "passw@rd");
    LicenseChecker.enterBadLicense(window, mail, "1234", "Activation failed a mail was sent at alfred@free.fr");
    checkLicenseExpiration();
    checkMailReceive(mail);
    checkKilled();
  }

  public void testMailSentLater() throws Exception {
    loggingAndRegisterFirstPicsou();
    window.dispose();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApp();

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");

    mailServer.stop();
    mailThread.join();
    LicenseChecker.enterBadLicense(window, MAIL, "1234", "Activation failed a mail was sent at alfred@free.fr");
    boolean received = false;
    try {
      checkMailReceive(MAIL);
      received = true;
    }
    catch (AssertionFailedError e) {
    }
    assertFalse(received);
    mailServer = new SimpleSmtpServer(2500);
    mailThread = new Thread() {
      public void run() {
        mailServer.run();
      }
    };
    mailThread.setDaemon(true);
    mailThread.start();
    checkMailReceive(MAIL);
  }

  private void checkLicenseExpiration() {
    OperationChecker operation = new OperationChecker(window);
    LicenseExpirationChecker licenseExpiration = new LicenseExpirationChecker(operation.getImportTrigger());
    licenseExpiration.close();
  }

  private void checkValidLicense(final boolean anonymous) {
    OperationChecker operation = new OperationChecker(window);
    Window dialog = WindowInterceptor.getModalDialog(operation.getImportTrigger());
    ImportChecker importChecker = new ImportChecker(dialog);
    importChecker.close();
    if (!anonymous) {
      assertFalse(window.getTextBox("licenseMessage").isVisible());
    }
    else {
      assertTrue(window.getTextBox("licenseMessage").isVisible());
    }
  }

  private void restartPicsouAndLogAndDispose() {
    startApp();
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("user", "passw@rd");
    window.dispose();
  }

  private void activateNewLicenseInNewVersion(String code) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApp();
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(true);
    LicenseChecker.enterLicense(window, MAIL, code);
    checkValidLicense(false);
    window.dispose();
  }

  private void checkVersionValidity(final boolean anonymous, final String pathToData) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApp();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logExistingUser("user", "passw@rd");
    checkValidLicense(anonymous);
    window.dispose();
  }

  private String loggingAndRegisterFirstPicsou() throws InterruptedException {
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
    LicenseChecker.enterLicense(window, MAIL, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();

    Glob license = getLicense(connection, MAIL, License.ACCESS_COUNT, 1L);
    assertEquals(1L, license.get(License.ACCESS_COUNT).longValue());
    connection.commitAndClose();
    return repoId;
  }

  private String checkRepoIdIsUpdated(SqlConnection connection, long repoCount, Constraint constraint) throws InterruptedException {
    Glob repoInfo = getGlob(connection, RepoInfo.COUNT, repoCount, constraint);
    Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
    assertTrue(Dates.isNear(new Date(), target, 10000));
    assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
    return repoInfo.get(RepoInfo.REPO_ID);
  }

  private void checkStillDayWithAppMessage() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("Still "));
  }

  private void checkMessageOver() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("Your free trial period is over"));
  }

  private void checkKilled() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("Activation failed a mail was sent at alfred@free.fr"));
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
    operation.openPreferences().setFutureMonthsCount(24).validate();
    Glob license = getLicense(connection, mail, License.ACCESS_COUNT, 1L);
    assertEquals(1L, license.get(License.ACCESS_COUNT).longValue());
    assertTrue(license.get(License.SIGNATURE).length > 1);
    checkValidLicense(false);
  }

}
