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
import org.globsframework.model.Glob;
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

  private static Logger logger = Logger.getLogger("ProviderAccessServlet");

  public ProviderAccessServlet(Directory directory) {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);

        boolean sameProvider = Utils.equal(user.get(CloudUser.PROVIDER), providerId);

        writer.object();
        writer.key("status").value(sameProvider ? "ok" : "not_recognized");
        writer.endObject();

        return HttpServletResponse.SC_OK;
      }
    };
    command.run();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        int providerId = getIntHeader(CloudConstants.PROVIDER_ID);
        String budgeaToken = getStringHeader(CloudConstants.PROVIDER_TOKEN);
        int budgeaUserId = getIntHeader(CloudConstants.PROVIDER_USER_ID);

        String newBudgeaToken;
        try {
          newBudgeaToken = registerPermanentBudgeaToken(budgeaToken);
        }
        catch (Exception e) {
          logger.error("Budgea registration failed - could not obtain permanent token", e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        try {
          saveProviderAccess(user, providerId, budgeaUserId, newBudgeaToken);
          logger.info("Saved new connection for budgea user " + budgeaUserId + " for cloud user " + user.get(CloudUser.ID));
          return HttpServletResponse.SC_OK;
        }
        catch (GlobsSQLException e) {
          logger.error("Could not store user '" + user.get(CloudUser.EMAIL) + "' in dabase", e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }

      private String registerPermanentBudgeaToken(String tempBudgeaToken) throws IOException {
        String url = BudgeaConstants.getServerUrl("/auth/token/access");
        Request request = Request.Post(url)
          .bodyForm(Form.form()
                      .add("code", tempBudgeaToken)
                      .add("client_id", Budgea.CLIENT_ID)
                      .add("client_secret", Budgea.CLIENT_SECRET)
                      .build());
        return json(request, url).getString("access_token");
      }

      private void saveProviderAccess(Glob user, int providerId, int providerUserId, String providerAccessToken) throws GlobsSQLException {
        SqlConnection connection = database.connect();
        connection.startUpdate(CloudUser.TYPE, Where.globEquals(user))
          .set(CloudUser.PROVIDER, providerId)
          .set(CloudUser.PROVIDER_USER_ID, providerUserId)
          .set(CloudUser.PROVIDER_ACCESS_TOKEN, providerAccessToken)
          .run();
        connection.commitAndClose();
        logger.debug("Saved connection for userId " + providerUserId + " with token " + providerAccessToken);
      }
    };
    command.run();
  }
}
