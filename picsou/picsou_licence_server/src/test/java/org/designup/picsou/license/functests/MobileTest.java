package org.designup.picsou.license.functests;

import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.ComCst;
import com.budgetview.shared.utils.Crypt;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.functests.checkers.CreateMobileAccountChecker;
import org.designup.picsou.functests.checkers.MessageDialogChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.checkers.Email;
import org.globsframework.model.GlobList;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;
import java.net.URLEncoder;

public class MobileTest extends ConnectedTestCase {
  private ApplicationChecker application;

  public void setUp() throws Exception {
    super.setUp();
    startServers();
    application = new ApplicationChecker();
    application.start();
    File directory = new File("/tmp/data/");
    Files.deleteSubtree(directory);
    directory.mkdir();
  }

  public void testCreateAndDeleteAccount() throws Exception {
    String mail = "test@mybudgetview.fr";
    SharingConnection connection = requestMobileAccount(mail);
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
    String mail = "test@mybudgetview.fr";
    {
      //String url = requestMobileAccount(mail, "hello");
      SharingConnection sharingConnection = requestMobileAccount(mail);
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

      HttpClient httpClient = new DefaultHttpClient();
      URIBuilder builder = new URIBuilder("http://localhost:" + httpPort + ComCst.GET_MOBILE_DATA);
      Crypt crypt = new Crypt(sharingConnection.password.toCharArray());
      builder.addParameter("mail", URLEncoder.encode(mail, "UTF-8"));
      builder.addParameter(ComCst.CRYPTED_INFO,
                           URLEncoder.encode(Crypt.encodeSHA1AndHex(crypt.encodeData(mail.getBytes("UTF-8"))), "UTF-8"));
      HttpGet method = new HttpGet(builder.build());
      HttpResponse response = httpClient.execute(method);
      assertEquals(200, response.getStatusLine().getStatusCode());
      DefaultGlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
      InputStream content = response.getEntity().getContent();
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      Files.copyStream(content, stream);
      String s = crypt.decodeAndUnzipData(stream.toByteArray());
      XmlGlobParser.parse(MobileModel.get(), repository, new StringReader(s), "globs");
      GlobList all = repository.getAll();
      assertFalse(all.isEmpty());

      // change password
      String url2 = newPasswordRequest(mail).url;

      application.getOperations()
        .sendDataToServer()
        .checkErrorMessageContains("Failed to send data to server: Password has changed")
        .close();

      followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-already-present");
      application.getOperations()
        .sendDataToServer()
        .checkSuccessMessageContains("Data sent to server")
        .close();
    }

    //try download with previous password.
    {
      HttpClient httpClient = new DefaultHttpClient();
      URIBuilder builder = new URIBuilder("http://localhost:" + httpPort + ComCst.GET_MOBILE_DATA);
      Crypt crypt = new Crypt("hello".toCharArray());
      builder.addParameter("mail", URLEncoder.encode(mail, "UTF-8"));
      builder.addParameter(ComCst.CRYPTED_INFO,
                           URLEncoder.encode(Crypt.encodeSHA1AndHex(crypt.encodeData(mail.getBytes("UTF-8"))), "UTF-8"));
      HttpGet method = new HttpGet(builder.build());
      HttpResponse response = httpClient.execute(method);
      assertEquals(403, response.getStatusLine().getStatusCode());
    }
  }

  public void testAlreadyActivated() throws Exception {
    String mail = "test@mybudgetview.fr";
    String url1 = requestMobileAccount(mail).url;
    String url2 = requestMobileAccount(mail).url;
    followUrl(url1, 302, "http://www.mybudgetview.com/mobile/account-ok");
    followUrl(url2, 302, "http://www.mybudgetview.com/mobile/account-already-present");
  }

  public void testError() throws Exception {
    File directory = new File("/tmp/data/");
    Files.deleteSubtree(directory);
    String mail = "test@mybudgetview.fr";
    String url = requestMobileAccount(mail).url;
    followUrl(url, 302, "http://www.mybudgetview.com/mobile/internal-error");
  }

  public void testReminderMail() throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    URIBuilder builder = new URIBuilder("http://localhost:" + httpPort + ComCst.SEND_MAIL_REMINDER_FROM_MOBILE);
    builder.addParameter("mail", URLEncoder.encode("test@budgetview.fr", "UTF-8"));
    HttpPost method = new HttpPost(builder.build());
    HttpResponse response = httpClient.execute(method);
    assertEquals(200, response.getStatusLine().getStatusCode());
    Email email = mailServer.checkReceivedMail("test@budgetview.fr");
    email.checkSubjectContains("Votre rappel pour BudgetView");
    email.checkContains("l'adresse suivante");
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

  private SharingConnection newPasswordRequest(String userMail) throws InterruptedException {
    return requestMobileAccount(userMail, true);
  }

  private SharingConnection requestMobileAccount(String userMail) throws InterruptedException {
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
