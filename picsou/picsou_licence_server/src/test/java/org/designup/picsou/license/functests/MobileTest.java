package org.designup.picsou.license.functests;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.functests.checkers.mobile.CreateMobileAccountChecker;
import org.designup.picsou.functests.checkers.mobile.MobileAppChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.checkers.Email;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;

import java.io.File;
import java.io.IOException;

public class MobileTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private MobileAppChecker mobileApp;
  private static final File DIRECTORY = new File("/tmp/data/");

  public void setUp() throws Exception {
    super.setUp();
    startServers();
    application = new ApplicationChecker();
    application.start();
    Files.deleteSubtreeOnly(DIRECTORY);
    DIRECTORY.mkdir();
    mobileApp = new MobileAppChecker(httpPort);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    application.dispose();
    application = null;
    mobileApp = null;
  }

  public void testMenuAvailableOnlyWhenAddOnActivated() throws Exception {
    application.checkMobileAccessDisabled();
    application.enableAllAddOns();
    application.checkMobileAccessEnabled();
  }

  public void testCreateAndDeleteAccount() throws Exception {
    application.enableAllAddOns();

    String mail = "testCreateDelete@mybudgetview.fr";
    SharingConnection connection = requestNewMobileAccount(mail);
    followUrl(connection.url, 302, "http://www.mybudgetview.com/mobile/account-ok", mail);

    application.openMobileAccountDialog()
      .setEmailAndValidate(mail)
      .checkConfirmationAndClose();

    application.openDeleteMobileAccountDialog()
      .checkUser(mail, connection.password)
      .validateAndClose();

    application.openMobileAccountDialog()
      .checkTitle()
      .close();
  }

  public void testEmptyEmailMessage() throws Exception {
    String mail = "testEmpty@mybudgetview.fr";
    application.enableAllAddOns();
    application.openMobileAccountDialog()
      .validateAndCheckEmailTip("You must enter your email address")
      .setEmailAndValidate(mail)
      .checkNoErrorsShown()
      .checkConfirmationAndClose();
    Email email = mailServer.checkReceivedMail(mail);
    email.checkContains("http");
  }

  public void testChangePassword() throws Exception {
    application.enableAllAddOns();
    String mail = "testChangePassword@mybudgetview.fr";
    CreateMobileAccountChecker dialog = application.openMobileAccountDialog();
    String generatedPassword = dialog
      .setEmailWithoutValidating(mail)
      .getPassword();
    dialog.checkReadOnlyPassword(generatedPassword)
      .editPasswordAndCancel("dummy")
      .checkReadOnlyPassword(generatedPassword);
    dialog.setEmptyPasswordAndCheckErrorOnApply("You must enter a password");
    dialog.setEmptyPasswordAndCheckErrorOnActivate("You must enter a password");
    dialog.setNewPassword("newPassword")
      .validateAndClose();
    Email email = mailServer.checkReceivedMail(mail);
    email.checkContains("http");
  }

  public void testGetData() throws Exception {
    application.enableAllAddOns();

    String emailAddress = "testGetData@mybudgetview.fr";

    SharingConnection sharingConnection = requestNewMobileAccount(emailAddress);
    String url = sharingConnection.url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/account-ok", emailAddress);

    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();
    application.getOperations().importOfxFile(path);

    application.getOperations()
      .sendDataToServer()
      .checkSuccessMessageContains("Data sent to server")
      .close();

    mobileApp.checkLogin(emailAddress, sharingConnection.password);

    // change password
    String url2 = requestAccountWithNewPassword(emailAddress).url;

    application.getOperations()
      .sendDataToServer()
      .checkErrorMessageContains("Failed to send data to server: Password has changed")
      .close();

    followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-ok", emailAddress);
    application.getOperations()
      .sendDataToServer()
      .checkSuccessMessageContains("Data sent to server")
      .close();

    mobileApp.checkLoginFails(emailAddress, "anotherPassword");
  }

  public void testAlreadyActivated() throws Exception {
    application.enableAllAddOns();
    String mail = "testAlreadyActivated@mybudgetview.fr";
    String url1 = requestNewMobileAccount(mail).url;
    String url2 = requestNewMobileAccount(mail).url;
    followUrl(url1, 302, "http://www.mybudgetview.com/mobile/account-ok", mail);
    followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-already-present", mail);
  }

  public void testError() throws Exception {
    application.enableAllAddOns();
    Files.deleteWithSubtree(DIRECTORY);
    String mail = "testError@mybudgetview.fr";
    String url = requestNewMobileAccount(mail).url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/internal-error");
  }

  public void testReminderMail() throws Exception {

    mobileApp.sendEmail("testReminderMail@budgetview.fr");

    Email email = mailServer.checkReceivedMail("testReminderMail@budgetview.fr");
    email.checkSubjectContains("Votre rappel pour BudgetView");
    email.checkContains("l'adresse suivante");
  }

  public void testPendingDataAreSentAtAccountCreation() throws Exception {
    application.enableAllAddOns();

    String emailAddress = "testPending@mybudgetview.fr";

    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();
    application.getOperations().importOfxFile(path);

    SharingConnection sharingConnection = requestNewMobileAccount(emailAddress);
    TestUtils.retry(new Runnable() {
      public void run() {
        String[] list = DIRECTORY.list();
        assertTrue(list != null && list.length == 1);
        list = new File(DIRECTORY, list[0]).list();
        assertTrue(list != null && list.length == 2);
        assertTrue(list[0].startsWith("pending"));
        assertTrue(list[1].startsWith("pending"));
      }
    });
    String url = sharingConnection.url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/account-ok", emailAddress);

    mobileApp.checkLogin(emailAddress, sharingConnection.password);
  }

  private void followUrl(String url, final int expectedReturnCode, final String expectedRedirect) throws IOException, InterruptedException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet method = new HttpGet(url);
    HttpClientParams.setRedirecting(method.getParams(), false);
    HttpResponse response = httpClient.execute(method);
    if (expectedReturnCode == 302){
      int code = response.getStatusLine().getStatusCode();
      assertTrue("got " + code + " but " + expectedReturnCode + " was expected.", code == 302 || code == 200);
    }
    Header locationHeader = response.getFirstHeader("location");
    assertNotNull(locationHeader);
    assertEquals(expectedRedirect, locationHeader.getValue());
  }

  private void followUrl(String url, final int expectedReturnCode, final String expectedRedirect, String emailAddress) throws IOException, InterruptedException {
    followUrl(url, expectedReturnCode, expectedRedirect);
    Email email = mailServer.checkReceivedMail(emailAddress);
    email.checkContains("To install the Android app");
  }

  private SharingConnection requestAccountWithNewPassword(String userMail) throws InterruptedException {
    return requestMobileAccount(userMail, "newpassword");
  }

  private SharingConnection requestNewMobileAccount(String userMail) throws InterruptedException {
    return requestMobileAccount(userMail, null);
  }

  private SharingConnection requestMobileAccount(String userMail, String requestedPassword) throws InterruptedException {
    CreateMobileAccountChecker dialog = application.openMobileAccountDialog();
    if (Strings.isNotEmpty(requestedPassword)) {
      dialog.setNewPassword(requestedPassword);
    }
    String activatedPassword = dialog.getPassword();
    dialog.setEmailAndValidate(userMail);
    dialog.checkConfirmationAndClose();

    Email email = mailServer.checkReceivedMail(userMail);
    email.checkContains("http");
    String content = email.getContent();
    int httpStartIndex = content.indexOf("href=\"");
    int httpEndIndex = content.indexOf("\">http");
    String url = content.substring(httpStartIndex + "href=\"".length(), httpEndIndex);
    url = url.replace("http://www.mybudgetview.fr", "http://localhost");
    Thread.sleep(500); // on attend que les donnée pending soit envoyé au serveur
    return new SharingConnection(url, activatedPassword);
  }

  private class SharingConnection {
    final String url;
    final String password;

    private SharingConnection(String url, String password) {
      this.url = url;
      this.password = password;
    }
  }
}
