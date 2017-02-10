package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.model.ProviderConnection;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SignupServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/signup");

  private final AuthenticationService authentication;
  private final EmailValidationService emailValidation;
  private final Mailer mailer;

  public SignupServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("POST");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        logger.info("Signup requested for " + email);

        String lang = getLangHeader(CloudConstants.LANG);

        try {
          processSignup(email, lang);
          setOk(writer);
          return HttpServletResponse.SC_OK;
        }
        catch (SubscriptionCheckFailed e) {
          setSubscriptionError(e, writer);
          return HttpServletResponse.SC_OK;
        }
        catch (Exception e) {
          logger.error("Could not process: " + email, e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }

      private String getLangHeader(String header) throws InvalidHeader {
        String lang = getStringHeader(header).trim().toLowerCase();
        if (Strings.isNullOrEmpty(lang)) {
          throw new InvalidHeaderValue(header, lang);
        }
        if (!"fr".equals(lang) && !"en".equals(lang)) {
          throw new InvalidHeaderValue(header, lang);
        }
        return lang;
      }

      private void processSignup(String email, String lang) throws GlobsSQLException, MessagingException, SubscriptionCheckFailed {
        Integer userId = authentication.findUser(email);
        if (userId == null) {
          logger.info("User not found for '" + email + "' - creating it");
          userId = authentication.createUser(email, lang);
        }
        else {
          logger.info("User " + userId + " found for '" + email + "'");
        }

        emailValidation.sendTempCode(userId, email, lang);
      }
    };
    command.run();
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.info("DELETE");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        String bvToken = getStringHeader(CloudConstants.BV_TOKEN);
        Glob user = authentication.findUserAndToken(email, bvToken);
        if (user == null) {
          logger.error("Could not identify user with email:" + email);
          return HttpServletResponse.SC_UNAUTHORIZED;
        }

        Integer userId = user.get(CloudUser.ID);
        Integer providerUserId = user.get(CloudUser.PROVIDER_USER_ID);
        try {
          BudgeaAPI budgeaAPI = new BudgeaAPI();
          budgeaAPI.setToken(user.get(CloudUser.PROVIDER_ACCESS_TOKEN));
          budgeaAPI.deleteUser(providerUserId);
        }
        catch (IOException e) {
          String message = "Failed to delete Budgea user " + email + " / " + userId + " with provider user ID " + providerUserId;
          logger.error(message, e);
          mailer.sendErrorToAdmin(getClass(), "Budgea user deletion failed", message, e);
        }

        GlobsDatabase database = directory.get(GlobsDatabase.class);
        SqlConnection connection = database.connect();
        try {
          connection
            .startDelete(ProviderUpdate.TYPE, Where.fieldEquals(ProviderUpdate.USER, providerUserId))
            .execute();
          connection
            .startDelete(ProviderConnection.TYPE, Where.fieldEquals(ProviderConnection.USER, providerUserId))
            .execute();
          connection
            .startDelete(CloudUserDevice.TYPE, Where.fieldEquals(CloudUserDevice.USER, providerUserId))
            .execute();
          connection
            .startDelete(CloudUser.TYPE, Where.fieldEquals(CloudUser.ID, providerUserId))
            .execute();
        }
        catch (GlobsSQLException e) {
          String message = "Failed to delete user " + email + " / " + userId + " with provider user ID " + providerUserId + " from database";
          logger.error(message, e);
          mailer.sendErrorToAdmin(getClass(), "User deletion failed", message, e);
          return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        finally {
          try {
            connection.commitAndClose();
          }
          catch (Exception e) {
            logger.error("Commit failed when deleting user: " + email + " / " + userId, e);
          }
        }

        return HttpServletResponse.SC_OK;
      }
    };
    command.run();
  }
}
