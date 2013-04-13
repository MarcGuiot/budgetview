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

import java.io.File;
import java.io.IOException;

public class MobileTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private MobileAppChecker mobileApp;

  public void setUp() throws Exception {
    super.setUp();
    startServers();
    application = new ApplicationChecker();
    application.start();
    File directory = new File("/tmp/data/");
    Files.deleteSubtree(directory);
    directory.mkdir();
    mobileApp = new MobileAppChecker(httpPort);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    application.dispose();
    application = null;
    mobileApp = null;
  }

  public void testCreateAndDeleteAccount() throws Exception {
    String mail = "test@mybudgetview.fr";
    SharingConnection connection = requestNewMobileAccount(mail);
    followUrl(connection.url, 302, "http://www.mybudgetview.com/mobile/account-ok");

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
    String mail = "test@mybudgetview.fr";
    application.openMobileAccountDialog()
      .validateAndCheckEmailTip("You must enter your email address")
      .setEmailAndValidate(mail)
      .checkNoErrorsShown()
      .checkConfirmationAndClose();
    Email email = mailServer.checkReceivedMail(mail);
    email.checkContains("http");
  }

  public void testGetData() throws Exception {
    String emailAddress = "test@mybudgetview.fr";

    SharingConnection sharingConnection = requestNewMobileAccount(emailAddress);
    String url = sharingConnection.url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/account-ok");

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

    followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-already-present");
    application.getOperations()
      .sendDataToServer()
      .checkSuccessMessageContains("Data sent to server")
      .close();

    mobileApp.checkLoginFails(emailAddress, "anotherPassword");
  }

  public void testAlreadyActivated() throws Exception {
    String mail = "test@mybudgetview.fr";
    String url1 = requestNewMobileAccount(mail).url;
    String url2 = requestNewMobileAccount(mail).url;
    followUrl(url1, 302, "http://www.mybudgetview.com/mobile/account-ok");
    followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-already-present");
  }

  public void testError() throws Exception {
    File directory = new File("/tmp/data/");
    Files.deleteSubtree(directory);
    String mail = "test@mybudgetview.fr";
    String url = requestNewMobileAccount(mail).url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/internal-error");
  }

  public void testReminderMail() throws Exception {

    mobileApp.sendEmail("test@budgetview.fr");

    Email email = mailServer.checkReceivedMail("test@budgetview.fr");
    email.checkSubjectContains("Votre rappel pour BudgetView");
    email.checkContains("l'adresse suivante");
  }

  public void testPendingDataAreSentAtAccountCreation() throws Exception {
    String emailAddress = "test@mybudgetview.fr";

    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();
    application.getOperations().importOfxFile(path);


    SharingConnection sharingConnection = requestNewMobileAccount(emailAddress);
    File directory = new File("/tmp/data/");
    String[] list = directory.list();
    assertTrue(list != null && list.length == 1);
    list = new File(directory, list[0]).list();
    assertTrue(list != null && list.length == 2);
    assertTrue(list[0].startsWith("pending"));
    assertTrue(list[1].startsWith("pending"));
    String url = sharingConnection.url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/account-ok");

    mobileApp.checkLogin(emailAddress, sharingConnection.password);


  }

  private void followUrl(String url, final int expectedReturnCode, final String expectedRedirect) throws IOException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet method = new HttpGet(url);
    HttpClientParams.setRedirecting(method.getParams(), false);
    HttpResponse response = httpClient.execute(method);
    assertEquals(expectedReturnCode, response.getStatusLine().getStatusCode());
    Header locationHeader = response.getFirstHeader("location");
    assertNotNull(locationHeader);
    assertEquals(expectedRedirect, locationHeader.getValue());
  }

  private SharingConnection requestAccountWithNewPassword(String userMail) throws InterruptedException {
    return requestMobileAccount(userMail, true);
  }

  private SharingConnection requestNewMobileAccount(String userMail) throws InterruptedException {
    return requestMobileAccount(userMail, false);
  }

  private SharingConnection requestMobileAccount(String userMail, boolean generateNewPwd) throws InterruptedException {
    CreateMobileAccountChecker dialog = application.openMobileAccountDialog();
    if (generateNewPwd) {
      dialog.generateNewPassword();
    }
    String password = dialog.getPassword();
    dialog.setEmailAndValidate(userMail);
    dialog.checkConfirmationAndClose();

    Email email = mailServer.checkReceivedMail(userMail);
    email.checkContains("http");
    int httpStartIndex = email.getContent().indexOf("href=\"");
    int httpEndIndex = email.getContent().indexOf("\">http");
    String url = email.getContent().substring(httpStartIndex + "href=\"".length(), httpEndIndex);
    url = url.replace("http://www.mybudgetview.fr", "http://localhost");
    return new SharingConnection(url, password);
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
