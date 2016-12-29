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
    super(directory, request, response, logger);
    this.authentication = directory.get(AuthenticationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  public void run() throws ServletException, IOException {

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

    budgeaAPI = new BudgeaAPI();
    budgeaAPI.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN), true);

    try {
      doRun();
    }
    catch (InvalidHeader invalidHeader) {
      logger.error(invalidHeader.getMessage());
      setBadRequest(response);
      return;
    }
  }

  protected void setSubscriptionError(HttpServletResponse response, SubscriptionCheckFailed e) throws IOException {
    JSONWriter writer = new JSONWriter(response.getWriter());
    writer.object();
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    writer.endObject();
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void setSubscriptionError(HttpServletResponse response, SubscriptionCheckFailed e, JSONWriter writer) throws IOException {
    writer.key(CloudConstants.STATUS).value("no_subscription");
    writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
