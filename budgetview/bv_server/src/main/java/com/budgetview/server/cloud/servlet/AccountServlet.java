package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.utils.Debug;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.utils.directory.Directory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccountServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("AccountServlet");

  public AccountServlet(Directory directory) {
    super(directory);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.debug("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        JSONObject root = getRequestBodyAsJson();
        JSONArray accounts = root.getJSONArray("accounts");
        for (Object a : accounts) {
          JSONObject account = (JSONObject) a;
          int accountId = account.getInt("provider_account_id");
          boolean enabled = account.getBoolean("enabled");
          if (Debug.isTestUser(user)) {
            logger.info("Updating budgea account " + accountId + " ==> " + enabled);
          }
          budgeaAPI.setAccountEnabled(accountId, enabled);
        }

        return HttpServletResponse.SC_OK;
      }
    };
    command.run();
  }
}
