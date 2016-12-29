package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.HttpCommand;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.SubscriptionCheckFailed;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

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

    Command command = new HttpCommand(directory, req, resp, logger) {
      protected void doRun() throws IOException, InvalidHeader {
        String email = getStringHeader(CloudConstants.EMAIL);
        logger.info("Signup requested for " + email);

        String lang = request.getHeader(CloudConstants.LANG);
        try {
          processSignup(email, lang);
        }
        catch (SubscriptionCheckFailed e) {
          setSubscriptionError(response, e);
          return;
        }
        catch (Exception e) {
          logger.error("Could not process: " + email, e);
          setInternalError(response);
          return;
        }

        setOk(response);
      }

      private void processSignup(String email, String lang) throws GlobsSQLException, MessagingException, SubscriptionCheckFailed {
        Integer userId = authentication.findUser(email);
        if (userId == null) {
          logger.info("User not found for '" + email + "' - creating it");
          userId = authentication.createUser(email);
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
