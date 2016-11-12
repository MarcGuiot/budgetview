package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.CloudSubscriptionException;
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

public class SignupServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/signup");

  private final AuthenticationService authentication;
  private final EmailValidationService emailValidation;

  public SignupServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    logger.info("Signup requested for " + email);
    if (Strings.isNullOrEmpty(email)) {
      logger.error("Missing email");
      setInternalError(response);
      return;
    }

    String lang = request.getHeader(CloudConstants.LANG);
    try {
      processSignup(email, lang);
    }
    catch (CloudSubscriptionException e) {
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

  private void processSignup(String email, String lang) throws GlobsSQLException, MessagingException, CloudSubscriptionException {
    Integer userId = authentication.findUser(email);
    if (userId == null) {
      userId = authentication.createUser(email);
    }

    authentication.invalidateUserEmail(userId);

    emailValidation.send(userId, email, lang);
  }
}
