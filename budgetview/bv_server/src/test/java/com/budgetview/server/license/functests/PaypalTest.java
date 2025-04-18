package com.budgetview.server.license.functests;

import com.budgetview.server.license.model.License;
import com.budgetview.server.license.utils.PaypalConstants;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import com.budgetview.server.license.ConnectedTestCase;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Where;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PaypalTest extends ConnectedTestCase {
  private static String SERVER_URL;
  private static final String RAPHAËL = "Raphaël";
  private static final String PARAMETER_WITH_ACCENT = "parameterWithAccent";
  private PayPalConfirm payPalConfirm = new PayPalConfirm();

  protected void setUp() throws Exception {
    super.setUp();

    SERVER_URL = System.getProperty(LicenseConstants.LICENSE_URL_PROPERTY);
    System.setProperty(PaypalConstants.PAYPAL_CONFIRM_URL_PROPERTY, SERVER_URL + "/Confirm");
    licenseServer.init();
    licenseServer.add(payPalConfirm, "/Confirm");
    startServersWithoutLicence();
  }

  public void test() throws Exception {
    doBuy("12345", true);
  }

  public void testParallelCall() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future> futures = new ArrayList<Future>();
    for (int i = 0; i < 10; i++) {
      final int finalI = i;
      Future<Object> submit = executor.submit(new Callable<Object>() {
        public Object call() throws Exception {
          doBuy("123" + finalI, false);
          return null;
        }
      });
      futures.add(submit);
    }

    for (Future future : futures) {
      future.get();
    }
  }

  private void doBuy(final String transactionId, boolean checkDb) throws Exception {
    HttpClient client = new DefaultHttpClient();
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseConstants.NEW_USER);

    builder.setParameter(PaypalConstants.PAYER_EMAIL, "toto" + transactionId + "@bv.fr");
    builder.setParameter(PaypalConstants.RECEIVER_EMAIL, "paypal@budgetview.fr");
    builder.setParameter(PaypalConstants.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(PaypalConstants.TRANSACTION_ID, transactionId);
    builder.setParameter(PARAMETER_WITH_ACCENT, RAPHAËL);

    HttpPost postMethod = new HttpPost(builder.build());
    HttpParams params = postMethod.getParams();
    params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    payPalConfirm.setVerified();
    HttpResponse response = client.execute(postMethod);
    int status = response.getStatusLine().getStatusCode();
    postMethod.releaseConnection();
    assertEquals(200, status);
    payPalConfirm.checkMail(RAPHAËL);
    if (checkDb) {
      GlobList glob = db.getConnection().selectAll(License.TYPE, Where.fieldEquals(License.MAIL, "toto" + transactionId + "@bv.fr"));
      assertEquals(3, glob.size());
      String code = glob.get(0).get(License.ACTIVATION_CODE);
      assertEquals(glob.get(0).get(License.TRANSACTION_ID), transactionId);
      mailServer.checkReceivedMail("toto" + transactionId + "@bv.fr").checkContainsAll(code);
      mailServer.checkReceivedMail("admin@budgetview.fr").checkContainsAll("toto" + transactionId + "@bv.fr");
    }
    postMethod.releaseConnection();
  }

  public void testNotValidated() throws Exception {
    HttpClient client = new DefaultHttpClient();
//    postMethod.getParams().setContentCharset("UTF-8");
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseConstants.NEW_USER);
    builder.setParameter(PaypalConstants.PAYER_EMAIL, "toto@bv.fr");
    builder.setParameter(PaypalConstants.RECEIVER_EMAIL, "paypal@budgetview.fr");
    builder.setParameter(PaypalConstants.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(PaypalConstants.TRANSACTION_ID, "12345");
    HttpPost postMethod = new HttpPost(builder.build());
    HttpParams params = postMethod.getParams();
    payPalConfirm.setRefused();
    HttpResponse response = client.execute(postMethod);
    int status = response.getStatusLine().getStatusCode();
    assertEquals(412, status);
    GlobList globsList =
      db.getConnection().selectAll(License.TYPE, Where.fieldEquals(License.MAIL, "toto@bv.fr"));
    assertTrue(globsList.isEmpty());
  }

  public void testMultipleBuy() throws Exception {
    doBuy("12345", true);
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseConstants.NEW_USER);
    HttpClient client = new DefaultHttpClient();
//    postMethod.getParams().setContentCharset("UTF-8");
    builder.setParameter(PaypalConstants.PAYER_EMAIL, "toto12345@bv.fr");
    builder.setParameter(PaypalConstants.RECEIVER_EMAIL, "paypal@budgetview.fr");
    builder.setParameter(PaypalConstants.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(PaypalConstants.TRANSACTION_ID, "12346");
    HttpPost postMethod = new HttpPost(builder.build());
    HttpResponse response = client.execute(postMethod);
    int status = response.getStatusLine().getStatusCode();
    assertEquals(200, status);
    GlobList globs =
      db.getConnection().selectAll(License.TYPE, Where.fieldEquals(License.MAIL, "toto12345@bv.fr"));
    assertEquals(3, globs.size());
    String code = globs.get(0).get(License.ACTIVATION_CODE);
    mailServer.checkReceivedMail("admin@budgetview.fr");
    mailServer.checkReceivedMail("toto12345@bv.fr").checkContainsAll(code);
    assertEquals(globs.get(0).get(License.TRANSACTION_ID), "12345");
  }

  class PayPalConfirm extends HttpServlet {
    private static final String VERIFIED = "VERIFIED";
    private static final String NOT_VERIFIED = "NOT_VERIFIED";
    String mail;
    String STATUS = VERIFIED;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      resp.getWriter().append(STATUS);
      mail = req.getParameter(PARAMETER_WITH_ACCENT);
      resp.setStatus(HttpServletResponse.SC_OK);
    }

    public void setVerified() {
      STATUS = VERIFIED;
    }

    public void setRefused() {
      STATUS = NOT_VERIFIED;
    }

    public void checkMail(String name) {
      if (!mail.equals(name)) {
        fail(name + " expected got " + mail);
      }
    }
  }
}
