package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.license.LicenseServer;
import com.budgetview.server.license.model.License;
import com.budgetview.shared.http.Http;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class CloudLicenseChecker {

  private LicenseServer licenseServer;
  public static final String CLOUD_SUBSCRIPTION_BACKDOOR = "/cloudSubscriptionBackdoor";

  public void startServer() throws Exception {
    licenseServer = new LicenseServer("budgetview/bv_server/dev/config/bv_license_test.properties");
    licenseServer.init();
    licenseServer.addServlet(new CloudSubscriptionBackdoorServlet(licenseServer.getDirectory()), CLOUD_SUBSCRIPTION_BACKDOOR);
    licenseServer.start();
  }

  public void purchaseLicence(String email, Date expirationDate) throws IOException {
    String url = LicenseConstants.getServerUrl(CLOUD_SUBSCRIPTION_BACKDOOR);
    Http.execute(url, Request.Post(url)
                   .addHeader(LicenseConstants.CLOUD_EMAIL, email)
                   .addHeader(LicenseConstants.CLOUD_END_DATE, JsonGlobFormat.toString(expirationDate))
    );
  }

  public void stopServer() throws Exception {
    licenseServer.stop();
    licenseServer = null;
  }

  public static class CloudSubscriptionBackdoorServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("CloudSubscriptionBackdoorServlet");
    private final GlobsDatabase db;

    public CloudSubscriptionBackdoorServlet(Directory directory) {
      db = directory.get(GlobsDatabase.class);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

      logger.info("POST");

      String email = request.getHeader(LicenseConstants.CLOUD_EMAIL);
      if (Strings.isNullOrEmpty(email)) {
        logger.info("Missing email parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      String date = request.getHeader(LicenseConstants.CLOUD_END_DATE);
      if (Strings.isNullOrEmpty(date)) {
        logger.info("Missing date parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      SqlConnection connection = db.connect();
      try {

        GlobList licenses = connection.selectAll(License.TYPE, Where.fieldEquals(License.MAIL, email));
        if (licenses.isEmpty()) {
          connection.startCreate(License.TYPE)
            .set(License.MAIL, email)
            .set(License.CLOUD_LICENSE_EXPIRATION_DATE, JsonGlobFormat.parseDate(date))
            .run();
        }
        else {
          connection.startUpdate(License.TYPE, Where.fieldEquals(License.MAIL, email))
            .set(License.CLOUD_LICENSE_EXPIRATION_DATE, JsonGlobFormat.parseDate(date))
            .run();
        }

        logger.info("Subscription expiration date set to " + date + " for " + email);

        response.setStatus(HttpServletResponse.SC_OK);
      }
      catch (ParseException e) {
        logger.error("Error setting " + email + " to " + date, e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
      finally {
        connection.commitAndClose();
      }
    }

  }
}
