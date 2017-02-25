package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.server.cloud.model.CloudDatabaseModel;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.license.model.License;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.http.Http;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class CloudChecker {

  private CloudServer cloudServer;

  public static final String CLOUD_SUBSCRIPTION_BACKDOOR = "/cloudSubscriptionBackdoor";

  public void startServer(final PaymentChecker payments) throws Exception {
    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties") {
      protected Directory createDirectory() throws Exception {
        Directory directory = new DefaultDirectory(super.createDirectory());
        payments.install(directory);
        return directory;
      }
    };
    cloudServer.init();
    cloudServer.addServlet(new CloudSubscriptionBackdoorServlet(cloudServer.getDirectory()), CLOUD_SUBSCRIPTION_BACKDOOR);
    cloudServer.start();
  }

  public void forceTokenExpirationDate(final Date date) {
    EmailValidationService.forceTokenExpirationDate(date);
  }

  public void cleanUpDatabase() {
    GlobsDatabase db = cloudServer.getDirectory().get(GlobsDatabase.class);
    SqlConnection connection = db.connect();
    for (GlobType type : CloudDatabaseModel.getAllTypes()) {
      connection.startDelete(type).execute();
    }
    connection.commit();
  }

  public void stopServer() throws Exception {
    cloudServer.resetDatabase();
    cloudServer.stop();
  }

  public Date createSubscription(String email, Date expirationDate) throws IOException {
    String url = CloudConstants.getServerUrl(CLOUD_SUBSCRIPTION_BACKDOOR);
    Http.execute(url, Request.Post(url)
      .addHeader(CloudConstants.CLOUD_EMAIL, email)
      .addHeader(CloudConstants.CLOUD_END_DATE, JsonGlobFormat.toString(expirationDate))
    );
    return expirationDate;
  }

  public static class CloudSubscriptionBackdoorServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("CloudSubscriptionBackdoorServlet");
    private final GlobsDatabase db;

    public CloudSubscriptionBackdoorServlet(Directory directory) {
      db = directory.get(GlobsDatabase.class);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

      logger.info("POST");

      String email = request.getHeader(CloudConstants.CLOUD_EMAIL);
      if (Strings.isNullOrEmpty(email)) {
        logger.info("Missing email parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      String date = request.getHeader(CloudConstants.CLOUD_END_DATE);
      if (Strings.isNullOrEmpty(date)) {
        logger.info("Missing date parameter");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      SqlConnection connection = db.connect();
      try {

        GlobList users = connection.selectAll(CloudUser.TYPE, Where.fieldEquals(CloudUser.EMAIL, email));
        if (users.isEmpty()) {
          connection.startCreate(CloudUser.TYPE)
            .set(CloudUser.EMAIL, email)
            .set(CloudUser.SUBSCRIPTION_END_DATE, JsonGlobFormat.parseDate(date))
            .run();
        }
        else {
          connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.EMAIL, email))
            .set(CloudUser.SUBSCRIPTION_END_DATE, JsonGlobFormat.parseDate(date))
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
