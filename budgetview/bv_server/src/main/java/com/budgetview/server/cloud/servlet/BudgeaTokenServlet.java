package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.CloudRequestStatus;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BudgeaTokenServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("BudgeaTokenServlet");

  public BudgeaTokenServlet(Directory directory) throws Exception {
    super(directory);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.debug("GET");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException {
        String permanentBudgeaToken = user.get(CloudUser.PROVIDER_ACCESS_TOKEN);
        String temporaryToken;
        if (Strings.isNotEmpty(permanentBudgeaToken)) {
          temporaryToken = BudgeaAPI.requestTemporaryToken(permanentBudgeaToken);
        }
        else {
          temporaryToken = BudgeaAPI.requestFirstTemporaryToken();
        }
        writer.object();
        writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
        writer.key(CloudConstants.STATUS).value(CloudRequestStatus.OK);
        writer.key(CloudConstants.PROVIDER_TOKEN).value(temporaryToken);
        writer.key(CloudConstants.PROVIDER_TOKEN_REGISTERED).value(Strings.isNotEmpty(permanentBudgeaToken));
        writer.endObject();
        return HttpServletResponse.SC_OK;
      }
    };
    command.run();
  }
}
