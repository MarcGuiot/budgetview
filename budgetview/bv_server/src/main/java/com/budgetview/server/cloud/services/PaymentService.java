package com.budgetview.server.cloud.services;

import org.globsframework.utils.exceptions.OperationFailed;

public interface PaymentService {

  CloudSubscription createSubscription(String email, String token) throws OperationFailed;

  void deleteSubscription(String customerId, String subscriptionId);
}
