package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ValidateServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/validate");

  private final AuthenticationService authentication;
  private final EmailValidationService emailValidation;

  public ValidateServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidation = directory.get(EmailValidationService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getHeader(CloudConstants.EMAIL);
    if (Strings.isNullOrEmpty(email)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String code = request.getHeader(CloudConstants.CODE);
    if (Strings.isNullOrEmpty(code)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    boolean checkOk;
    try {
      checkOk = check(email, code);
    }
    catch (Exception e) {
      logger.error("Could not process: " + email, e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    JSONWriter writer = new JSONWriter(response.getWriter());
    writer.object();
    writer.key("status");
    writer.value(checkOk ? "ok" : "invalid");
    writer.endObject();

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private boolean check(String email, String code) throws GlobsSQLException, MessagingException {
    Integer userId = authentication.findUser(email);
    if (userId == null) {
      logger.info("User not found: " + email);
      return false;
    }

    return emailValidation.check(userId, code);
  }
}
