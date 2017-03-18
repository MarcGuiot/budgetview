package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.WebsiteUrls;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.globsframework.model.FieldValue.value;

public class StripeFormServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("StripeFormServlet");

  private final AuthenticationService authentication;
  private final EmailValidationService emailValidationService;

  public StripeFormServlet(Directory directory) {
    this.authentication = directory.get(AuthenticationService.class);
    this.emailValidationService = directory.get(EmailValidationService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String token = request.getParameter("stripeToken");
    String email = request.getParameter("stripeEmail");

    try {
      Glob user = authentication.findOrCreateUser(email, "fr", value(CloudUser.STRIPE_TOKEN, token));
      emailValidationService.sendSubscriptionEmailValidationLink(user.get(CloudUser.ID), email, "fr");
    }
    catch (Exception e) {
      logger.error("Could not process stripe form", e);
      response.sendRedirect(WebsiteUrls.error());
      return;
    }

    response.sendRedirect(WebsiteUrls.emailSent());
  }
}
