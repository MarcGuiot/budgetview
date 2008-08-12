package org.designup.picsou.licence.mail;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsou.licence.LicenseTestCase;
import org.designup.picsou.licence.model.License;
import org.designup.picsou.licence.model.MailError;
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
    PostMethod postMethod = sendRequest();
    Header header = postMethod.getResponseHeader("status");
    assertEquals("mailSent", header.getValue());
    checkReceive("monPremierClient@pirate.du");
    checkInBase("monPremierClient@pirate.du");
  }

  private PostMethod sendRequest() throws IOException {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    postMethod.setRequestHeader("mailto", "monPremierClient@pirate.du");
    client.executeMethod(postMethod);
    return postMethod;
  }

  public void testResendIfAdresseAlreadyRegistered() throws Exception {
    sendRequest();
    PostMethod postMethod = sendRequest();
    Header header = postMethod.getResponseHeader("status");
    assertEquals("mailSent", header.getValue());
  }

  public void testAddInDbIfBadAdress() throws Exception {
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    String badMail = "monPremierClient@pirate";
    postMethod.setRequestHeader("mailto", badMail);
    client.executeMethod(postMethod);
    Header header = postMethod.getResponseHeader("status");
    assertEquals("badAdress", header.getValue());
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
}
