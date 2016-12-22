package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
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
import org.globsframework.utils.directory.Directory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class ConnectionServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/connections");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;

  public ConnectionServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);

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

    BudgeaAPI api = new BudgeaAPI();
    api.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN), true);
    JSONObject connections = api.getUserConnections(user.get(CloudUser.PROVIDER_ID));

    Map<Integer, String> bankNames = getBankNames(api);

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key("connections");
    writer.array();
    for (Object c : connections.getJSONArray("connections")) {
      JSONObject connection = (JSONObject)c;
      writer.object();
      writer.key(CloudConstants.PROVIDER).value(Integer.toString(Provider.BUDGEA.getId()));
      writer.key(CloudConstants.PROVIDER_ID).value(connection.getInt("id"));
      writer.key(CloudConstants.NAME).value(getName(connection, bankNames));
      writer.endObject();
    }
    writer.endArray();
    writer.endObject();

    setOk(response, writer);
  }

  protected String getName(JSONObject connection, Map<Integer, String> bankNames) {
    int bankId = connection.optInt("bank_id");
    String name = bankNames.get(bankId);
    if (Strings.isNullOrEmpty(name)) {
      name = "-?-";
      logger.error("/Get - no bank found fon bank_id " + bankId);
    }
    return name;
  }

  private Map<Integer,String> getBankNames(BudgeaAPI api) throws IOException {
    Map<Integer, String> result = new HashMap<Integer, String>();

    JSONObject banks = api.getBanks();
    for (Object b : banks.getJSONArray("banks")) {
      JSONObject bank = (JSONObject)b;
      result.put(bank.getInt("id"), bank.optString("name"));
    }

    return result;
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
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      logger.error("No token provided");
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
      logger.error("Budgea registration failed", e);
      setInternalError(response);
      return;
    }

    try {
      saveCloudConnection(user.get(CloudUser.ID), budgeaUserId, newBudgeaToken);
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
