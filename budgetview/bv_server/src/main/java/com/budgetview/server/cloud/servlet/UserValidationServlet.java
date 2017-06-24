package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.ValidationFailed;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.streams.GlobStream;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserValidationServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("UserValidationServlet");

  private final EmailValidationService emailValidation;
  private final AuthenticationService authentication;
  private final GlobsDatabase database;

  public UserValidationServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("POST");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        logger.debug("Validation received for " + email);

        String code = getStringHeader(CloudConstants.VALIDATION_CODE);

        try {

          Glob user = authentication.findUserWithEmail(email);
          if (user == null) {
            return HttpServletResponse.SC_UNAUTHORIZED;
          }

          Integer userId = user.get(CloudUser.ID);
          writer.object();
          writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
          try {
            emailValidation.checkTempCode(userId, code);
            writer.key(CloudConstants.CLOUD_USER_ID).value(userId);
            AuthenticationService.DeviceId device = authentication.registerUserDevice(userId);
            writer.key(CloudConstants.DEVICE_ID).value(device.id);
            writer.key(CloudConstants.DEVICE_TOKEN).value(device.token);
          }
          catch (ValidationFailed checkFailed) {
            writer.key(CloudConstants.STATUS).value(checkFailed.getStatus());
            writer.endObject();
            return HttpServletResponse.SC_OK;
          }

          try {
            authentication.checkSubscriptionIsValid(userId);
            writer.key(CloudConstants.STATUS).value("ok");
            writer.key(CloudConstants.EXISTING_STATEMENTS).value(containsStatements(userId));
          }
          catch (SubscriptionCheckFailed e) {
            setSubscriptionError(response, e, writer);
          }

          writer.endObject();

          logger.info("User " + email + " registered with id " + userId);

          return HttpServletResponse.SC_OK;
        }
        catch (Exception e) {
          logger.error("Could not process: " + email, e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }

      private boolean containsStatements(Integer userId) {
        SqlConnection connection = database.connect();

        if (logger.isDebugEnabled()) {
          logger.debug("Existing statements:" + connection.selectAll(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId)));
        }

        SqlSelect query = connection.startSelect(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId))
          .getQuery();
        GlobStream stream = query.getStream();
        boolean containsEntry = stream.next();
        stream.close();
        query.close();
        return containsEntry;
      }
    };
    command.run();
  }
}
