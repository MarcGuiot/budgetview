package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderConnection;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.utils.Debug;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class BankConnectionsServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("BankConnectionsServlet");

  public BankConnectionsServlet(Directory directory) {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        Integer connectionId = getOptionalIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);
        if (connectionId == null) {
          getAllConnections(writer);
        }
        else {
          Integer providerId = getOptionalIntHeader(CloudConstants.PROVIDER_ID);
          if (providerId == null) {
            logger.error("No provider set in call where a provider connection id (" + connectionId + ") is set");
            return HttpServletResponse.SC_BAD_REQUEST;
          }
          if (providerId != Provider.BUDGEA.getId()) {
            logger.error("Unknown provider id " + providerId);
            return HttpServletResponse.SC_BAD_REQUEST;
          }
          getSingleConnection(providerId, connectionId, writer);
        }
        return HttpServletResponse.SC_OK;
      }

      private void getAllConnections(JSONWriter writer) throws IOException {

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)))
            .selectAll()
            .getList();
        sqlConnection.commitAndClose();

        Map<Integer, Glob> connectionsById = new HashMap<Integer, Glob>();
        for (Glob connection : connections) {
          connectionsById.put(connection.get(ProviderConnection.PROVIDER_CONNECTION), connection);
        }

        Integer providerUserId = user.get(CloudUser.PROVIDER_USER_ID);
        JSONArray budgeaConnections;
        JSONArray connectionAccounts;
        Map<Integer, String> bankNames;
        if (providerUserId != null) {
          budgeaConnections = fetchConnections(providerUserId);
          bankNames = fetchBankNames();
          connectionAccounts = fetchAccounts(providerUserId);
          if (Debug.isTestUser(user)) {
            logger.info("Accounts:" + connectionAccounts.toString(2));
          }
        }
        else {
          budgeaConnections = new JSONArray();
          bankNames = new HashMap<Integer, String>();
          connectionAccounts = new JSONArray();
        }

        writer.object();
        writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
        writer.key(CloudConstants.STATUS).value("ok");
        writer.key(CloudConstants.SUBSCRIPTION_END_DATE).value(JsonGlobFormat.toString(user.get(CloudUser.SUBSCRIPTION_END_DATE)));
        writer.key("connections");
        writer.array();
        for (Object c : budgeaConnections) {
          JSONObject budgeaConnection = (JSONObject) c;
          int budgeaConnectionId = budgeaConnection.getInt("id");
          int budgeaBankId = budgeaConnection.getInt("id_bank");
          Glob connection = connectionsById.get(budgeaConnectionId);
          boolean initialized = connection != null && connection.isTrue(ProviderConnection.INITIALIZED);
          Integer provider = connection != null ? connection.get(ProviderConnection.PROVIDER) : null;
          boolean passwordError = connection != null && connection.isTrue(ProviderConnection.PASSWORD_ERROR);
          boolean actionNeeded = connection != null && connection.isTrue(ProviderConnection.ACTION_NEEDED);

          writer.object();
          writer.key(CloudConstants.PROVIDER_ID).value(provider);
          writer.key(CloudConstants.PROVIDER_CONNECTION_ID).value(budgeaConnectionId);
          writer.key(CloudConstants.PROVIDER_BANK_ID).value(budgeaBankId);
          writer.key(CloudConstants.BANK_NAME).value(getName(budgeaConnection, bankNames));
          writer.key(CloudConstants.INITIALIZED).value(initialized);
          writer.key(CloudConstants.PASSWORD_ERROR).value(passwordError);
          writer.key(CloudConstants.ACTION_NEEDED).value(actionNeeded);
          writeConnectionAccounts(writer, budgeaConnectionId, connectionAccounts);
          writer.endObject();
        }
        writer.endArray();
        writer.endObject();
      }

      private void getSingleConnection(Integer providerId, Integer connectionId, JSONWriter writer) throws IOException {

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER, providerId),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION, connectionId)))
            .selectAll()
            .getList();
        sqlConnection.commitAndClose();

        writer.object();
        writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
        if (connections.isEmpty()) {
          writer.key(CloudConstants.STATUS).value("not_found");
          return;
        }
        else {
          writer.key(CloudConstants.STATUS).value("ok");
          writer.key("connections");
          writer.array();
          Glob connection = connections.getFirst();
          Integer providerConnectionId = connection.get(ProviderConnection.PROVIDER_CONNECTION);
          writer.object();
          writer.key("id").value(connectionId);
          writer.key(CloudConstants.PROVIDER_ID).value(connection.get(ProviderConnection.PROVIDER));
          writer.key(CloudConstants.PROVIDER_CONNECTION_ID).value(providerConnectionId);
          writer.key(CloudConstants.INITIALIZED).value(connection.isTrue(ProviderConnection.INITIALIZED));
          writer.key(CloudConstants.PASSWORD_ERROR).value(connection.isTrue(ProviderConnection.PASSWORD_ERROR));
          writer.key(CloudConstants.ACTION_NEEDED).value(connection.isTrue(ProviderConnection.ACTION_NEEDED));
          writer.endObject();
          writer.endArray();
        }
        writer.endObject();
      }

      private void writeConnectionAccounts(JSONWriter writer, int budgeaConnectionId, JSONArray connectionAccounts) {
        writer.key("accounts");
        writer.array();
        for (Object o : connectionAccounts) {
          JSONObject account = (JSONObject) o;
          int connectionId = account.getInt("id_connection");
          if (connectionId != budgeaConnectionId) {
            continue;
          }
          writer.object();
          writer.key(CloudConstants.PROVIDER_ACCOUNT_ID).value(account.getInt("id"));
          writer.key(CloudConstants.NAME).value(account.optString("name"));
          writer.key(CloudConstants.NUMBER).value(account.optString("number"));
          writer.key(CloudConstants.ENABLED).value(!Budgea.isDeleted(account));
          writer.endObject();
        }
        writer.endArray();
      }

      private JSONArray fetchConnections(Integer providerUserId) throws IOException {
        return budgeaAPI.getUserConnections(providerUserId).getJSONArray("connections");
      }

      private JSONArray fetchAccounts(Integer providerUserId) throws IOException {
        return budgeaAPI.getUserAccounts(providerUserId).getJSONArray("accounts");
      }

      private Map<Integer, String> fetchBankNames() throws IOException {
        Map<Integer, String> result = new HashMap<Integer, String>();
        JSONObject banks = budgeaAPI.getBanks();
        for (Object b : banks.getJSONArray("banks")) {
          JSONObject bank = (JSONObject) b;
          result.put(bank.getInt("id"), bank.optString("name"));
        }
        return result;
      }

      protected String getName(JSONObject connection, Map<Integer, String> bankNames) {
        int bankId = connection.optInt("id_bank");
        String name = bankNames.get(bankId);
        if (Strings.isNullOrEmpty(name)) {
          name = "-?-";
          logger.error("/Get - no bank found for bank_id " + bankId);
        }
        return name;
      }
    };
    command.run();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.debug("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        Integer userId = user.get(CloudUser.ID);
        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        if (providerId != Provider.BUDGEA.getId()) {
          logger.error("Unknown provider id " + providerId);
          return HttpServletResponse.SC_BAD_REQUEST;
        }

        int connectionId = getIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, userId),
                                              fieldEquals(ProviderConnection.PROVIDER, providerId),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION, connectionId)))
            .selectAll()
            .getList();
        if (connections.isEmpty()) {
          sqlConnection.startCreate(ProviderConnection.TYPE)
            .set(ProviderConnection.USER, userId)
            .set(ProviderConnection.PROVIDER, providerId)
            .set(ProviderConnection.PROVIDER_CONNECTION, connectionId)
            .set(ProviderConnection.INITIALIZED, false)
            .run();
          logger.info("Added connection " + connectionId + " for user " + userId);
        }
        sqlConnection.commitAndClose();

        setOk(writer);
        return HttpServletResponse.SC_OK;
      }
    };
    command.run();
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("DELETE");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        int providerConnectionId = getIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        try {
          if (providerId == Provider.BUDGEA.getId()) {
            budgeaAPI.deleteConnection(providerConnectionId);
          }
        }
        catch (IOException e) {
          logger.error("Budgea rejected request for deleting connection " + providerConnectionId + " for user " + user.get(CloudUser.ID), e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        try {
          SqlConnection sqlConnection = database.connect();
          sqlConnection.startDelete(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER, providerId),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION, providerConnectionId)))
            .execute();
          sqlConnection.startDelete(ProviderUpdate.TYPE,
                                    Where.and(fieldEquals(ProviderUpdate.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderUpdate.PROVIDER, providerId),
                                              fieldEquals(ProviderUpdate.PROVIDER_CONNECTION, providerConnectionId)))
            .execute();
          sqlConnection.commitAndClose();

          logger.info("Deleted connection " + providerConnectionId + " for user " + user.get(CloudUser.ID));
          setOk(writer);
          return HttpServletResponse.SC_OK;
        }
        catch (Exception e) {
          logger.error("Could not delete connection " + providerConnectionId + " for user " + user.get(CloudUser.ID), e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }
    };
    command.run();
  }
}
