package com.budgetview.server.cloud.services;

import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import org.apache.log4j.Logger;
import org.globsframework.utils.Dates;
import org.globsframework.utils.exceptions.OperationFailed;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StripeService implements PaymentService {

  private static final String STRIPE_PLAN = "cloud-std";
  private static final double TVA_RATE = 20.0;

  private static Logger logger = Logger.getLogger("StripeService");

  public StripeService() {
    Stripe.apiKey = "sk_test_p2kZ7X2c5pJ4r7Y6U44bkH79";  // https://dashboard.stripe.com/account/apikeys
  }

  public CloudSubscription createSubscription(String email, String stripeToken) throws OperationFailed {

    String customerId;
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("email", email);
      params.put("source", stripeToken);
      Customer customer = Customer.create(params);
      customerId = customer.getId();
    }
    catch (Exception e) {
      logger.error("Could not create customer for " + email, e);
      throw new OperationFailed(e);
    }

    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("customer", customerId);
      params.put("plan", STRIPE_PLAN);
      params.put("tax_percent", TVA_RATE);
      Subscription subscription = Subscription.create(params);
      logger.info("Created Stripe subscription for " + email);
      return convertToCloudSubscription(subscription);
    }
    catch (Exception e) {
      logger.error("Could not create subscription for customer " + customerId + " with email " + email + " and token " + stripeToken, e);
      throw new OperationFailed(e);
    }
  }

  public void updateCard(String customerId, String stripeToken) throws OperationFailed {

    logger.info("Updating card for " + customerId);

    try {
      Customer customer = Customer.retrieve(customerId);
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("source", stripeToken);
      customer.update(params);
    }
    catch (Exception e) {
      logger.error("Could not update card for customer " + customerId, e);
      throw new OperationFailed(e);
    }
  }

  private CloudSubscription convertToCloudSubscription(Subscription subscription) {
    String subscriptionId = subscription.getId();
    Date currentPeriodEndDate = toDate(subscription.getCurrentPeriodEnd());
    logger.debug("New subscription end: " + subscription.getCurrentPeriodEnd() + " ==> " + Dates.toString(currentPeriodEndDate));
    return new CloudSubscription(subscription.getCustomer(), subscriptionId, currentPeriodEndDate);
  }

  public CloudSubscription getSubscription(String subscriptionId) throws OperationFailed {
    try {
      Subscription subscription = Subscription.retrieve(subscriptionId);
      return convertToCloudSubscription(subscription);
    }
    catch (Exception e) {
      logger.error("Could not retrieve subscription with id " + subscriptionId, e);
      throw new OperationFailed(e);
    }
  }

  public void deleteSubscription(String customerId, String subscriptionId) throws OperationFailed {

    try {
      Customer customer = Customer.retrieve(customerId);
      Subscription subscription = customer.getSubscriptions().retrieve(subscriptionId);
      subscription.cancel(null);
    }
    catch (Exception e) {
      logger.error("Could not delete subscription for customer " + customerId + " and id " + subscriptionId, e);
      throw new OperationFailed(e);
    }
  }

  public CloudInvoice getInvoiceForEvent(String eventId) throws OperationFailed {
    Event event = null;
    try {
      event = Event.retrieve(eventId);
    }
    catch (Exception e) {
      logger.error("Could not retrieve invoice for event " + eventId, e);
      throw new OperationFailed(e);
    }
    if (event == null) {
      logger.error("No event found with id  " + eventId);
      throw new OperationFailed("No event found with id  " + eventId);
    }

    Object data = event.getData().getObject();
    if (data == null) {
      logger.error("No data for event " + eventId);
      throw new OperationFailed("No data for event " + eventId);
    }
    if (!(data instanceof Invoice)) {
      logger.error("Invalid data type for event " + data.getClass());
      throw new OperationFailed("No data for event " + "Invalid data type for event " + data.getClass());
    }

    Invoice invoice = (Invoice) data;
    Long total = invoice.getTotal();
    Long tax = invoice.getTax() != null ? invoice.getTax() : (long)(total * TVA_RATE / 100);
    return new CloudInvoice(invoice.getSubscription(),
                            invoice.getCharge().substring(3),
                            toAmount(total),
                            toAmount(tax),
                            toDate(invoice.getDate()));
  }

  private Double toAmount(Long value) {
    if (value == null) {
      return null;
    }
    Double result = (double)value;
    return result / 100;
  }

  private Date toDate(Long time) {
    if (time == null) {
      return null;
    }
    return new Date(time *  1000);
  }
}
