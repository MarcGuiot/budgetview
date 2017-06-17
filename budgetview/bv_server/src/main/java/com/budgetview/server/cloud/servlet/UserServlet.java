package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.model.*;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.services.UserService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/user");

  private final AuthenticationService authentication;
  private final EmailValidationService emailValidation;
  private final Mailer mailer;

  public UserServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("POST");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        String lang = getLangHeader(CloudConstants.LANG);

        logger.info("Signup requested for new user " + email);

        try {
          Glob user = authentication.findUserWithEmail(email);
          if (user == null) {
            logger.debug("User not found for '" + email + "'");
            setSubscriptionError(CloudSubscriptionStatus.NO_SUBSCRIPTION, writer);
            return HttpServletResponse.SC_OK;
          }

          emailValidation.sendDeviceValidationTempCode(user.get(CloudUser.ID), email, lang);
          setOk(writer);
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

    };
    command.run();
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("DELETE");

    Command command = new HttpCommand(req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
        final Integer userId = getIntHeader(CloudConstants.CLOUD_USER_ID);
        Integer deviceId = getIntHeader(CloudConstants.DEVICE_ID);
        String deviceToken = getStringHeader(CloudConstants.DEVICE_TOKEN);
        final Glob user = authentication.findUser(userId, deviceId, deviceToken);
        if (user == null) {
          logger.error("Could not identify user:" + userId);
          return HttpServletResponse.SC_UNAUTHORIZED;
        }

        final String email = user.get(CloudUser.EMAIL);

        boolean success = directory.get(UserService.class).deleteUser(user, new UserService.DeletionCallback() {
          public void processOk() {
            try {
              mailer.sendCloudAccountDeleted(user.get(CloudUser.EMAIL), user.get(CloudUser.LANG));
            }
            catch (Exception e) {
              logger.error("Failed to send cloud account deletion email to " + email, e);
            }
            logger.info("Deleted account for user " + userId + " with email " + email);
          }

          public void processError(String message, Exception e) {
            logger.error(message, e);
            mailer.sendErrorToAdmin(getClass(), "Error when deleting user " + email, message, e);
          }
        });


        return success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      }
    };
    command.run();
  }

}
