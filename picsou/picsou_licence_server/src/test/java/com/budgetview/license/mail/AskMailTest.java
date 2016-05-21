package com.budgetview.license.mail;

import com.budgetview.license.checkers.Email;
import com.budgetview.license.model.License;
import com.budgetview.license.model.MailError;
import com.budgetview.shared.utils.ComCst;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import com.budgetview.gui.config.ConfigService;
import com.budgetview.license.ConnectedTestCase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Utils;

import java.io.IOException;

public class AskMailTest extends ConnectedTestCase {
  private HttpClient client;

  protected void setUp() throws Exception {
    super.setUp();
    startServers();
    client = new DefaultHttpClient();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    client = null;
  }

  public void testSendMailInEn() throws Exception {
    checkMail("en", "Here is the new activation code", "monPremierClient@pirate.du");
  }

  public void testSendMailInFr() throws Exception {
    checkMail("fr", "Suite a votre demande, veuillez trouver", "monPremierClient@pirate.du");
  }

  private void checkMail(String lang, final String expected, String... nextExpected) throws IOException, InterruptedException {
    addUser("monPremierClient@pirate.du");
    HttpResponse response = sendRequest(lang);
    Header header = response.getFirstHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_SENT, header.getValue());
    Email email = mailServer.checkReceivedMail("monPremierClient@pirate.du");
    for (String content : Utils.join(expected, nextExpected)) {
      email.checkContains(content);
    }
  }

  private HttpResponse sendRequest(String lang) throws IOException {
    HttpPost postMethod = new HttpPost("http://localhost:" + httpPort + "/mailTo");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    postMethod.setHeader(ConfigService.HEADER_MAIL, "monPremierClient@pirate.du");
    postMethod.setHeader(ComCst.HEADER_LANG, lang);
    return client.execute(postMethod);
  }

  public void testAddInDbIfBadAdress() throws Exception {
    HttpPost postMethod = new HttpPost("http://localhost:" + httpPort + "/mailTo");
    String badMail = "monPremierClient@pirate";
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    postMethod.setHeader(ConfigService.HEADER_MAIL, badMail);
    postMethod.setHeader(ComCst.HEADER_LANG, "en");
    HttpResponse response = client.execute(postMethod);
    Header header = response.getFirstHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_UNKNOWN, header.getValue());
    SqlConnection connection = db.getConnection();
    connection.getQueryBuilder(MailError.TYPE, Constraints.equal(MailError.MAIL, badMail))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void checkInBase(String mailTo) {
    SqlConnection connection = db.getConnection();
    connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mailTo))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void addUser(String mail) {
    SqlConnection connection = db.getConnection();
    try {
      connection.getCreateBuilder(License.TYPE)
        .set(License.MAIL, mail)
        .getRequest().run();
    }
    finally {
      connection.commitAndClose();
    }
  }
}
