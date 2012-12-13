package org.designup.picsou.license.mail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.checkers.Email;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.Utils;

import java.io.IOException;

public class AskMailTest extends ConnectedTestCase {
  private HttpClient client;

  protected void setUp() throws Exception {
    super.setUp();
    startServers();
    client = new HttpClient();
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
    PostMethod postMethod = sendRequest(lang);
    Header header = postMethod.getResponseHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_SENT, header.getValue());
    Email email = mailServer.checkReceivedMail("monPremierClient@pirate.du");
    for (String content : Utils.join(expected, nextExpected)) {
      email.checkContains(content);
    }
  }

  private PostMethod sendRequest(String lang) throws IOException {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    postMethod.getParams().setContentCharset("UTF-8");
    postMethod.setRequestHeader(ConfigService.HEADER_MAIL, "monPremierClient@pirate.du");
    postMethod.setRequestHeader(ConfigService.HEADER_LANG, lang);
    client.executeMethod(postMethod);
    return postMethod;
  }

  public void testAddInDbIfBadAdress() throws Exception {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    String badMail = "monPremierClient@pirate";
    postMethod.getParams().setContentCharset("UTF-8");
    postMethod.setRequestHeader(ConfigService.HEADER_MAIL, badMail);
    postMethod.setRequestHeader(ConfigService.HEADER_LANG, "en");
    client.executeMethod(postMethod);
    Header header = postMethod.getResponseHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_UNKNOWN, header.getValue());
    SqlConnection connection = db.getConnection();
    connection.getQueryBuilder(MailError.TYPE, Constraints.equal(MailError.MAIL, badMail))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void checkInBase(String mailTo) {
    SqlConnection connection = db.getConnection();
    connection.getQueryBuilder(License.TYPE, Constraints.equal(License.EMAIL, mailTo))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void addUser(String mail) {
    SqlConnection connection = db.getConnection();
    try {
      connection.getCreateBuilder(License.TYPE)
        .set(License.EMAIL, mail)
        .getRequest().run();
    }
    finally {
      connection.commitAndClose();
    }
  }
}
