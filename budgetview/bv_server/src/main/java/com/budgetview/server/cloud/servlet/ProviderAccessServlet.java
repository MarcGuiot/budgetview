package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.model.Provider;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.budgetview.shared.json.Json.json;

public class ProviderAccessServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/provider/access");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;

  public ProviderAccessServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);
    String providerIdString = request.getHeader(CloudConstants.PROVIDER_ID);

    if (Strings.isNullOrEmpty(email)) {
      logger.error("No email provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      logger.error("No token provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(providerIdString)) {
      logger.error("No provider specified");
      setBadRequest(response);
      return;
    }

    Integer providerId;
    try {
      providerId = Integer.parseInt(providerIdString);
    }
    catch (NumberFormatException e) {
      logger.error("Provider '" + providerIdString + "' not recognized");
      setBadRequest(response);
      return;
    }

    Glob user;
    try {
      user = authentication.checkUserToken(email, bvToken);
    }
    catch (SubscriptionCheckFailed e) {
      setSubscriptionError(response, e);
      return;
    }
    if (user == null) {
      logger.error("Could not identify user with email:" + email);
      setUnauthorized(response);
      return;
    }

    boolean sameProvider = Utils.equal(user.get(CloudUser.PROVIDER), providerId);

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key("status").value(sameProvider ? "ok" : "not_recognized");
    writer.endObject();
    setOk(response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);
    String providerId = request.getHeader(CloudConstants.PROVIDER_ID);
    String budgeaToken = request.getHeader(CloudConstants.PROVIDER_TOKEN);
    String budgeaUserId = request.getHeader(CloudConstants.PROVIDER_USER_ID);

    if (Strings.isNullOrEmpty(email)) {
      logger.error("No email provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      logger.error("No token provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(providerId)) {
      logger.error("No provider specified");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(budgeaToken)) {
      logger.error("No token provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(budgeaUserId)) {
      logger.error("No userId provided");
      setBadRequest(response);
      return;
    }

    Glob user = null;
    try {
      user = authentication.checkUserToken(email, bvToken);
    }
    catch (SubscriptionCheckFailed e) {
      setSubscriptionError(response, e);
      return;
    }
    if (user == null) {
      logger.error("Could not identify user with email:" + email);
      setUnauthorized(response);
      return;
    }

    String newBudgeaToken;
    try {
      newBudgeaToken = registerPermanentBudgeaToken(budgeaToken);
    }
    catch (Exception e) {
      logger.error("Budgea registration failed - could not obtain permanent token", e);
      setInternalError(response);
      return;
    }

    try {
      saveProviderAccess(user.get(CloudUser.ID), providerId, budgeaUserId, newBudgeaToken);
    }
    catch (GlobsSQLException e) {
      logger.error("Could not store user '" + email + "' in dabase", e);
      setInternalError(response);
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

  private void saveProviderAccess(Integer userId, String providerId, String providerUserId, String providerAccessToken) throws GlobsSQLException {
    SqlConnection connection = database.connect();
    connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
      .set(CloudUser.PROVIDER, Integer.parseInt(providerId))
      .set(CloudUser.PROVIDER_USER_ID, Integer.parseInt(providerUserId))
      .set(CloudUser.PROVIDER_ACCESS_TOKEN, providerAccessToken)
      .run();
    connection.commitAndClose();
    logger.info("Saved connection for userId " + providerUserId + " with token " + providerAccessToken);
  }
}
