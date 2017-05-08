package com.budgetview.server.cloud.commands;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AuthenticatedCommand extends DatabaseCommand {

  protected final AuthenticationService authentication;
  protected BudgeaAPI budgeaAPI;
  protected Glob user;

  public AuthenticatedCommand(Directory directory, HttpServletRequest request, HttpServletResponse response, Logger logger) {
    super(directory, request, response, logger);
    this.authentication = directory.get(AuthenticationService.class);
  }

  public void run() throws ServletException, IOException {

    int cloudUserId = 0;
    int deviceId = 0;
    try {
      cloudUserId = getIntHeader(CloudConstants.CLOUD_USER_ID);
      deviceId = getIntHeader(CloudConstants.DEVICE_ID);
    }
    catch (InvalidHeader invalidHeader) {
      logger.error("[AuthenticatedCommand] Missing header", invalidHeader);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    String deviceToken = request.getHeader(CloudConstants.DEVICE_TOKEN);
    if (Strings.isNullOrEmpty(deviceToken)) {
      logger.error("[AuthenticatedCommand] No token provided in call from " + cloudUserId);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      user = authentication.checkUserToken(cloudUserId, deviceId, deviceToken);
    }
    catch (SubscriptionCheckFailed e) {
      response.setStatus(HttpServletResponse.SC_OK);
      JSONWriter writer = new JSONWriter(response.getWriter());
      writer.object();
      writer.key(CloudConstants.STATUS).value("no_subscription");
      writer.key(CloudConstants.SUBSCRIPTION_STATUS).value(e.getStatus().getName());
      writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
      writer.endObject();
      return;
    }

    if (user == null) {
      logger.error("[AuthenticatedCommand] Could not identify user with id: " + cloudUserId + " / " + deviceId + " / " + deviceToken);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    budgeaAPI = new BudgeaAPI();
    budgeaAPI.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN));

    super.run();
  }

}
