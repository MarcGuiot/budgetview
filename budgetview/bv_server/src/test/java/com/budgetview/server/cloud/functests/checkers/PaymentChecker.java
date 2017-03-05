package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.services.CloudInvoice;
import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.PaymentService;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.http.Http;
import com.stripe.model.Event;
import junit.framework.AssertionFailedError;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;
import org.junit.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class PaymentChecker {

  private DummyPaymentService paymentService = new DummyPaymentService();
  private String lastEmail;
  private String lastToken;
  private String lastSubscriptionId;
  private Date subscriptionEndDate = Dates.tomorrow();
  private String lastCustomerId;
  private String lastDeletedCustomer;
  private String lastDeletedSubscription;
  private Map<String, CloudSubscription> subscriptions = new HashMap<String, CloudSubscription>();
  private Map<String, CloudInvoice> invoiceEvents = new HashMap<String, CloudInvoice>();
  private int index = 100;

  public PaymentChecker() {
  }

  public void install(Directory directory) {
    directory.add(PaymentService.class, paymentService);
  }

  public void setSubscriptionEndDate(Date subscriptionEndDate) {
    this.subscriptionEndDate = subscriptionEndDate;
  }

  public String checkLastRequest(String email, String token) {
    assertEquals(email + " / " + token, lastEmail + " / " + lastToken);
    return lastSubscriptionId;
  }

  public void checkSubscriptionDeleted(String subscriptionId) {
    if (lastSubscriptionId == null) {
      Assert.fail("No subscription deleted");
    }
    Assert.assertEquals(subscriptionId, lastSubscriptionId);
  }

  private class DummyPaymentService implements PaymentService {

    private Logger logger = Logger.getLogger("DummyPaymentService");

    private int index = 100000;

    public CloudSubscription createSubscription(String email, String token) throws OperationFailed {
      logger.info("createSubscription(" + email + ", " + token + ")");
      PaymentChecker.this.lastEmail = email;
      PaymentChecker.this.lastToken = token;
      PaymentChecker.this.lastCustomerId = "customer/" + index;
      PaymentChecker.this.lastSubscriptionId = "subscription/" + index;
      index++;
      CloudSubscription subscription = new CloudSubscription(lastCustomerId, lastSubscriptionId, subscriptionEndDate);
      subscriptions.put(lastSubscriptionId, subscription);
      return subscription;
    }

    public CloudSubscription getSubscription(String subscriptionId) throws OperationFailed {
      return doGetSubscription(subscriptionId);
    }

    public void deleteSubscription(String customerId, String subscriptionId) {
      PaymentChecker.this.lastDeletedCustomer = customerId;
      PaymentChecker.this.lastDeletedSubscription = subscriptionId;
      subscriptions.remove(lastDeletedSubscription);
    }

    public CloudInvoice getInvoiceForEvent(String eventId) {
      CloudInvoice invoice = invoiceEvents.get(eventId);
      if (invoice == null) {
        throw new AssertionFailedError("No event found with id " + eventId + " - actual content: " + Utils.toString(invoiceEvents.keySet()));
      }
      return invoice;
    }
  }

  public CloudSubscription doGetSubscription(String subscriptionId) {
    CloudSubscription subscription = subscriptions.get(subscriptionId);
    if (subscription == null) {
      throw new AssertionFailedError("No subscription registered for " + subscriptionId);
    }
    return subscription;
  }

  public void notifyInvoice(String subscriptionId, Date currentPeriodEndDate, String receiptNumber) throws Exception {

    String eventId = "event" + index++;

    CloudSubscription subscription = doGetSubscription(subscriptionId);
    subscription.currentPeriodEndDate = currentPeriodEndDate;

    CloudInvoice invoice = new CloudInvoice(subscriptionId, receiptNumber, 2.25, 0.5, currentPeriodEndDate);
    invoiceEvents.put(eventId, invoice);

    Event event = new Event();
    event.setId(eventId);
    event.setType("invoice.payment_succeeded");

    String url = CloudConstants.getServerUrl("/stripe");
    Http.execute(url, Request.Post(url)
      .bodyString(event.toJson(), ContentType.APPLICATION_JSON));
  }
}
