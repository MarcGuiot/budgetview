package org.designup.picsou.license.mail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;

import java.io.IOException;

public class AskMailTest extends ConnectedTestCase {
  private HttpClient client;

  protected void setUp() throws Exception {
    super.setUp();
    licenseServer.init();
    startServers();
    client = new HttpClient();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stop();
    client = null;
  }

  public void testSendMailInEn() throws Exception {
    checkMail("en", "You 'monPremierClient@pirate.du' ask for a new code");
  }

  public void testSendMailInfr() throws Exception {
    checkMail("fr", "Suite a votre demande, veuillez trouver");
  }

  private void checkMail(String s, final String expected) throws IOException, InterruptedException {
    addUser("monPremierClient@pirate.du");
    PostMethod postMethod = sendRequest(s);
    Header header = postMethod.getResponseHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_SENT, header.getValue());
    String content = checkReceivedMail("monPremierClient@pirate.du");
    assertTrue(content, content.contains(expected));
  }


  private PostMethod sendRequest(String lang) throws IOException {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    postMethod.setRequestHeader(ConfigService.HEADER_MAIL, "monPremierClient@pirate.du");
    postMethod.setRequestHeader(ConfigService.HEADER_LANG, lang);
    client.executeMethod(postMethod);
    return postMethod;
  }

  public void testAddInDbIfBadAdress() throws Exception {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    String badMail = "monPremierClient@pirate";
    postMethod.setRequestHeader(ConfigService.HEADER_MAIL, badMail);
    postMethod.setRequestHeader(ConfigService.HEADER_LANG, "en");
    client.executeMethod(postMethod);
    Header header = postMethod.getResponseHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_BAD_ADRESS, header.getValue());
    SqlConnection connection = getSqlConnection();
    connection.getQueryBuilder(MailError.TYPE, Constraints.equal(MailError.MAIL, badMail))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void checkInBase(String mailTo) {
    SqlConnection connection = getSqlConnection();
    connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, mailTo))
      .getQuery().executeUnique();
    connection.commitAndClose();
  }

  private void addUser(String mail) {
    SqlConnection db = getSqlConnection();
    try {
      db.getCreateBuilder(License.TYPE)
        .set(License.MAIL, mail)
        .getRequest().run();
    }
    finally {
      db.commitAndClose();
    }
  }
}
