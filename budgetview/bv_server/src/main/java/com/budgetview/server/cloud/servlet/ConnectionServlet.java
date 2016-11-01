package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
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
  private final AuthenticationService authentication;

  public ConnectionServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);
    String budgeaToken = request.getHeader(CloudConstants.BUDGEA_TOKEN);
    String budgeaUserId = request.getHeader(CloudConstants.BUDGEA_USER_ID);

    if (Strings.isNullOrEmpty(email)) {
      logger.error("No email provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      logger.error("No token provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(budgeaToken)) {
      logger.error("No token provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(budgeaUserId)) {
      logger.error("No userId provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Integer userId = authentication.checkUserToken(email, bvToken);
    if (userId == null) {
      logger.error("Could not identify user with email:" + email);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String newBudgeaToken;
    try {
      newBudgeaToken = registerPermanentBudgeaToken(budgeaToken);
    }
    catch (Exception e) {
      logger.error("Budgea registration failed", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      saveCloudConnection(userId, budgeaUserId, newBudgeaToken);
    }
    catch (GlobsSQLException e) {
      logger.error("Could not store user '" + email + "' in dabase", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private String registerPermanentBudgeaToken(String tempBudgeaToken) throws IOException {
    String serverUrl = BudgeaConstants.getServerUrl("/auth/token/access");
    Request request = Request.Post(serverUrl)
      .bodyForm(Form.form()
                  .add("code", tempBudgeaToken)
                  .add("client_id", Budgea.CLIENT_ID)
                  .add("client_secret", Budgea.CLIENT_SECRET)
                  .build());

    return json(request.execute()).getString("access_token");
  }

  private void saveCloudConnection(Integer userId, String providerUserId, String providerAccessToken) throws GlobsSQLException {
    SqlConnection connection = database.connect();
    connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
      .set(CloudUser.PROVIDER, Provider.BUDGEA.getId())
      .set(CloudUser.PROVIDER_ID, Integer.parseInt(providerUserId))
      .set(CloudUser.PROVIDER_ACCESS_TOKEN, providerAccessToken)
      .run();
    connection.commitAndClose();
    logger.info("Saved connection for userId " + providerUserId + " with token " + providerAccessToken);
  }
}
