package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.budgetview.shared.json.Json.json;

public class ProviderAccessServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/provider/access");

  public ProviderAccessServlet(Directory directory) {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {

        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);

        boolean sameProvider = Utils.equal(user.get(CloudUser.PROVIDER), providerId);

        JsonGlobWriter writer = new JsonGlobWriter(response.getWriter());
        writer.object();
        writer.key("status").value(sameProvider ? "ok" : "not_recognized");
        writer.endObject();
        setOk(response);
      }
    };
    command.run();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {

        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        String budgeaToken = getStringHeader(CloudConstants.PROVIDER_TOKEN);
        int budgeaUserId = getIntHeader(CloudConstants.PROVIDER_USER_ID);

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
          logger.error("Could not store user '" + user.get(CloudUser.EMAIL) + "' in dabase", e);
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

      private void saveProviderAccess(Integer userId, int providerId, int providerUserId, String providerAccessToken) throws GlobsSQLException {
        SqlConnection connection = database.connect();
        connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
          .set(CloudUser.PROVIDER, providerId)
          .set(CloudUser.PROVIDER_USER_ID, providerUserId)
          .set(CloudUser.PROVIDER_ACCESS_TOKEN, providerAccessToken)
          .run();
        connection.commitAndClose();
        logger.info("Saved connection for userId " + providerUserId + " with token " + providerAccessToken);
      }
    };
    command.run();
  }
}
