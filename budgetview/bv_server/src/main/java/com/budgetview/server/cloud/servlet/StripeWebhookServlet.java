package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.commands.Command;
import com.budgetview.server.cloud.commands.DatabaseCommand;
import com.budgetview.server.cloud.model.CloudInvoiceLog;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.CloudInvoice;
import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.PaymentService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.utils.AmountFormat;
import com.stripe.model.Event;
import com.stripe.net.APIResource;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StripeWebhookServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("StripeWebhookServlet");
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

  private final Directory directory;
  private final PaymentService paymentService;
  private final Mailer mailer;

  public StripeWebhookServlet(Directory directory) {
    this.directory = directory;
    this.paymentService = directory.get(PaymentService.class);
    this.mailer = directory.get(Mailer.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    logger.debug("POST");

    Event event;
    try {
      event = parseEvent(request); // The event is retrieved again from the server in subsequent commands
    }
    catch (Exception e) {
      logger.error("Error parsing stripe webhook event", e);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (logger.isDebugEnabled()) {
      logger.debug(event.toJson());
    }

    // List of payment types: https://stripe.com/docs/api#event_types

    try {
      if ("invoice.payment_succeeded".equals(event.getType())) {
        logger.debug("invoice.payment_succeeded");
        CloudInvoice invoice = paymentService.getInvoiceForEvent(event.getId());
        final CloudSubscription subscription = paymentService.getSubscription(invoice.subscriptionId);
        Command command = new ProcessInvoicePaymentSuccess(invoice, subscription, request, response, event);
        command.run();
        return;
      }

      if ("invoice.payment_failed".equals(event.getType())) {
        logger.debug("invoice.payment_failed");
        CloudInvoice invoice = paymentService.getInvoiceForEvent(event.getId());
        final CloudSubscription subscription = paymentService.getSubscription(invoice.subscriptionId);
        Command command = new ProcessInvoicePaymentFailure(invoice, subscription, request, response, event);
        command.run();
        return;
      }
    }
    catch (Exception e) {
      logger.error("Error processing stripe webhook event\n" + event.toJson(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private class ProcessInvoicePaymentSuccess extends DatabaseCommand {
    private final CloudInvoice invoice;
    private final CloudSubscription subscription;
    private final Event event;

    public ProcessInvoicePaymentSuccess(CloudInvoice invoice, CloudSubscription subscription, HttpServletRequest request, HttpServletResponse response, Event event) {
      super(StripeWebhookServlet.this.directory, request, response, StripeWebhookServlet.logger);
      this.invoice = invoice;
      this.subscription = subscription;
      this.event = event;
    }

    protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
      SqlConnection connection = database.connect();
      GlobList users;
      try {
        connection.startUpdate(CloudUser.TYPE,
                               Where.fieldEquals(CloudUser.STRIPE_SUBSCRIPTION_ID, subscription.subscriptionId))
          .set(CloudUser.SUBSCRIPTION_END_DATE, subscription.currentPeriodEndDate)
          .run();

        users =
          connection.selectAll(CloudUser.TYPE,
                               Where.fieldEquals(CloudUser.STRIPE_SUBSCRIPTION_ID, subscription.subscriptionId));

        if (users == null || users.isEmpty()) {
          logger.error("No user found with subscriptionId " + subscription.subscriptionId);
          mailer.sendErrorToAdmin(getClass(), "Stripe webhook - user not found", "No CloudUser found with subscriptionId " + subscription + "\nEvent:" + event.toJson());
          return HttpServletResponse.SC_OK;
        }

        if (users.size() > 1) {
          logger.error("Several users found with subscriptionId " + subscription.subscriptionId + "\n" + GlobPrinter.toString(users));
          mailer.sendErrorToAdmin(getClass(), "Stripe webhook - several users found", "No CloudUser found with subscriptionId " + subscription + "\nEvent:" + event.toJson());
          return HttpServletResponse.SC_OK;
        }

        Glob user = users.getFirst();

        logger.debug("Processing invoice: " + invoice);

        String email = user.get(CloudUser.EMAIL);
        String total = AmountFormat.toString(invoice.total);
        String tax = AmountFormat.toString(invoice.tax);
        String excludingTaxes = AmountFormat.toString(invoice.total - invoice.tax);
        String date = toDate(invoice.date);

        boolean emailSent =  mailer.sendSubscriptionInvoice(email, "fr", invoice.receiptNumber, total, tax, excludingTaxes, date);

        connection.startCreate(CloudInvoiceLog.TYPE)
          .set(CloudInvoiceLog.USER, user.get(CloudUser.ID))
          .set(CloudInvoiceLog.EMAIL, email)
          .set(CloudInvoiceLog.DATE, invoice.date)
          .set(CloudInvoiceLog.AMOUNT, invoice.total)
          .set(CloudInvoiceLog.RECEIPT_NUMBER, invoice.receiptNumber)
          .set(CloudInvoiceLog.EMAIL_SENT, emailSent)
          .run();

        logger.info("Processed invoice " + invoice.receiptNumber + " for user " + email + " (" + user.get(CloudUser.ID) + ") for subscription " + invoice.subscriptionId);
      }
      finally {
        connection.commitAndClose();
      }

      return HttpServletResponse.SC_OK;
    }
  }

  private class ProcessInvoicePaymentFailure extends DatabaseCommand {
    private final CloudInvoice invoice;
    private final CloudSubscription subscription;
    private final Event event;

    public ProcessInvoicePaymentFailure(CloudInvoice invoice, CloudSubscription subscription, HttpServletRequest request, HttpServletResponse response, Event event) {
      super(StripeWebhookServlet.this.directory, request, response, StripeWebhookServlet.logger);
      this.invoice = invoice;
      this.subscription = subscription;
      this.event = event;
    }

    protected int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader {
      SqlConnection connection = database.connect();
      GlobList users;
      try {
        users =
          connection.selectAll(CloudUser.TYPE,
                               Where.fieldEquals(CloudUser.STRIPE_SUBSCRIPTION_ID, subscription.subscriptionId));
      }
      finally {
        connection.commitAndClose();
      }

      if (users == null || users.isEmpty()) {
        logger.error("No user found with subscriptionId " + subscription.subscriptionId);
        mailer.sendErrorToAdmin(getClass(), "Stripe webhook - user not found", "No CloudUser found with subscriptionId " + subscription + "\nEvent:" + event.toJson());
        return HttpServletResponse.SC_OK;
      }

      if (users.size() > 1) {
        logger.error("Several users found with subscriptionId " + subscription.subscriptionId + "\n" + GlobPrinter.toString(users));
        mailer.sendErrorToAdmin(getClass(), "Stripe webhook - several users found", "No CloudUser found with subscriptionId " + subscription + "\nEvent:" + event.toJson());
        return HttpServletResponse.SC_OK;
      }

      Glob user = users.getFirst();

      String email = user.get(CloudUser.EMAIL);
      String date = toDate(invoice.date);
      mailer.sendSubscriptionInvoiceFailed(email, "fr", invoice.receiptNumber, date);

      if (logger.isDebugEnabled()) {
        logger.debug("Processed invoice " + invoice.receiptNumber + " failure for user " + email + " (" + user.get(CloudUser.ID) + ") - email sent");
      }

      return HttpServletResponse.SC_OK;
    }
  }

  private String toDate(Date date) {
    return dateFormat.format(date);
  }

  private Event parseEvent(HttpServletRequest request) throws IOException {
    Event event;
    InputStream inputStream = request.getInputStream();
    String json = Files.loadStreamToString(inputStream, "UTF-8");
    event = APIResource.GSON.fromJson(json, Event.class);
    return event;
  }
}
