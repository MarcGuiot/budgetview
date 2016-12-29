package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
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
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class BankConnectionsServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/banks/connections");

  private final GlobsDatabase database;
  private final AuthenticationService authentication;

  public BankConnectionsServlet(Directory directory) {
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
    JSONObject connections = api.getUserConnections(user.get(CloudUser.PROVIDER_USER_ID));

    logger.info("Budgea connections:" + connections.toString(2));

    Map<Integer, String> bankNames = getBankNames(api);

    JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
    writer.object();
    writer.key("connections");
    writer.array();
    for (Object c : connections.getJSONArray("connections")) {
      JSONObject connection = (JSONObject)c;
      writer.object();
      writer.key(CloudConstants.PROVIDER_ID).value(Integer.toString(Provider.BUDGEA.getId()));
      writer.key(CloudConstants.PROVIDER_CONNECTION_ID).value(connection.getInt("id"));
      writer.key(CloudConstants.NAME).value(getName(connection, bankNames));
      writer.endObject();
    }
    writer.endArray();
    writer.endObject();
    setOk(response);
  }

  protected String getName(JSONObject connection, Map<Integer, String> bankNames) {
    int bankId = connection.optInt("id_bank");
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

  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);
    String providerId = request.getHeader(CloudConstants.PROVIDER_ID);
    String providerConnectionId = request.getHeader(CloudConstants.PROVIDER_CONNECTION_ID);

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
      logger.error("No provider provided");
      setBadRequest(response);
      return;
    }
    if (Strings.isNullOrEmpty(providerConnectionId)) {
      logger.error("No providerConnectionId provided");
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

    try {
      api.deleteConnection(Integer.parseInt(providerConnectionId));
      logger.info("Deleted connection " + providerConnectionId + " for user " + user.get(CloudUser.ID));
      setOk(response);
    }
    catch (Exception e) {
      logger.error("Could not delete connection " + providerConnectionId + " for user " + user.get(CloudUser.ID), e);
      setInternalError(response);
    }
  }
}
