package org.designup.picsoulicence.functests;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsoulicence.MailTestCase;

public class AskMailTest extends MailTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    start();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stop();
  }

  public void testSendMail() throws Exception {
    HttpClient httpClient = new HttpClient();
    PostMethod postMethod = new PostMethod("http://localhost/mailTo");
    postMethod.setRequestHeader("mailto", "monPremierClient@pirate.du");
    httpClient.executeMethod(postMethod);
    postMethod.getResponseHeader("OK");
    checkReceive("monPremierClient@pirate.du");
  }
}
