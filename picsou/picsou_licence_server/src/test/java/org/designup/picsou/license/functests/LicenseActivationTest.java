package org.designup.picsou.license.functests;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.license.LicenseActivationChecker;
import org.designup.picsou.functests.checkers.license.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.DuplicateLine;
import org.designup.picsou.license.checkers.DbChecker;
import org.designup.picsou.license.checkers.Email;
import org.designup.picsou.license.mail.Mailbox;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.RepoInfo;
import org.designup.picsou.license.servlet.RegisterServlet;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseActivationTest extends ConnectedTestCase {
  public static final String ACTIVATION_CODE = "Activation code : <b>";
  private PicsouApplication picsouApplication;
  private Window window;
  private LoginChecker login;
  private LicenseChecker license;

  private static final String MAIL = "alfred@free.fr";
  private static final String SECOND_PATH = "tmp/otherprevayler";
  private static final String THIRD_PATH = "tmp/otherprevayler_2";
  private static final String FOURTH_PATH = "tmp/otherprevayler_3";

  protected void setUp() throws Exception {
    LoggedInFunctionalTestCase.resetWindow();
    System.setProperty("budgetview.log.sout", "true");
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    startServers();
    startApplication(true);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "true");
    if (window != null) {
      try {
        exit();
      }
      catch (Throwable e) {
      }
    }
    picsouApplication.shutdown();
    picsouApplication = null;
    login = null;
    window = null;
  }

  private void startApplication(boolean isFirst) {
    final ApplicationChecker application = new ApplicationChecker();
    if (isFirst) {
      window = application.start();
    }
    else {
      window = WindowInterceptor.run(new Trigger() {
        public void run() throws Exception {
          picsouApplication = new PicsouApplication();
          picsouApplication.run();
        }
      });
    }
    if (isFirst) {
      new OperationChecker(window).logout();
      picsouApplication = application.getApplication();
    }
    login = new LoginChecker(window);
    license = new LicenseChecker(window);
  }

  public void testConnectAtStartup() throws Exception {

    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense();

    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd");

    Glob license = db.getLicense(email, License.ACCESS_COUNT, 3L);
    assertEquals(3L, license.get(License.ACCESS_COUNT).longValue());
    checkValidLicense();
  }

  public void testMultipleAnonymousConnect() throws Exception {
    db.checkRepoIdIsUpdated(1L, null);
    login.logNewUser("user", "passw@rd");
    exit();

    startApplication(false);
    db.checkRepoIdIsUpdated(2L, null);
    login.logExistingUser("user", "passw@rd");
    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkUserNotRegistered();
  }

  public void testResendsActivationKeyIfCountDecreases() throws Exception {
    String repoId = loginAndRegisterFirstPicsou();

    exit();
    restartAppAndLogAndDispose();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");

    startApplication(true);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");

    db.checkRepoIdIsUpdated(1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));
    license.checkInfoMessageHidden();

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed. An email was sent at " + MAIL + " with further information.");
    license.checkInfoMessage("Activation failed. An email was sent");
    Email email = mailServer.checkReceivedMail(MAIL);
    email.checkContains("To prevent anyone else from using your code");
    String emailcontent = email.getContent();
    int startCode = emailcontent.indexOf(ACTIVATION_CODE) + ACTIVATION_CODE.length();
    String newActivationCode = emailcontent.substring(startCode, startCode + 4);
    Integer.parseInt(newActivationCode);
    exit();

    TimeService.setCurrentDate(Dates.parse("2008/08/01"));

    checkVersionValidity(false, PATH_TO_DATA);
    activateNewLicenseInNewVersion(newActivationCode);
    checkVersionValidity(false, SECOND_PATH);
    TimeService.setCurrentDate(Dates.parse("2008/12/10"));
    checkVersionValidity(false, SECOND_PATH);
    TimeService.setCurrentDate(Dates.parse("2009/05/10"));
    checkVersionValidity(false, SECOND_PATH);
    checkKilledVersion(PATH_TO_DATA);
    stopServers();
    TimeService.setCurrentDate(Dates.parse("2009/07/10"));
    checkVersionValidity(false, SECOND_PATH);
  }

  private void checkKilledVersion(String pathToData) throws Exception {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    license.checkKilled();
    exit();
  }

  public void testRegisterAndReRegisterWithBadEmail() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense();

    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, "titi@foo.org", "4321", "Unknown email address");

    db.checkLicenseCount(email, 3);
    checkValidLicense();
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
  }

  public void testRegisterAndReRegisterWithBadActivationCode() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense();

    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    db.checkLicenseCount(email, 3);
    checkValidLicense();
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
  }

  public void testRegisterAndReRegisterWithBadActivationCodeWithoutRestart() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense();
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    checkValidLicense();
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
  }

  public void testUnknownEmailAddress() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker activation = LicenseActivationChecker.open(window);
    activation
      .enterLicenseAndActivate("titi@foo.org", "az")
      .checkErrorMessage("Unknown email address")
      .checkActivationCodeIsEmptyAndMailIs("titi@foo.org");
    checkMessage("Unknown email address");

    db.registerMail("toto@zer", "1234");
    activation
      .enterLicenseAndActivate("toto@zer", "1234")
      .checkActivationCompleted()
      .complete();
    checkValidLicense();
  }

  public void testRegistrationWithBadKey() throws Exception {
    db.registerMail("titi@foo.org", "1234");
    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker.open(window)
      .enterLicenseAndActivate("titi@foo.org", "az")
      .checkErrorMessage("Activation failed")
      .close();
    checkMessage("This activation code is not valid. You can request");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    exit();
    startApplication(false);
    this.login.logExistingUser("user", "passw@rd");
    checkUserNotRegistered();
  }

  public void testStartRegistrationAndStopServer() throws Exception {
    login.logNewUser("user", "passw@rd");

    LicenseActivationChecker license =
      LicenseActivationChecker.open(window)
        .enterLicense("titi@foo.org", "az");

    stopServers();

    license.validateWithError();
    license.checkErrorMessage("You must be connected to the Internet")
      .close();

    checkMessage("Cannot connect to remote server");

    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkUserNotRegistered();
  }

  public void testEmptyActivationCode() throws Exception {
    login.logNewUser("user", "passw@rd");

    db.registerMail("titi@foo.org", "4321");
    LicenseActivationChecker license = LicenseActivationChecker.open(window);
    license.checkMsgToReceiveNewCode();
    license.enterLicense("titi@foo.org", "az");
    license.checkMsgSendNewCode();
    license.validateWithError();
    license.checkErrorMessage("Activation failed");
    license.enterLicense("titi@foo.org", "");
    license.checkMsgSendNewCode();
    license.validateWithError();
    license.checkErrorMessage("Activation failed");
    license.close();

    checkMessage("This activation code is not valid. You can request");

    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkUserNotRegistered();
  }

  private void checkMessage(final String messageText) {
    license.checkInfoMessage(messageText);
  }

  public void testRegisterAndReRegisterToOtherFailedAndSendAMail() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense();

    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    restartAppAndLogAndDispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    TimeService.setCurrentDate(Dates.parse("2008/07/10"));
    startApplication(true);
    login.logNewUser("user", "passw@rd");
    exit();

    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, email, "1234",
                                             "Activation failed. An email was sent at " + MAIL + " with further information.");
    mailServer.checkReceivedMail(email);
    license.checkMailKilled(MAIL);
  }

  public void testSendCodeFromActivation() throws Exception {
    db.registerMail("alfred@free.fr", "1234");
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker activation = LicenseActivationChecker.open(window);
    activation.enterLicense("alfred@free.fr", "");
    activation.askForCode();
    String newCode = checkMailAndExtractCode();
    assertFalse(newCode.equals("1234"));
    activation.close();
  }

  public void testMailSentLater() throws Exception {
    loginAndRegisterFirstPicsou();
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    login.logNewUser("user", "passw@rd");

    mailServer.stop();
    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234",
                                             "Activation failed. An email was sent at " + MAIL + " with further information.");
    boolean received = false;
    try {
      mailServer.checkReceivedMail(MAIL);
      received = true;
    }
    catch (AssertionFailedError e) {
    }
    assertFalse(received);

    mailServer.start();
    mailServer.checkReceivedMail(MAIL);
  }

  public void testLicenseActivatesKey() throws Exception {
    loginAndRegisterFirstPicsou();
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    login.logNewUser("user", "passw@rd");
    exit();

    TimeService.setCurrentDate(Dates.parseMonth("2008/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd");

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234",
                                             "Activation failed. An email was sent at " + MAIL + " with further information.");
    String newCode = checkMailAndExtractCode();
    LicenseActivationChecker.enterLicense(window, "alfred@free.fr", newCode);
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");

    LicenseChecker license = new LicenseChecker(window);
    license.requestNewLicense()
      .checkMail("alfred@free.fr")
      .sendKey()
      .close();

    String newEmail = mailServer.checkReceivedMail(MAIL).getContent();
    exit();
    SqlConnection connection = db.getConnection();
    Glob glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, MAIL))
      .selectAll()
      .getQuery().executeUnique();
    String activationCode = glob.get(License.ACTIVATION_CODE);
    if (activationCode.length() < 4) {
      Assert.fail("Invalid activation code found in DB: " + activationCode);
    }
    if (!newEmail.contains(activationCode)) {
      Assert.fail("New email does not contain activation code '" + activationCode + "' : \n" + newEmail);
    }
  }

  private String checkMailAndExtractCode() throws InterruptedException {
    String email = mailServer.checkReceivedMail(MAIL).getContent();
    int start = email.indexOf(ACTIVATION_CODE);
    if (start == -1) {
      fail(ACTIVATION_CODE + " not found in " + email);
    }
    int index = start + ACTIVATION_CODE.length();
    return email.substring(index, index + 4);
  }

  public void testMultipleActivation() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    db.registerMail(MAIL, "4321");
    db.registerMail(MAIL, "4321");
    db.registerMail(MAIL, "4321");

    register(MAIL, "4321");
    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    restartAppAndLogAndDispose();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    register(MAIL, "4321");
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, THIRD_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    register(MAIL, "4321");
    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, FOURTH_PATH);
    startApplication(true);
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker.enterBadLicense(window, MAIL, "4321",
                                             "Activation failed. An email was sent at alfred@free.fr with further information.");
    String newCode = checkMailAndExtractCode();

    LicenseActivationChecker.enterLicense(window, MAIL, newCode);
    checkValidLicense();
    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    license.checkUserIsRegistered();
    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
  }

  private static final String OTHERMAIL_FREE_FR = "othermail@free.Fr";

  public void testUpgrade() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    db.registerMail(MAIL, "4321");
    register(MAIL, "4321");

    SqlConnection connection = db.getConnection();
    connection.getCreateBuilder(License.TYPE)
      .set(License.MAIL, OTHERMAIL_FREE_FR)
      .set(License.ACTIVATION_CODE, "1111")
      .getRequest()
      .run();
    connection.commit();

    DuplicateLine.complete(db.getConnection());
    GlobList list = connection.getQueryBuilder(License.TYPE)
      .selectAll()
      .getQuery().executeAsGlobs();

    assertEquals(6, list.size());

    list = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, MAIL))
      .selectAll()
      .getQuery().executeAsGlobs().sort(RegisterServlet.COMPARATOR);
    assertEquals(3, list.size());
    Glob l11 = list.get(0);
    Glob l12 = list.get(1);
    Glob l13 = list.get(2);
    assertEquals(l11.get(License.ACTIVATION_CODE), "4321");
    assertNull(l11.get(License.REPO_ID));
    assertEquals(l12.get(License.ACTIVATION_CODE), "4321");
    assertNull(l12.get(License.REPO_ID));
    assertNull(l13.get(License.ACTIVATION_CODE));
    assertNotNull(l13.get(License.REPO_ID));

    list = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, OTHERMAIL_FREE_FR))
      .selectAll()
      .getQuery().executeAsGlobs().sort(RegisterServlet.COMPARATOR);
    assertEquals(3, list.size());

    l11 = list.get(0);
    l12 = list.get(1);
    l13 = list.get(2);
    assertEquals(l11.get(License.ACTIVATION_CODE), "1111");
    assertNull(l11.get(License.REPO_ID));
    assertEquals(l12.get(License.ACTIVATION_CODE), "1111");
    assertNull(l12.get(License.REPO_ID));
    assertEquals(l13.get(License.ACTIVATION_CODE), "1111");
    assertNull(l13.get(License.REPO_ID));
  }

  public void testRegisterTwice() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    db.registerMail(MAIL, "4321");
    db.registerMail(MAIL, "4321");

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    register(MAIL, "4321");
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker.enterLicense(window, MAIL, "4321");
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    LicenseActivationChecker activation = LicenseActivationChecker.open(window);
    activation.enterLicense(MAIL, "");
    activation.askForCode();
    String mail = mailServer.checkReceivedMail(MAIL).getContent();
    int length = mail.indexOf("Activation code : <b>");
    String newCode = mail.substring(length + "Activation code : <b>".length(), length + "Activation code : <b>".length() + 4).trim();
    LicenseActivationChecker.enterLicense(window, MAIL, newCode);
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
  }

  private void checkUserNotRegistered() {
    license.checkUserNotRegistered();
  }

  private void checkValidLicense() {
    license.checkUserIsRegistered();
  }

  private void restartAppAndLogAndDispose() throws Exception {
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    exit();
  }

  private void activateNewLicenseInNewVersion(String code) throws Exception {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
    LicenseActivationChecker.enterLicense(window, MAIL, code);
    checkValidLicense();
    exit();
  }

  private void checkVersionValidity(final boolean anonymous, final String pathToData) throws Exception {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApplication(false);
    login.logExistingUser("user", "passw@rd");
    checkValidLicense();
    exit();
  }

  private String loginAndRegisterFirstPicsou() throws InterruptedException {
    String repoId = db.checkRepoIdIsUpdated(1L, null);

    login.logNewUser("user", "passw@rd");
    db.registerMail(MAIL, "1234");
    LicenseActivationChecker.enterLicense(window, MAIL, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();

    db.checkLicenseCount(MAIL, 1);
    return repoId;
  }

  private void register(DbChecker db, String email, final String code) throws InterruptedException {
    db.checkRepoIdIsUpdated(1L, null);
    register(email, code);
    db.checkLicenseCount(email, 1);
  }

  public void register(String email, String code) {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.enterLicense(window, email, code);
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();
  }

  public void testSendConfirmationFeedback() throws Exception {
    final String email = "alfred@free.fr";
    db.registerMail(email, "1234");

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");

    LicenseActivationChecker.open(window)
      .enterLicenseAndActivate(email, "1234")
      .checkActivationCompleted()
      .enterAnswer(0, "My first answer")
      .enterAnswer(2, "My third answer")
      .complete();

    mailServer.checkReceivedMail(Mailbox.SUPPORT.getEmail())
      .checkContains(Lang.get("license.activation.feedback.question.0"))
      .checkContains("My first answer")
      .checkDoesNotContain(Lang.get("license.activation.feedback.question.1"))
      .checkContains(Lang.get("license.activation.feedback.question.2"))
      .checkContains("My third answer");
  }

  public void testConfirmationWithoutFeedback() throws Exception {
    final String email = "alfred@free.fr";
    db.registerMail(email, "1234");

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");

    LicenseActivationChecker.open(window)
      .enterLicenseAndActivate(email, "1234")
      .checkActivationCompleted()
      .complete();

    mailServer.checkEmpty();
  }

  private void exit() {
    new OperationChecker(window).exit();
    assertFalse(window.isVisible());
    window = null;
  }
}
