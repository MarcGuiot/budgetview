package com.budgetview.server.cloud.services;

import org.globsframework.utils.exceptions.OperationFailed;

public interface PaymentService {

  CloudSubscription createSubscription(String email, String token) throws OperationFailed;

  CloudSubscription getSubscription(String subscriptionId) throws OperationFailed;

  void updateCard(String customerId, String stripeToken);

  void deleteSubscription(String customerId, String subscriptionId);

  CloudInvoice getInvoiceForEvent(String eventId);
}
