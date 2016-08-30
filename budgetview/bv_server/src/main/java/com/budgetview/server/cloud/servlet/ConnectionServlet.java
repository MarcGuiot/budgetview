package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.budgetview.shared.json.Json.json;

public class ConnectionServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/connections");

  private final GlobsDatabase database;

  public ConnectionServlet(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String token = request.getHeader(CloudConstants.BUDGEA_TOKEN);
    String userId = request.getHeader(CloudConstants.BUDGEA_USER_ID);

    if (Strings.isNullOrEmpty(email)) {
      logger.error("No email provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(token)) {
      logger.error("No token provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(userId)) {
      logger.error("No userId provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String newToken;
    try {
      newToken = registerBudgeaToken(token);
    }
    catch (Exception e) {
      logger.error("Budgea registration failed", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      saveCloudUser(email, userId, newToken);
    }
    catch (GlobsSQLException e) {
      logger.error("Could not store user '" + email + "' in dabase", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private String registerBudgeaToken(String code) throws IOException {
    String serverUrl = BudgeaConstants.getServerUrl("/auth/token/access");
    Request request = Request.Post(serverUrl)
      .bodyForm(Form.form()
                  .add("code", code)
                  .add("client_id", Budgea.CLIENT_ID)
                  .add("client_secret", Budgea.CLIENT_SECRET)
                  .build());

    return json(request.execute()).getString("access_token");
  }

  private void saveCloudUser(String email, String userId, String token) throws GlobsSQLException {
    SqlConnection connection = database.connect();
    connection.startCreate(CloudUser.TYPE)
      .set(CloudUser.EMAIL, email)
      .set(CloudUser.PROVIDER, Provider.BUDGEA.getId())
      .set(CloudUser.PROVIDER_ID, Integer.parseInt(userId))
      .set(CloudUser.PROVIDER_ACCESS_TOKEN, token)
      .run();
    connection.commitAndClose();
    logger.info("Saved " + email + " with userId " + userId + " and token " + token);
  }
}
