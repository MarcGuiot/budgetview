package org.designup.picsou.license.functests;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.LicenseTestCase;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.servlet.LicenseServer;
import org.designup.picsou.license.servlet.NewUserServlet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.mortbay.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PaypalTest extends LicenseTestCase {
  private static String SERVER_URL;

  protected void setUp() throws Exception {
    super.setUp();

    SERVER_URL = System.getProperty(ConfigService.COM_APP_LICENSE_URL);
    System.setProperty(NewUserServlet.PAYPAL_CONFIRM_URL_PROPERTY, SERVER_URL + "/Confirm");
    licenseServer.init();
    licenseServer.add(new ServletHolder(new PayPalConfirm()), "/Confirm");
    startServers();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stop();
  }

  public void test() throws Exception {
    doBuy("12345");
  }

  private void doBuy(final String transactionId) throws IOException, InterruptedException {
    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(SERVER_URL + LicenseServer.NEW_USER);
    postMethod.setParameter(NewUserServlet.PAYER_EMAIL, "toto@bv.fr");
    postMethod.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    postMethod.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    postMethod.setParameter(NewUserServlet.TRANSACTION_ID, transactionId);
    PayPalConfirm.STATUS = PayPalConfirm.VERIFIED;
    int status = client.executeMethod(postMethod);
    assertEquals(200, status);
    SqlConnection connection = getSqlConnection();
    Glob glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto@bv.fr"))
      .selectAll()
      .getQuery().executeUnique();
    assertNotNull(glob);
    String code = glob.get(License.ACTIVATION_CODE);
    String content = checkReceivedMail("toto@bv.fr");
    assertTrue(content.contains(code));
    assertEquals(glob.get(License.TRANSACTION_ID), transactionId);
    checkReceivedMail("support@mybudgetview.fr");
    assertTrue(content.contains("toto@bv.fr"));
  }

  public void testNoValidated() throws Exception {
    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(SERVER_URL + LicenseServer.NEW_USER);
    postMethod.setParameter(NewUserServlet.PAYER_EMAIL, "toto@bv.fr");
    postMethod.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    postMethod.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    postMethod.setParameter(NewUserServlet.TRANSACTION_ID, "12345");
    PayPalConfirm.STATUS = PayPalConfirm.NOT_VENRIFIED;
    int status = client.executeMethod(postMethod);
    assertEquals(412, status);
    SqlConnection connection = getSqlConnection();
    GlobList glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto@bv.fr"))
      .selectAll()
      .getQuery().executeAsGlobs();
    assertTrue(glob.isEmpty());
  }

  public void testMultipleBuy() throws Exception {
    doBuy("12345");
    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(SERVER_URL + LicenseServer.NEW_USER);
    postMethod.setParameter(NewUserServlet.PAYER_EMAIL, "toto@bv.fr");
    postMethod.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    postMethod.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    postMethod.setParameter(NewUserServlet.TRANSACTION_ID, "12346");
    int status = client.executeMethod(postMethod);
    assertEquals(200, status);
    SqlConnection connection = getSqlConnection();
    Glob glob = connection.getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto@bv.fr"))
      .selectAll()
      .getQuery().executeUnique();
    assertNotNull(glob);
    String code = glob.get(License.ACTIVATION_CODE);
    checkReceivedMail("support@mybudgetview.fr");
    String content = checkReceivedMail("toto@bv.fr");
    assertTrue(content.contains(code));
    assertEquals(glob.get(License.TRANSACTION_ID), "12345");
  }

  static class PayPalConfirm extends HttpServlet {
    private static final String VERIFIED = "VERIFIED";
    private static final String NOT_VENRIFIED = "NOT_VERIFIED";
    static String STATUS = VERIFIED;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.getWriter().append(STATUS);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }
}
