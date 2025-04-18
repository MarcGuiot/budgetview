package com.budgetview.server.license.servlet;

import com.budgetview.server.license.generator.LicenseGenerator;
import com.budgetview.server.license.mail.Mailbox;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.utils.PaypalConstants;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.constraints.Where;
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
  public static final int LICENCE_COUNT = Integer.parseInt(System.getProperty("budgetview.licence.count", "3"));
  private GlobsDatabase db;
  private Mailer mailer;
  private HttpClient client;


  public NewUserServlet(Directory directory) {
    String url = System.getProperty(PaypalConstants.PAYPAL_CONFIRM_URL_PROPERTY);
    if (url != null) {
      PaypalConstants.PAYPAL_CONFIRM_URL = url;
    }
    db = directory.get(GlobsDatabase.class);
    mailer = directory.get(Mailer.class);
    client = new DefaultHttpClient(new PoolingClientConnectionManager());
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mail = "???";
    HttpPost postMethod = null;

    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      logger.info("receive new User  : ");
      mail = req.getParameter(PaypalConstants.PAYER_EMAIL);
      logger.info("NewUser : mail : '" + mail);

      String transactionId = "";
      String paymentStatus = "";
      String receiverEmail = "";
      String lang = null;
      URIBuilder builder = new URIBuilder(PaypalConstants.PAYPAL_CONFIRM_URL);
      builder.addParameter("cmd", "_notify-validate");
      Map<String, String[]> map = (Map<String, String[]>)req.getParameterMap();
      StringBuffer paramaters = new StringBuffer();
      for (Map.Entry<String, String[]> entry : map.entrySet()) {
        String key = entry.getKey();
        for (String name : entry.getValue()) {
          builder.setParameter(key, name);
          paramaters.append(key).append("='").append(req.getParameter(key))
            .append("'; ");
        }
        if (key.equalsIgnoreCase(PaypalConstants.TRANSACTION_ID)) {
          transactionId = req.getParameter(key);
        }
        else if (key.equalsIgnoreCase(PaypalConstants.PAYMENT_STATUS_ID)) {
          paymentStatus = req.getParameter(key);
        }
        else if (key.equalsIgnoreCase(PaypalConstants.RECEIVER_EMAIL)) {
          receiverEmail = req.getParameter(key);
        }
        else if (key.equalsIgnoreCase(PaypalConstants.CUSTOM)){
          String parameter = req.getParameter(PaypalConstants.CUSTOM);
          if ("1".equalsIgnoreCase(parameter)){
            lang = "fr";
          }
          else if ("2".equalsIgnoreCase(parameter)){
            lang = "en";
          }
        }
        else if (key.equalsIgnoreCase(PaypalConstants.MC_CURRENCY) && lang == null) {
          if (req.getParameter(PaypalConstants.MC_CURRENCY).equalsIgnoreCase("EUR")) {
            lang = "fr";
          }
          else {
            lang = "en";
          }
        }
      }
      logger.info(paramaters.toString());
      if (!receiverEmail.equalsIgnoreCase("paypal@budgetview.fr")) {
        logger.error("Invalid email : " + receiverEmail);
        return;
      }
      if (!paymentStatus.equalsIgnoreCase("Completed")) {
        logger.info("Status " + paymentStatus);
        return;
      }

      postMethod = new HttpPost(builder.build());
      HttpParams params = postMethod.getParams();
      params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
      params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");

      HttpResponse response = client.execute(postMethod);
      if (response.getStatusLine().getStatusCode() == 200) {
        InputStream responseBodyAsStream = response.getEntity().getContent();
        byte[] buffer = new byte[500];
        int readed = responseBodyAsStream.read(buffer);
        if (readed == -1) {
          logger.error("Paypal returned empty response");
          return;
        }
        String content = new String(buffer, 0, readed);
        if (content.equalsIgnoreCase("VERIFIED")) {
          logger.info("Email : '" + mail + "' VERIFIED");
          registerNewUser(resp, mail, transactionId, lang);
        }
        else {
          logger.error("NewUser : Paypal refused confirmation " + content);
          resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
        }
      }
      else {
        logger.error("Paypal refuse connection " + response.getStatusLine());
        resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      }
    }
    catch (Exception e) {
      logger.error("For newUser :  " + mail, e);
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
  }

  private void registerNewUser(HttpServletResponse resp, String mail, String transactionId, String lang) {
    SqlConnection connection = db.connect();
    try {
      register(resp, mail, transactionId, connection, lang);
    }
    catch (Exception e) {
      logger.error("NewUser : RegisterServlet:doPost", e);
      SqlConnection retryConnection = db.connect();
      try {
        register(resp, mail, transactionId, retryConnection, lang);
      }
      catch (Exception e1) {
        resp.setStatus(HttpServletResponse.SC_OK);
        if (retryConnection != null) {
          retryConnection.commitAndClose();
        }
      }
    }
    finally {
      if (connection != null) {
        connection.commitAndClose();
      }
    }
  }

  private void register(HttpServletResponse resp, String email, String transactionId, SqlConnection connection, String lang)
    throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    SqlSelect query = connection.startSelect(License.TYPE,
                                             Where.fieldEquals(License.MAIL, email))
      .selectAll()
      .getQuery();
    GlobList globList = query.getList();
    connection.commit();
    if (globList.isEmpty()) {
      String code = LicenseGenerator.generateActivationCode();
      byte[] signature = LicenseGenerator.generateSignature(email);
      SqlRequest sqlRequest = connection.startCreate(License.TYPE)
        .set(License.ACCESS_COUNT, 1L)
        .set(License.SIGNATURE, signature)
        .set(License.ACTIVATION_CODE, code)
        .set(License.MAIL, email)
        .set(License.TRANSACTION_ID, transactionId)
        .getRequest();
      for (int i = 0; i < LICENCE_COUNT; i++) {
        sqlRequest.execute();
      }
      sqlRequest.close();

      connection.commit();
      logger.info("NewUser : ok  for " + email + " code is " + code + " in " + lang);
      mailer.sendNewLicense(email, code, lang);
      mailer.sendToUs(Mailbox.ADMIN, email, "New User", " Licence code : " + code + "\nLang: " + lang);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    else {
      Glob glob = globList.get(0);
      String code = glob.get(License.ACTIVATION_CODE);
      if (code == null) {
        code = LicenseGenerator.generateActivationCode();
        connection.startUpdate(License.TYPE, Where.fieldEquals(License.MAIL, email))
          .set(License.ACTIVATION_CODE, code)
          .run();
      }
      String previousTrId = glob.get(License.TRANSACTION_ID);
      if (previousTrId != null && previousTrId.equals(transactionId)) {
        logger.info("NewUser : Receive transaction twice (resend code)");
      }
      else {
        String message = "NewUser : Receive different TransactionId for the same mail txId='" + transactionId +
                         "' previousTxId='" + previousTrId + "' for '" + email + "' lang " + lang;
        logger.error(message);
        mailer.sendToUs(Mailbox.ADMIN, email, "different TransactionId", message + "'. We should contact them to ask them for an other mail.");
      }
      mailer.sendNewLicense(email, code, lang);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }
}