package org.designup.picsou.license.servlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.designup.picsou.license.generator.LicenseGenerator;
import org.designup.picsou.license.mail.Mailer;
import org.designup.picsou.license.model.License;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class NewUserServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("NewUserServlet");
  public static final String PAYPAL_CONFIRM_URL_PROPERTY = "PAYPAL_CONFIRM_URL";
  //  private static String PAYPAL_CONFIRM_URL = "http://www.sandbox.paypal.com/fr/cgi-bin/webscr";
  private static String PAYPAL_CONFIRM_URL = "http://www.paypal.com/fr/cgi-bin/webscr";
  private SqlService sqlService;
  private Mailer mailer;
  public static final String PAYER_EMAIL = "payer_email";
  public static final String TRANSACTION_ID = "txn_id";
  public static final String PAYMENT_STATUS_ID = "payment_status";
  public static final String RECEIVER_EMAIL = "receiver_email";
  private HttpClient client;


  public NewUserServlet(Directory directory) {
    String url = System.getProperty(PAYPAL_CONFIRM_URL_PROPERTY);
    if (url != null) {
      PAYPAL_CONFIRM_URL = url;
    }
    sqlService = directory.get(SqlService.class);
    mailer = directory.get(Mailer.class);
    client = new HttpClient();
    client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = "???";
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      logger.info("receive new User  : ");
      mail = req.getParameter(PAYER_EMAIL);
      logger.info("NewUser : mail : '" + mail);

      String transactionId = "";
      String paymentStatus = "";
      String receiverEmail = "";
      PostMethod postMethod = new PostMethod(PAYPAL_CONFIRM_URL);
      postMethod.getParams().setContentCharset("UTF-8");
      postMethod.setParameter("cmd", "_notify-validate");
      Map<String, String[]> map = (Map<String, String[]>)req.getParameterMap();
      StringBuffer paramaters = new StringBuffer();
      for (Map.Entry<String, String[]> entry : map.entrySet()) {
        String key = entry.getKey();
        for (String name : entry.getValue()) {
          postMethod.setParameter(key, name);
          paramaters.append(key).append("='").append(req.getParameter(key))
            .append("'; ");
        }
        if (key.equalsIgnoreCase(TRANSACTION_ID)) {
          transactionId = req.getParameter(key);
        }
        else if (key.equalsIgnoreCase(PAYMENT_STATUS_ID)) {
          paymentStatus = req.getParameter(key);
        }
        else if (key.equalsIgnoreCase(RECEIVER_EMAIL)) {
          receiverEmail = req.getParameter(key);
        }
      }
      logger.info(paramaters.toString());
      if (!receiverEmail.equalsIgnoreCase("paypal@mybudgetview.fr")) {
        logger.error("NewUser : Bad mail : " + receiverEmail);
        return;
      }
      if (!paymentStatus.equalsIgnoreCase("Completed")) {
        logger.info("NewUser : status " + paymentStatus);
        return;
      }
      int result = client.executeMethod(postMethod);
      if (result == 200) {
        InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
        byte[] buffer = new byte[500];
        int readed = responseBodyAsStream.read(buffer);
        if (readed == -1) {
          logger.error("NewUser : Paypal empty response");
          return;
        }
        String content = new String(buffer, 0, readed);
        if (content.equalsIgnoreCase("VERIFIED")) {
          logger.info("NewUser : mail : '" + mail + " VERIFIED");
          SqlConnection db = sqlService.getDb();
          try {
            register(resp, mail, transactionId, sqlService.getDb());
          }
          catch (Exception e) {
            logger.error("NewUser : RegisterServlet:doPost", e);
            SqlConnection db2 = sqlService.getDb();
            try {
              register(resp, mail, transactionId, db2);
            }
            catch (Exception e1) {
              resp.setStatus(HttpServletResponse.SC_OK);
              if (db2 != null) {
                db2.commitAndClose();
              }
            }
          }
          finally {
            if (db != null) {
              db.commitAndClose();
            }
          }
        }
        else {
          logger.error("NewUser : Paypal refuse confirmation " + content);
          resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
        }
      }
      else {
        logger.error("NewUser : Paypal refuse connection " + result);
        resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      }
    }
    catch (Exception e) {
      logger.error("For newUser :  " + mail, e);
    }
  }

  private void register(HttpServletResponse resp, String mail, String transactionId, SqlConnection db)
    throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    SelectQuery query = db.getQueryBuilder(License.TYPE,
                                           Constraints.equal(License.MAIL, mail))
      .selectAll()
      .getQuery();
    GlobList globList = query.executeAsGlobs();
    db.commit();
    if (globList.isEmpty()) {
      String code = LicenseGenerator.generateActivationCode();
      byte[] signature = LicenseGenerator.generateSignature(mail);
      db.getCreateBuilder(License.TYPE)
        .set(License.ACCESS_COUNT, 1L)
        .set(License.SIGNATURE, signature)
        .set(License.ACTIVATION_CODE, code)
        .set(License.MAIL, mail)
        .set(License.TRANSACTION_ID, transactionId)
        .getRequest()
        .run();
      db.commit();
      logger.info("NewUser : ok  for " + mail + " code is " + code);
      mailer.sendNewLicense(mail, code, "fr");
      mailer.sendToSupport(mail, "New User", " Licence code : " + code);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    else {
      Glob glob = globList.get(0);
      String code = glob.get(License.ACTIVATION_CODE);
      if (code == null) {
        code = LicenseGenerator.generateActivationCode();
        db.getUpdateBuilder(License.TYPE, Constraints.equal(License.MAIL, mail))
          .update(License.ACTIVATION_CODE, code)
          .getRequest()
          .run();
      }
      String previousTrId = glob.get(License.TRANSACTION_ID);
      if (previousTrId != null && previousTrId.equals(transactionId)) {
        logger.info("NewUser : Receive transaction twice (resend code)");
      }
      else {
        String message = "NewUser : Receive different TransactionId for the same mail txId='" + transactionId +
                         "' previousTxId='" + previousTrId + "' for '" + mail + "'";
        logger.error(message);
        mailer.sendToSupport(mail, "different TransactionId", message + "'. We should contact them to ask them for an other mail.");
      }
      mailer.sendNewLicense(mail, code, "fr");
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }
}