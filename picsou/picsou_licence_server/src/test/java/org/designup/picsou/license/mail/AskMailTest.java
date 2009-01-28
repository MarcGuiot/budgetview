package org.designup.picsou.license.mail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.LicenseTestCase;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;

import java.io.IOException;

public class AskMailTest extends LicenseTestCase {
  private HttpClient client;

  protected void setUp() throws Exception {
    super.setUp();
    start();
    client = new HttpClient();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stop();
    client = null;
  }

  public void testSendMail() throws Exception {
    addUser("monPremierClient@pirate.du");
    PostMethod postMethod = sendRequest();
    Header header = postMethod.getResponseHeader(ConfigService.HEADER_STATUS);
    assertEquals(ConfigService.HEADER_MAIL_SENT, header.getValue());
    checkReceive("monPremierClient@pirate.du");
  }

  private PostMethod sendRequest() throws IOException {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    postMethod.setRequestHeader(ConfigService.HEADER_MAIL, "monPremierClient@pirate.du");
    postMethod.setRequestHeader(ConfigService.HEADER_LANG, "en");
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
