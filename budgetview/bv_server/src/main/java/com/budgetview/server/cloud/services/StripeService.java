package com.budgetview.server.cloud.services;

import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import org.apache.log4j.Logger;
import org.globsframework.utils.exceptions.OperationFailed;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StripeService implements PaymentService {

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
      params.put("plan", "cloud_std");
      Customer customer = Customer.create(params);
      customerId = customer.getId();
    }
    catch (Exception e) {
      logger.error("Could not create customer", e);
      throw new OperationFailed(e);
    }

    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("customer", customerId);
      params.put("plan", "standard-cloud");
      com.stripe.model.Subscription subscription = com.stripe.model.Subscription.create(params);

      String subscriptionId = subscription.getId();
      Date currentPeriodEndDate = new Date(subscription.getCurrentPeriodEnd());
      return new CloudSubscription(customerId, subscriptionId, currentPeriodEndDate);
    }
    catch (Exception e) {
      logger.error("Could not create subscription for customer " + customerId + " with email "  + email + " and token " + stripeToken, e);
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
}
