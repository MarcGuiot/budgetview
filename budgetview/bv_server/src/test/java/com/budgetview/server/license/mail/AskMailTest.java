package com.budgetview.server.license.mail;

import com.budgetview.server.license.checkers.Email;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import com.budgetview.server.license.ConnectedTestCase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
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
    Header header = response.getFirstHeader(LicenseConstants.HEADER_STATUS);
    assertEquals(LicenseConstants.HEADER_MAIL_SENT, header.getValue());
    Email email = mailServer.checkReceivedMail("monPremierClient@pirate.du");
    for (String content : Utils.join(expected, nextExpected)) {
      email.checkContains(content);
    }
  }

  private HttpResponse sendRequest(String lang) throws IOException {
    HttpPost postMethod = new HttpPost("http://localhost:" + httpPort + "/mailTo");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    postMethod.setHeader(LicenseConstants.HEADER_MAIL_FROM, "monPremierClient@pirate.du");
    postMethod.setHeader(MobileConstants.HEADER_LANG, lang);
    return client.execute(postMethod);
  }

  public void testAddInDbIfBadAdress() throws Exception {
    HttpPost postMethod = new HttpPost("http://localhost:" + httpPort + "/mailTo");
    String badMail = "monPremierClient@pirate";
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    postMethod.setHeader(LicenseConstants.HEADER_MAIL_FROM, badMail);
    postMethod.setHeader(MobileConstants.HEADER_LANG, "en");
    HttpResponse response = client.execute(postMethod);
    Header header = response.getFirstHeader(LicenseConstants.HEADER_STATUS);
    assertEquals(LicenseConstants.HEADER_MAIL_UNKNOWN, header.getValue());
    SqlConnection connection = db.getConnection();
    connection.selectUnique(MailError.TYPE, Where.fieldEquals(MailError.MAIL, badMail));
    connection.commitAndClose();
  }

  private void addUser(String mail) {
    SqlConnection connection = db.getConnection();
    try {
      connection.startCreate(License.TYPE)
        .set(License.MAIL, mail)
        .run();
    }
    finally {
      connection.commitAndClose();
    }
  }
}
