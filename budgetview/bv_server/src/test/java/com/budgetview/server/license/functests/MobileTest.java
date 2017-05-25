package com.budgetview.server.license.functests;

import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.functests.checkers.mobile.CreateMobileAccountChecker;
import com.budgetview.functests.checkers.mobile.MobileAppChecker;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.server.license.ConnectedTestCase;
import com.budgetview.server.license.checkers.Email;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;

import java.io.File;

public class MobileTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private MobileAppChecker mobileApp;

  public void setUp() throws Exception {
    super.setUp();
    startServers();
    application = new ApplicationChecker();
    application.start();
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
    MobileConnection connection = requestNewMobileAccount(mail);
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
    email.checkContainsAll("http");
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
    email.checkContainsAll("http");
  }

  public void testGetData() throws Exception {
    application.enableAllAddOns();

    String emailAddress = "testGetData@mybudgetview.fr";

    MobileConnection mobileConnection = requestNewMobileAccount(emailAddress);
    String url = mobileConnection.url;
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

    mobileApp.checkLogin(emailAddress, mobileConnection.password);

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
    Files.deleteWithSubtree(MOBILE_DATA_DIR);
    String mail = "testError@mybudgetview.fr";
    String url = requestNewMobileAccount(mail).url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/internal-error");
  }

  public void testReminderMail() throws Exception {

    mobileApp.sendEmail("testReminderMail@budgetview.fr");

    Email email = mailServer.checkReceivedMail("testReminderMail@budgetview.fr");
    email.checkSubjectContains("Votre rappel pour BudgetView");
    email.checkContainsAll("l'adresse suivante");
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

    MobileConnection mobileConnection = requestNewMobileAccount(emailAddress);
    TestUtils.retry(new Runnable() {
      public void run() {
        String[] list = MOBILE_DATA_DIR.list();
        assertTrue(list != null && list.length == 1);
        list = new File(MOBILE_DATA_DIR, list[0]).list();
        assertTrue(list != null && list.length == 2);
        assertTrue(list[0].startsWith("pending"));
        assertTrue(list[1].startsWith("pending"));
      }
    });
    String url = mobileConnection.url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/account-ok", emailAddress);

    mobileApp.checkLogin(emailAddress, mobileConnection.password);
  }

  private void followUrl(String url, final int expectedReturnCode, final String expectedRedirect) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet method = new HttpGet(url);
    HttpClientParams.setRedirecting(method.getParams(), false);
    HttpResponse response = httpClient.execute(method);
    if (expectedReturnCode == 302) {
      int code = response.getStatusLine().getStatusCode();
      assertTrue("got " + code + " but " + expectedReturnCode + " was expected.", code == 302 || code == 200);
    }
    Header locationHeader = response.getFirstHeader("location");
    assertNotNull(locationHeader);
    assertEquals(expectedRedirect, locationHeader.getValue());
  }

  private void followUrl(String url, final int expectedReturnCode, final String expectedRedirect, String emailAddress) throws Exception {
    followUrl(url, expectedReturnCode, expectedRedirect);
    Email email = mailServer.checkReceivedMail(emailAddress);
    email.checkContainsAll("To install the Android app");
  }

  private MobileConnection requestAccountWithNewPassword(String userMail) throws Exception {
    return requestMobileAccount(userMail, "newpassword");
  }

  private MobileConnection requestNewMobileAccount(String userMail) throws Exception {
    return requestMobileAccount(userMail, null);
  }

  private MobileConnection requestMobileAccount(String userMail, String requestedPassword) throws Exception {
    CreateMobileAccountChecker dialog = application.openMobileAccountDialog();
    if (Strings.isNotEmpty(requestedPassword)) {
      dialog.setNewPassword(requestedPassword);
    }
    String activatedPassword = dialog.getPassword();
    dialog.setEmailAndValidate(userMail);
    dialog.checkConfirmationAndClose();

    Email email = mailServer.checkReceivedMail(userMail);
    email.checkContainsAll("http");
    String content = email.getContent();
    int httpStartIndex = content.indexOf("href=\"");
    int httpEndIndex = content.indexOf("\">http");
    String url = content.substring(httpStartIndex + "href=\"".length(), httpEndIndex);
    url = url.replace("https://www.mybudgetview.fr", "http://localhost");
    Thread.sleep(500); // on attend que les donnée pending soit envoyé au serveur
    return new MobileConnection(url, activatedPassword);
  }

  private class MobileConnection {
    final String url;
    final String password;

    private MobileConnection(String url, String password) {
      this.url = url;
      this.password = password;
    }
  }
}
