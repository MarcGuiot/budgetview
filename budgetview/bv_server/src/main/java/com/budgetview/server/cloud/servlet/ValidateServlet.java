package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.ValidationFailed;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
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

public class ValidateServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/validate");

  private final EmailValidationService emailValidation;
  private final AuthenticationService authentication;
  private final GlobsDatabase database;

  public ValidateServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("POST");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        logger.info("Validation received for " + email);

        String code = getStringHeader(CloudConstants.VALIDATION_CODE);

        try {

          Integer userId = authentication.findUser(email);
          if (userId == null) {
            return HttpServletResponse.SC_UNAUTHORIZED;
          }

          writer.object();
          try {
            emailValidation.checkTempCode(userId, code);
            String newToken = authentication.registerUserDevice(userId);
            writer.key(CloudConstants.BV_TOKEN).value(newToken);
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

          logger.info("User " + email + " registered");

          return HttpServletResponse.SC_OK;
        }
        catch (Exception e) {
          logger.error("Could not process: " + email, e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }

      private boolean containsStatements(Integer userId) {
        SqlConnection connection = database.connect();

        logger.info("Existing statements:" + connection.selectAll(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, userId)));


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
