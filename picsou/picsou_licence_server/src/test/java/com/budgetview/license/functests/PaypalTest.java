package com.budgetview.license.functests;

import com.budgetview.license.model.License;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import com.budgetview.gui.config.ConfigService;
import com.budgetview.license.ConnectedTestCase;
import com.budgetview.license.servlet.LicenseServer;
import com.budgetview.license.servlet.NewUserServlet;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.mortbay.jetty.servlet.ServletHolder;

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

    SERVER_URL = System.getProperty(ConfigService.COM_APP_LICENSE_URL);
    System.setProperty(NewUserServlet.PAYPAL_CONFIRM_URL_PROPERTY, SERVER_URL + "/Confirm");
    licenseServer.init();
    licenseServer.add(new ServletHolder(payPalConfirm), "/Confirm");
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

  private void doBuy(final String transactionId, boolean checkDb) throws IOException, InterruptedException, URISyntaxException {
    HttpClient client = new DefaultHttpClient();
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseServer.NEW_USER);

    builder.setParameter(NewUserServlet.PAYER_EMAIL, "toto" + transactionId + "@bv.fr");
    builder.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    builder.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(NewUserServlet.TRANSACTION_ID, transactionId);
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
      GlobList glob = db.getConnection().getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto" + transactionId + "@bv.fr"))
        .selectAll()
        .getQuery().executeAsGlobs();
      assertFalse(glob.isEmpty());
      assertEquals(3, glob.size());
      String code = glob.get(0).get(License.ACTIVATION_CODE);
      assertEquals(glob.get(0).get(License.TRANSACTION_ID), transactionId);
      mailServer.checkReceivedMail("toto" + transactionId + "@bv.fr").checkContains(code);
      mailServer.checkReceivedMail("admin@mybudgetview.fr").checkContains("toto" + transactionId + "@bv.fr");
    }
    postMethod.releaseConnection();
  }

  public void testNoValidated() throws Exception {
    HttpClient client = new DefaultHttpClient();
//    postMethod.getParams().setContentCharset("UTF-8");
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseServer.NEW_USER);
    builder.setParameter(NewUserServlet.PAYER_EMAIL, "toto@bv.fr");
    builder.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    builder.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(NewUserServlet.TRANSACTION_ID, "12345");
    HttpPost postMethod = new HttpPost(builder.build());
    HttpParams params = postMethod.getParams();
    payPalConfirm.setRefused();
    HttpResponse response = client.execute(postMethod);
    int status = response.getStatusLine().getStatusCode();
    assertEquals(412, status);
    GlobList glob =
      db.getConnection().getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto@bv.fr"))
        .selectAll()
        .getQuery().executeAsGlobs();
    assertTrue(glob.isEmpty());
  }

  public void testMultipleBuy() throws Exception {
    doBuy("12345", true);
    URIBuilder builder = new URIBuilder(SERVER_URL + LicenseServer.NEW_USER);
    HttpClient client = new DefaultHttpClient();
//    postMethod.getParams().setContentCharset("UTF-8");
    builder.setParameter(NewUserServlet.PAYER_EMAIL, "toto12345@bv.fr");
    builder.setParameter(NewUserServlet.RECEIVER_EMAIL, "paypal@mybudgetview.fr");
    builder.setParameter(NewUserServlet.PAYMENT_STATUS_ID, "completed");
    builder.setParameter(NewUserServlet.TRANSACTION_ID, "12346");
    HttpPost postMethod = new HttpPost(builder.build());
    HttpResponse response = client.execute(postMethod);
    int status = response.getStatusLine().getStatusCode();
    assertEquals(200, status);
    GlobList globs =
      db.getConnection().getQueryBuilder(License.TYPE, Constraints.equal(License.MAIL, "toto12345@bv.fr"))
        .selectAll()
        .getQuery().executeAsGlobs();
    assertEquals(3, globs.size());
    String code = globs.get(0).get(License.ACTIVATION_CODE);
    mailServer.checkReceivedMail("admin@mybudgetview.fr");
    mailServer.checkReceivedMail("toto12345@bv.fr").checkContains(code);
    assertEquals(globs.get(0).get(License.TRANSACTION_ID), "12345");
  }

  class PayPalConfirm extends HttpServlet {
    private static final String VERIFIED = "VERIFIED";
    private static final String NOT_VENRIFIED = "NOT_VERIFIED";
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
      STATUS = NOT_VENRIFIED;
    }

    public void checkMail(String name) {
      if (!mail.equals(name)) {
        fail(name + " expected got " + mail);
      }
    }
  }
}
