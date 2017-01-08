package com.budgetview.server.license.servlet;

import com.budgetview.server.license.model.License;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class CloudSubscriptionEndDateServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(LicenseConstants.CLOUD_SUBSCRIPTION_END_DATE);
  private final GlobsDatabase db;

  public CloudSubscriptionEndDateServlet(Directory directory) {
    db = directory.get(GlobsDatabase.class);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    logger.info("GET");

    String email = request.getHeader(LicenseConstants.CLOUD_EMAIL);
    if (Strings.isNullOrEmpty(email)) {
      logger.info("Missing email parameter");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    logger.info("Get for " + email);

    SqlConnection connection = db.connect();
    try {
      GlobList licenses = connection.startSelect(License.TYPE, Where.fieldEquals(License.MAIL, email.trim()))
        .selectAll()
        .getList();

      Date result = null;
      for (Glob license : licenses) {
        Date date = license.get(License.CLOUD_LICENSE_EXPIRATION_DATE);
        if (date != null && (result == null || date.after(result))) {
          result = date;
        }
      }

      response.setStatus(HttpServletResponse.SC_OK);

      String endDate = result == null ? null : JsonGlobFormat.toString(result);
      JSONWriter writer = new JSONWriter(response.getWriter());
      writer.object();
      writer.key(LicenseConstants.CLOUD_END_DATE).value(endDate);
      writer.endObject();
      logger.info("Returning " + endDate);
    }
    catch (Exception e) {
      logger.error("Exception when retrieving end date for " + email, e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    finally {
      connection.commitAndClose();
    }
  }

}
