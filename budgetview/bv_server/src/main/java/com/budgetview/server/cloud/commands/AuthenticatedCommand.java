package com.budgetview.server.cloud.commands;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AuthenticatedCommand extends HttpCommand {

  protected final GlobsDatabase database;
  protected final AuthenticationService authentication;
  protected BudgeaAPI budgeaAPI;
  protected Glob user;

  public AuthenticatedCommand(Directory directory, HttpServletRequest request, HttpServletResponse response, Logger logger) {
    super(request, response, logger);
    this.authentication = directory.get(AuthenticationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  public void run() throws ServletException, IOException {

    String email = request.getHeader(CloudConstants.EMAIL);
    String bvToken = request.getHeader(CloudConstants.BV_TOKEN);
    if (Strings.isNullOrEmpty(email)) {
      logger.error("No email provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      logger.error("No token provided");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      user = authentication.checkUserToken(email, bvToken);
    }
    catch (SubscriptionCheckFailed e) {
      response.setStatus(HttpServletResponse.SC_OK);
      JSONWriter writer = new JSONWriter(response.getWriter());
      writer.object();
      writer.key(CloudConstants.STATUS).value("no_subscription");
      writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
      writer.endObject();
      return;
    }

    if (user == null) {
      logger.error("Could not identify user with email:" + email);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    budgeaAPI = new BudgeaAPI();
    budgeaAPI.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN), true);

    super.run();
  }

}
