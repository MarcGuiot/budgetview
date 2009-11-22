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
  private LoginChecker login;
  private static final String MAIL = "alfred@free.fr";
  private static final String SECOND_PATH = "tmp/otherprevayler";

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    startServers();
    startApplication();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "true");
    if (window != null) {
      window.dispose();
      window = null;
    }
    picsouApplication.shutdown();
    picsouApplication = null;
    login = null;
  }

  private void startApplication() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
    login = new LoginChecker(window);
  }


  public void testConnectAtStartup() throws Exception {
    DbChecker dbChecker = new DbChecker();
    String mail = "alfred@free.fr";
    dbChecker.registerMail(mail,  "1234");
    register(dbChecker, mail, "1234");
    checkValidLicense(false);

    window.dispose();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication();
    login.logExistingUser("user", "passw@rd");

    Glob license = dbChecker.getLicense(mail, License.ACCESS_COUNT, 2L);
    assertEquals(2L, license.get(License.ACCESS_COUNT).longValue());
    checkValidLicense(false);
  }

  public void testMultipleAnonymousConnect() throws Exception {
    DbChecker dbChecker = new DbChecker();
    dbChecker.checkRepoIdIsUpdated(1L, null);
    login.logNewUser("user", "passw@rd");
    window.dispose();

    startApplication();
    dbChecker.checkRepoIdIsUpdated(2L, null);
    login.logExistingUser("user", "passw@rd");
    window.dispose();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkLicenseExpired();
  }

  public void testResendsActivationKeyIfCountDecreases() throws Exception {
    String repoId = loggingAndRegisterFirstPicsou();
    window.dispose();
    restartAppAndLogAndDispose();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    DbChecker dbChecker = new DbChecker();
    dbChecker.checkRepoIdIsUpdated(1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));
    checkDaysLeftMessage();

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed a mail was sent at " + MAIL);
    checkDaysLeftMessage();
    String mailcontent = checkReceivedMail(MAIL);
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
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkKilled();
    window.dispose();
  }

  public void testRegisterAndReRegisterWithBadEmail() throws Exception {
    DbChecker dbChecker = new DbChecker();
    String mail = "alfred@free.fr";
    dbChecker.registerMail(mail, "1234");
    register(dbChecker, mail, "1234");
    checkValidLicense(false);

    window.dispose();
    startApplication();
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, "titi@foo.org", "4321", "Unknown mail adress");

    dbChecker.checkLicenseCount(mail, 2);
    checkValidLicense(false);
    window.dispose();
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(false);
  }

  public void testRegisterAndReRegisterWithBadActivationCode() throws Exception {
    DbChecker dbChecker = new DbChecker();
    String mail = "alfred@free.fr";
    dbChecker.registerMail(mail, "1234");
    register(dbChecker, mail, "1234");
    checkValidLicense(false);

    window.dispose();
    startApplication();
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    dbChecker.checkLicenseCount(mail, 2);
    checkValidLicense(false);
    window.dispose();
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(false);
  }

  public void testRegisterAndReRegisterWithBadActivationCodeWithoutRestart() throws Exception {
    DbChecker dbChecker = new DbChecker();
    String mail = "alfred@free.fr";
    dbChecker.registerMail(mail, "1234");
    register(dbChecker, mail, "1234");
    checkValidLicense(false);
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    checkValidLicense(false);
    window.dispose();
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(false);
  }

  public void testUnknownEmailAddress() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker activationChecker = LicenseActivationChecker.open(window);
    activationChecker
      .enterLicenseAndValidate("titi@foo.org", "az")
      .checkErrorMessage("Unknown mail adress")
      .checkActivationCodeIsEmptyAndMailIs("titi@foo.org");
    checkMessage("Unknown mail adress");
    DbChecker dbChecker = new DbChecker();
    dbChecker.registerMail("toto@zer", "1234");
    activationChecker
      .enterLicense("toto@zer", "1234")
      .validate()
      .checkClosed();
    checkValidLicense(false);
  }

  public void testRegistrationWithBadKey() throws Exception {
    DbChecker dbChecker = new DbChecker();
    dbChecker.registerMail("titi@foo.org", "1234");
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.open(window)
      .enterLicenseAndValidate("titi@foo.org", "az")
      .checkErrorMessage("Activation failed")
      .cancel();
    window.dispose();
    checkMessage("31 days left for trying CashPilot");
    checkMessage("Activation code not valid. You can ");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkLicenseExpired();
  }

  public void testStartRegistrationAndStopServer() throws Exception {
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker license = LicenseActivationChecker.open(window)
      .enterLicense("titi@foo.org", "az");

    stop();
    license.validate();
    license.checkErrorMessage("Activation failed")
      .cancel();

    checkMessage("31 days left for trying CashPilot");
    checkMessage("Can not connect to remote server");

    window.dispose();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkLicenseExpired();
  }

  public void testEmptyActivationCode() throws Exception {
    login.logNewUser("user", "passw@rd");

    DbChecker dbChecker = new DbChecker();
    dbChecker.registerMail("titi@foo.org", "4321");
    LicenseActivationChecker license = LicenseActivationChecker.open(window);
    license.enterLicense("titi@foo.org", "az");
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.enterLicense("titi@foo.org", "");
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.cancel();

    checkMessage("31 days left for trying CashPilot");
    checkMessage("Activation code not valid. You can ask for ");

    window.dispose();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkLicenseExpired();
  }

  private void checkMessage(final String messageTxt) {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains(messageTxt));
  }

  public void testRegisterAndReRegisterToOtherFailedAndSendAMail() throws Exception {
    DbChecker dbChecker = new DbChecker();
    String mail = "alfred@free.fr";
    dbChecker.registerMail(mail, "1234");
    register(dbChecker, mail, "1234");
    checkValidLicense(false);

    window.dispose();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    restartAppAndLogAndDispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    TimeService.setCurrentDate(Dates.parse("2008/07/10"));
    startApplication();
    login.logNewUser("user", "passw@rd");
    window.dispose();

    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication();
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, mail, "1234", "Activation failed a mail was sent at alfred@free.fr");
    checkReceivedMail(mail);
    checkWithMailKilled();
  }

  public void testMailSentLater() throws Exception {
    loggingAndRegisterFirstPicsou();
    window.dispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication();
    login.logNewUser("user", "passw@rd");

    mailServer.stop();
    mailThread.join();
    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed a mail was sent at alfred@free.fr");
    boolean received = false;
    try {
      checkReceivedMail(MAIL);
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
    checkReceivedMail(MAIL);
  }

  public void testLicenseActivatesKey() throws Exception {
    loggingAndRegisterFirstPicsou();
    window.dispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication();
    login.logNewUser("user", "passw@rd");
    window.dispose();

    TimeService.setCurrentDate(Dates.parseMonth("2008/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication();
    login.logExistingUser("user", "passw@rd");

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed a mail was sent at alfred@free.fr");
    String messageCode = checkReceivedMail(MAIL);
    String newCode = messageCode.substring(messageCode.length() - 5, messageCode.length() - 1).trim();
    LicenseActivationChecker.enterLicense(window, "alfred@free.fr", newCode);
    window.dispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    startApplication();
    login.logExistingUser("user", "passw@rd");

    LicenseMessageChecker licenseMessageChecker = new LicenseMessageChecker(window);
    LicenseExpirationChecker expirationChecker = licenseMessageChecker.clickNewLicense();
    expirationChecker
      .checkMail("alfred@free.fr")
      .sendKey()
      .close();

    messageCode = checkReceivedMail(MAIL);
    newCode = messageCode.substring(messageCode.length() - 5, messageCode.length() - 1).trim();
    window.dispose();
    SqlConnection connection = getSqlConnection();
    Glob glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, MAIL))
      .selectAll()
      .getQuery().executeUnique();
    assertEquals(newCode, glob.get(License.ACTIVATION_CODE));
  }

  public void testTrialVersionIsOver() throws Exception {
    login.logNewUser("user", "passw@rd");
    window.dispose();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication();
    login.logExistingUser("user", "passw@rd");
    LicenseMessageChecker messageChecker = new LicenseMessageChecker(window);
    messageChecker.checkMessage("Your free trial period is over. You can buy");
  }

  public void testActivationFailDuringTrial() throws Exception {
    DbChecker dbChecker = new DbChecker();
    dbChecker.registerMail(MAIL, "4321");
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed");
    checkActivationFailed();
    checkDaysLeftMessage();
  }

  private void checkLicenseExpired() {
    OperationChecker operations = new OperationChecker(window);
    Window dialog = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    LicenseActivationChecker licenseActivation = new LicenseActivationChecker(dialog);
    licenseActivation.cancel();
    checkMessageOver();
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

  private void restartAppAndLogAndDispose() {
    startApplication();
    login.logExistingUser("user", "passw@rd");
    window.dispose();
  }

  private void activateNewLicenseInNewVersion(String code) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(true);
    LicenseActivationChecker.enterLicense(window, MAIL, code);
    checkValidLicense(false);
    window.dispose();
  }

  private void checkVersionValidity(final boolean anonymous, final String pathToData) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApplication();
    login.logExistingUser("user", "passw@rd");
    checkValidLicense(anonymous);
    window.dispose();
  }

  private String loggingAndRegisterFirstPicsou() throws InterruptedException {
    DbChecker dbChecker = new DbChecker();
    String repoId = dbChecker.checkRepoIdIsUpdated(1L, null);

    login.logNewUser("user", "passw@rd");
    dbChecker.registerMail(MAIL, "1234");
    LicenseActivationChecker.enterLicense(window, MAIL, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();

    dbChecker.checkLicenseCount(MAIL, 1);
    return repoId;
  }


  private void checkDaysLeftMessage() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("days left"));
  }

  private void checkMessageOver() {
    checkMessage("Your free trial period is over");
  }

  private void checkWithMailKilled() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("Activation failed a mail was sent at alfred@free.fr"));
  }

  private void checkActivationFailed() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("Activation code not valid"));
    assertTrue(message.textContains("You can ask"));
  }

  private void checkKilled() {
    TextBox message = window.getTextBox("licenseMessage");
    assertThat(message.isVisible());
    assertTrue(message.textContains("You are not allowed to import data anymore"));
  }


  private void register(DbChecker dbChecker, String mail, final String code) throws InterruptedException {
    dbChecker.checkRepoIdIsUpdated(1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.enterLicense(window, mail, code);
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();
    dbChecker.checkLicenseCount(mail, 1);
  }

  class DbChecker {
    SqlConnection connection;

    DbChecker() {
      connection = getSqlConnection();
    }

    public void registerMail(String mail, String code){
      connection.getCreateBuilder(License.TYPE)
        .set(License.MAIL, mail)
        .set(License.ACTIVATION_CODE, code)
        .getRequest()
        .run();
      connection.commit();

    }

    private Glob getGlob(Field field, Object expected,
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

    public Glob getLicense(String mail, Field field, Object expected) throws InterruptedException {
      return getGlob(field, expected, Constraints.equal(License.MAIL, mail));
    }

    public String checkRepoIdIsUpdated(long repoCount, Constraint constraint) throws InterruptedException {
      Glob repoInfo = getGlob(RepoInfo.COUNT, repoCount, constraint);
      Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
      assertTrue(Dates.isNear(new Date(), target, 10000));
      assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
      return repoInfo.get(RepoInfo.REPO_ID);
    }

    public void checkLicenseCount(String mail, long count) throws InterruptedException {
      Glob license = getLicense(mail, License.ACCESS_COUNT, count);
      assertEquals(count, license.get(License.ACCESS_COUNT).longValue());
      assertTrue(license.get(License.SIGNATURE).length > 1);
    }
  }
}
