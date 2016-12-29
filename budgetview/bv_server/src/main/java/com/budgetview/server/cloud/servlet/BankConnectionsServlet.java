package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BankConnectionsServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/banks/connections");

  public BankConnectionsServlet(Directory directory) {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException {

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

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("DELETE");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {
        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        int providerConnectionId = getIntHeader(CloudConstants.PROVIDER_CONNECTION_ID);

        try {
          budgeaAPI.deleteConnection(providerConnectionId);
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
