package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.ValidationFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.streams.GlobStream;
import org.globsframework.utils.directory.Directory;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserEmailChangeValidationServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("UserEmailChangeValidationServlet");

  private final EmailValidationService emailValidation;
  private final AuthenticationService authentication;
  private final GlobsDatabase database;

  public UserEmailChangeValidationServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.info("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        Integer userId = user.get(CloudUser.ID);
        String newEmail = getStringHeader(CloudConstants.NEW_EMAIL);
        logger.info("Validation received for new email: " + newEmail);
        String code = getStringHeader(CloudConstants.VALIDATION_CODE);
        try {
          writer.object();
          writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
          try {
            emailValidation.checkTempCode(userId, code);
          }
          catch (ValidationFailed checkFailed) {
            writer.key(CloudConstants.STATUS).value(checkFailed.getStatus());
            writer.endObject();
            return HttpServletResponse.SC_OK;
          }

          try {
            updateEmailAddress(userId, newEmail);
          }
          catch (Exception e) {
            logger.error("Error saving new adress: " + newEmail);
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
          }

          logger.info("Email changed for user: " + userId + " to " + newEmail);
          writer.key(CloudConstants.NEW_EMAIL).value(newEmail);
          writer.key(CloudConstants.STATUS).value("ok");
          writer.endObject();
          return HttpServletResponse.SC_OK;
        }
        catch (Exception e) {
          logger.error("Could not process: " + newEmail, e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }

      private void updateEmailAddress(Integer userId, String newEmail) throws GlobsSQLException {
        SqlConnection connection = database.connect();
        connection.startUpdate(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, userId))
          .set(CloudUser.EMAIL, newEmail)
          .run();
        connection.commitAndClose();
      }
    };
    command.run();
  }
}
