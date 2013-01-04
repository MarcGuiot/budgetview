package org.designup.picsou.license.functests;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.checkers.Email;
import org.globsframework.utils.Files;

import java.io.File;
import java.io.IOException;

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

  public void testCreateAccount() throws Exception {
    String mail = "test@mybudgetview.fr";
    String url = requestMobileAccount(mail);
    followUrl(url, 302, "http://www.mybudgetview.fr/mobileAccountOk-en.html");
    application.getMobileAccountChecker()
      .setMail(mail)
      .setPassword("hello")
      .checkAlreadyCreated()
      .cancel();
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

  private String requestMobileAccount(String userMail) throws InterruptedException {
    application.getMobileAccountChecker()
      .setMail(userMail)
      .setPassword("hello")
      .validate();
    Email email = mailServer.checkReceivedMail(userMail);
    email.checkContains("http");
    int httpStartIndex = email.getContent().indexOf("href=\"");
    int httpEndIndex = email.getContent().indexOf("\">http");
    String url = email.getContent().substring(httpStartIndex + "href=\"".length(), httpEndIndex);
    url = url.replace("https://www.mybudgetview.fr:443", "http://localhost:" + httpPort);
    return url;
  }

}
