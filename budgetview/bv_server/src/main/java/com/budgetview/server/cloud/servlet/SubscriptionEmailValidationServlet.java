package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.services.PaymentService;
import com.budgetview.server.cloud.utils.WebsiteUrls;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TimeExpired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SubscriptionEmailValidationServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("SubscriptionEmailValidationServlet");

  private final EmailValidationService emailValidationService;
  private final PaymentService payments;
  private final GlobsDatabase database;

  public SubscriptionEmailValidationServlet(Directory directory) {
    this.emailValidationService = directory.get(EmailValidationService.class);
    this.payments = directory.get(PaymentService.class);
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("GET");
    String code = request.getParameter("code");
    if (Strings.isNullOrEmpty(code)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Glob user;
    try {
      user = emailValidationService.getUser(code);
    }
    catch (ItemNotFound e) {
      response.sendRedirect(WebsiteUrls.invalidCode());
      return;
    }
    catch (TimeExpired e) {
      response.sendRedirect(WebsiteUrls.codeTimeout());
      return;
    }
    catch (Exception e) {
      logger.error("Error processing code " + code, e);
      response.sendRedirect(WebsiteUrls.error());
      return;
    }

    String email = user.get(CloudUser.EMAIL);
    String stripeToken = user.get(CloudUser.STRIPE_TOKEN);
    CloudSubscription subscription;
    try {
      subscription = payments.createSubscription(email, stripeToken);
    }
    catch (Exception e) {
      logger.error("Could not create subscription for " + email + " with stripeToken " + stripeToken, e);
      response.sendRedirect(WebsiteUrls.error());
      return;
    }

    SqlConnection connection = database.connect();
    try {
      connection.startUpdate(CloudUser.TYPE, Where.globEquals(user))
        .set(CloudUser.STRIPE_SUBSCRIPTION_ID, subscription.subscriptionId)
        .set(CloudUser.SUBSCRIPTION_END_DATE, subscription.currentPeriodEndDate)
        .run();
    }
    catch (Exception e) {
      logger.error("Error saving subscription for " + email, e);
      response.sendRedirect(WebsiteUrls.error());
      return;
    }
    finally {
      connection.commitAndClose();
    }

    String url = WebsiteUrls.subscriptionCreated();
    logger.info("Creation completed - redirect to " + url);
    response.sendRedirect(url);
  }
}
