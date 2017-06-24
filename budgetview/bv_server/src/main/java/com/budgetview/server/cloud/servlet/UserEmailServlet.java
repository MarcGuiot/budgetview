package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.AuthenticatedCommand;
import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserEmailServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("UserEmailServlet");

  private final EmailValidationService emailValidation;
  private final Mailer mailer;

  public UserEmailServlet(Directory directory) {
    super(directory);
    this.emailValidation = directory.get(EmailValidationService.class);
    this.mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.debug("POST");

    Command command = new AuthenticatedCommand(directory, req, resp, logger) {
      protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {

        String currentEmail = getStringHeader(CloudConstants.EMAIL);
        String newEmail = getStringHeader(CloudConstants.NEW_EMAIL);
        String lang = user.get(CloudUser.LANG);
        Integer userId = user.get(CloudUser.ID);

        mailer.setEmailModificationRequestAlert(currentEmail, newEmail, lang);
        emailValidation.setNewEmailValidationCode(userId, newEmail, lang);

        logger.info("User email change requested for user " + userId + " from " + currentEmail + " to " + newEmail);
        setOk(writer);
        return HttpServletResponse.SC_OK;
      }

    };
    command.run();
  }
}
