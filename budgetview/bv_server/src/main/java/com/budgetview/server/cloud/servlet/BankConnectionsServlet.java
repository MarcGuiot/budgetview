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
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

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

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {

        Integer connectionId = getOptionalIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        if (connectionId == null) {
          getAllConnections();
        }
        else {
          getSingleConnection(connectionId);
        }

      }

      private void getAllConnections() throws IOException {
        JSONObject bankConnections = budgeaAPI.getUserConnections(user.get(CloudUser.PROVIDER_USER_ID));

        logger.info("Budgea connections:" + bankConnections.toString(2));

        Map<Integer, String> bankNames = getBankNames();

        JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
        writer.object();
        writer.key("connections");
        writer.array();
        for (Object c : bankConnections.getJSONArray("connections")) {
          JSONObject bankConnection = (JSONObject) c;
          writer.object();
          writer.key(CloudConstants.PROVIDER_ID).value(Integer.toString(Provider.BUDGEA.getId()));
          writer.key(CloudConstants.PROVIDER_CONNECTION_ID).value(bankConnection.getInt("id"));
          writer.key(CloudConstants.NAME).value(getName(bankConnection, bankNames));
          writer.endObject();
        }
        writer.endArray();
        writer.endObject();
        setOk(response);
      }

      private void getSingleConnection(Integer connectionId) throws IOException {

        SqlConnection sqlConnection = database.connect();
        GlobList connections =
          sqlConnection.startSelect(ProviderConnection.TYPE,
                                    Where.and(fieldEquals(ProviderConnection.USER, user.get(CloudUser.ID)),
                                              fieldEquals(ProviderConnection.PROVIDER_CONNECTION_ID, connectionId)))
            .selectAll()
            .getList();

        logger.info("All connections:");
        GlobPrinter.print(sqlConnection.startSelect(ProviderConnection.TYPE).selectAll().getList());

        logger.info("Select for user/connection:");
        GlobPrinter.print(connections);

        JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
        writer.object();
        writer.key("connection_id").value(connectionId);
        if (connections.isEmpty()) {
          writer.key("status").value("not_found");
        }
        else {
          Glob connection = connections.getFirst();
          writer.key("status").value(connection.isTrue(ProviderConnection.INITIALIZED) ? "ok" : "not_ready");
        }
        writer.endObject();
        setOk(response);
        sqlConnection.commitAndClose();
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
      protected void doRun() throws IOException, InvalidHeader {

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

        setOk(response);
      }
    };
    command.run();
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("DELETE");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {
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
          setOk(response);
        }
        catch (Exception e) {
          logger.error("Could not delete connection " + providerConnectionId + " for user " + user.get(CloudUser.ID), e);
          setInternalError(response);
        }
      }
    };
    command.run();
  }
}
