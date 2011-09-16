package org.designup.picsou.license.functests;

import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.checkers.DbChecker;
import org.designup.picsou.license.checkers.Email;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.RepoInfo;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Dates;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseTest extends ConnectedTestCase {
  private PicsouApplication picsouApplication;
  private Window window;
  private LoginChecker login;
  private static final String MAIL = "alfred@free.fr";
  private static final String SECOND_PATH = "tmp/otherprevayler";

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
      exit();
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
  }

  public void testConnectAtStartup() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense(false);

    exit();

    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);

    Glob license = db.getLicense(email, License.ACCESS_COUNT, 3L);
    assertEquals(3L, license.get(License.ACCESS_COUNT).longValue());
    checkValidLicense(false);
  }

  public void testMultipleAnonymousConnect() throws Exception {
    db.checkRepoIdIsUpdated(1L, null);
    login.logNewUser("user", "passw@rd");
    exit();

    startApplication(false);
    db.checkRepoIdIsUpdated(2L, null);
    login.logExistingUser("user", "passw@rd", true);
    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkLicenseExpired();
  }

  public void testResendsActivationKeyIfCountDecreases() throws Exception {
    String repoId = loggingAndRegisterFirstPicsou();
    exit();
    restartAppAndLogAndDispose();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("user", "passw@rd");

    db.checkRepoIdIsUpdated(1L, Constraints.notEqual(RepoInfo.REPO_ID, repoId));
    checkDaysLeftMessage();

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234", "Activation failed. An email was sent at " + MAIL + " with further information.");
    checkDaysLeftMessage();
    Email email = mailServer.checkReceivedMail(MAIL);
    email.checkContains("Multiple use of the same license");

    String emailcontent = email.getContent();
    int startCode = emailcontent.indexOf("new code ") + 9;
    String newActivationCode = emailcontent.substring(startCode, startCode + 4);
    Integer.parseInt(newActivationCode);
    exit();

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
    login.logExistingUser("user", "passw@rd", true);
    checkKilled();
    exit();
  }

  public void testRegisterAndReRegisterWithBadEmail() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense(false);

    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    LicenseActivationChecker.enterBadLicense(window, "titi@foo.org", "4321", "Unknown email address");

    db.checkLicenseCount(email, 3);
    checkValidLicense(false);
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkValidLicense(false);
  }

  public void testRegisterAndReRegisterWithBadActivationCode() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense(false);

    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    db.checkLicenseCount(email, 3);
    checkValidLicense(false);
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkValidLicense(false);
  }

  public void testRegisterAndReRegisterWithBadActivationCodeWithoutRestart() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense(false);
    LicenseActivationChecker.enterBadLicense(window, "alfred@free.fr", "4321", "Activation failed");

    checkValidLicense(false);
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkValidLicense(false);
  }

  public void testUnknownEmailAddress() throws Exception {
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker activationChecker = LicenseActivationChecker.open(window);
    activationChecker
      .enterLicenseAndValidate("titi@foo.org", "az")
      .checkErrorMessage("Unknown email address")
      .checkActivationCodeIsEmptyAndMailIs("titi@foo.org");
    checkMessage("Unknown email address");

    db.registerMail("toto@zer", "1234");
    activationChecker
      .enterLicense("toto@zer", "1234")
      .validate()
      .checkClosed();
    checkValidLicense(false);
  }

  public void testRegistrationWithBadKey() throws Exception {
    db.registerMail("titi@foo.org", "1234");
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.open(window)
      .enterLicenseAndValidate("titi@foo.org", "az")
      .checkErrorMessage("Activation failed")
      .cancel();
    checkMessage("46 days left for trying BudgetView");
    checkMessage("This activation code is not valid. You can request");
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    exit();
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkLicenseExpired();
  }

  public void testStartRegistrationAndStopServer() throws Exception {
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker license = LicenseActivationChecker.open(window)
      .enterLicense("titi@foo.org", "az");

    stopServers();
    license.validate();
    license.checkErrorMessage("Activation failed")
      .cancel();

    checkMessage("46 days left for trying BudgetView");
    checkMessage("Cannot connect to remote server");

    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkLicenseExpired();
  }

  public void testEmptyActivationCode() throws Exception {
    login.logNewUser("user", "passw@rd");

    db.registerMail("titi@foo.org", "4321");
    LicenseActivationChecker license = LicenseActivationChecker.open(window);
    license.checkMsgToReceiveNewCode();
    license.enterLicense("titi@foo.org", "az");
    license.checkMsgSendNewCode();
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.enterLicense("titi@foo.org", "");
    license.checkMsgSendNewCode();
    license.validate();
    license.checkErrorMessage("Activation failed");
    license.cancel();

    checkMessage("46 days left for trying BudgetView");
    checkMessage("This activation code is not valid. You can request");

    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkLicenseExpired();
  }

  private void checkMessage(final String messageTxt) {
    TextBox message = getMessageBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains(messageTxt));
  }

  public void testRegisterAndReRegisterToOtherFailedAndSendAMail() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    register(db, email, "1234");
    checkValidLicense(false);

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
    login.logExistingUser("user", "passw@rd", true);
    LicenseActivationChecker.enterBadLicense(window, email, "1234",
                                             "Activation failed. An email was sent at " + MAIL + " with further information.");
    mailServer.checkReceivedMail(email);
    checkWithMailKilled();
  }

  public void testSendCodeFromActivation() throws Exception {
    db.registerMail("alfred@free.fr", "1234");
    login.logNewUser("user", "passw@rd");
    LicenseActivationChecker activation = LicenseActivationChecker.open(window);
    activation.enterLicense("alfred@free.fr", "");
    activation.askForCode();
    String newCode = checkMailAndExtractCode();
    assertFalse(newCode.equals("1234"));
    activation.cancel();
  }

  public void testMailSentLater() throws Exception {
    loggingAndRegisterFirstPicsou();
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
    loggingAndRegisterFirstPicsou();
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    startApplication(true);
    login.logNewUser("user", "passw@rd");
    exit();

    TimeService.setCurrentDate(Dates.parseMonth("2008/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);

    LicenseActivationChecker.enterBadLicense(window, MAIL, "1234",
                                             "Activation failed. An email was sent at " + MAIL + " with further information.");
    String newCode = checkMailAndExtractCode();
    LicenseActivationChecker.enterLicense(window, "alfred@free.fr", newCode);
    exit();

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);

    LicenseInfoChecker licenseInfo = new LicenseInfoChecker(window);
    LicenseExpirationChecker expirationChecker = licenseInfo.clickNewLicense();
    expirationChecker
      .checkMail("alfred@free.fr")
      .sendKey()
      .close();

    String newEmail = mailServer.checkReceivedMail(MAIL).getContent();
    newCode = newEmail.substring(newEmail.length() - 5, newEmail.length()).trim();
    exit();
    SqlConnection connection = db.getConnection();
    Glob glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, MAIL))
      .selectAll()
      .getQuery().executeUnique();
    assertEquals(newCode, glob.get(License.ACTIVATION_CODE));
  }

  private String checkMailAndExtractCode() throws InterruptedException {
    return mailServer.checkReceivedMail(MAIL).getEnd(5);
  }

  public void testTrialVersionIsOver() throws Exception {
    login.logNewUser("user", "passw@rd");
    exit();
    TimeService.setCurrentDate(Dates.parse("2008/10/10"));
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    LicenseInfoChecker licenseInfo = new LicenseInfoChecker(window);
    licenseInfo.checkMessage("Your free trial period is over.");
  }

  public void testActivationFailDuringTrial() throws Exception {
    db.registerMail(MAIL, "4321");
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
    ImportDialogChecker importDialog = new ImportDialogChecker(dialog, true);
    importDialog.close();
    TextBox messageBox = window.getTextBox("licenseInfoMessage");
    if (!anonymous) {
      assertFalse(messageBox.isVisible());
    }
    else {
      assertTrue(messageBox.isVisible());
    }
  }

  private void restartAppAndLogAndDispose() throws Exception {
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    exit();
  }

  private void activateNewLicenseInNewVersion(String code) throws Exception {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, SECOND_PATH);
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkValidLicense(true);
    LicenseActivationChecker.enterLicense(window, MAIL, code);
    checkValidLicense(false);
    exit();
  }

  private void checkVersionValidity(final boolean anonymous, final String pathToData) throws Exception {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, pathToData);
    startApplication(false);
    login.logExistingUser("user", "passw@rd", true);
    checkValidLicense(anonymous);
    exit();
  }

  private String loggingAndRegisterFirstPicsou() throws InterruptedException {
    String repoId = db.checkRepoIdIsUpdated(1L, null);

    login.logNewUser("user", "passw@rd");
    db.registerMail(MAIL, "1234");
    LicenseActivationChecker.enterLicense(window, MAIL, "1234");
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();

    db.checkLicenseCount(MAIL, 1);
    return repoId;
  }

  private void checkDaysLeftMessage() {
    TextBox message = getMessageBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("days left"));
  }

  private TextBox getMessageBox() {
    return window.getTextBox("licenseInfoMessage");
  }

  private void checkMessageOver() {
    checkMessage("Your free trial period is over");
  }

  private void checkWithMailKilled() {
    TextBox message = getMessageBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("Activation failed. An email was sent at " + MAIL + " with further information."));
  }

  private void checkActivationFailed() {
    TextBox message = getMessageBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("This activation code is not valid."));
    assertTrue(message.textContains("You can request"));
  }

  private void checkKilled() {
    TextBox message = getMessageBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("You are not allowed to import data anymore"));
  }

  private void register(DbChecker db, String email, final String code) throws InterruptedException {
    db.checkRepoIdIsUpdated(1L, null);
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    LicenseActivationChecker.enterLicense(window, email, code);
    OperationChecker operation = new OperationChecker(window);
    operation.openPreferences().setFutureMonthsCount(24).validate();
    db.checkLicenseCount(email, 1);
  }

  private void exit() {
    new OperationChecker(window).exit();
    assertFalse(window.isVisible());
    window = null;
  }
}
