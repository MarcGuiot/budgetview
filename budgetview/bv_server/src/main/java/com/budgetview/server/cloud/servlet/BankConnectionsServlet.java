package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderConnection;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
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

  private static Logger logger = Logger.getLogger("/banks/connections");

  public BankConnectionsServlet(Directory directory) {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

    logger.info("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        Integer connectionId = getOptionalIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        if (connectionId == null) {
          getAllConnections(writer);
        }
        else {
          getSingleConnection(connectionId, writer);
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
          connectionsById.put(connection.get(ProviderConnection.PROVIDER_CONNECTION_ID), connection);
        }

        JSONObject budgeaConnections = budgeaAPI.getUserConnections(user.get(CloudUser.PROVIDER_USER_ID));

        logger.info("Budgea connections:" + budgeaConnections.toString(2));

        Map<Integer, String> bankNames = getBankNames();

        writer.object();
        writer.key(CloudConstants.STATUS).value("ok");
        writer.key("connections");
        writer.array();
        for (Object c : budgeaConnections.getJSONArray("connections")) {
          JSONObject budgeaConnection = (JSONObject) c;
          int budgeaConnectionId = budgeaConnection.getInt("id");
          Glob connection = connectionsById.get(budgeaConnectionId);
          boolean initialized = connection != null ? connection.get(ProviderConnection.INITIALIZED) : false;

          writer.object();
          writer.key(CloudConstants.PROVIDER_ID).value(Integer.toString(Provider.BUDGEA.getId()));
          writer.key(CloudConstants.PROVIDER_CONNECTION_ID).value(budgeaConnectionId);
          writer.key(CloudConstants.BANK_NAME).value(getName(budgeaConnection, bankNames));
          writer.key(CloudConstants.INITIALIZED).value(initialized);
          writer.endObject();
        }
        writer.endArray();
        writer.endObject();
      }

      private void getSingleConnection(Integer connectionId, JSONWriter writer) throws IOException {

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)))
            .selectAll()
            .getList();
        sqlConnection.commitAndClose();

        writer.object();
        if (connections.isEmpty()) {
          writer.key(CloudConstants.STATUS).value("not_found");
          return;
        }
        else {
          writer.key(CloudConstants.STATUS).value("ok");
          writer.key("connections");
          writer.array();
          Glob connection = connections.getFirst();
          writer.object();
          writer.key("id").value(connectionId);
          writer.key(CloudConstants.INITIALIZED).value(connection.isTrue(ProviderConnection.INITIALIZED));
          writer.endObject();
          writer.endArray();
        }
        writer.endObject();
      }

      private Map<Integer, String> getBankNames() throws IOException {
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
          logger.error("/Get - no bank found fon bank_id " + bankId);
        }
        return name;
      }
    };
    command.run();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.info("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        Integer userId = user.get(CloudUser.ID);
        int connectionId = getIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, userId),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)))
            .selectAll()
            .getList();
        if (connections.isEmpty()) {
          sqlConnection.startCreate(ProviderConnection.TYPE)
            .set(ProviderConnection.USER, userId)
            .set(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)
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

    logger.info("DELETE");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        int providerConnectionId = getIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        try {
          budgeaAPI.deleteConnection(providerConnectionId);

          SqlConnection sqlConnection = database.connect();
          sqlConnection.startDelete(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION_ID, providerConnectionId)))
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
