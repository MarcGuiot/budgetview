package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
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

  public SignupServlet(Directory directory) {
    super(directory);
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
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
}
