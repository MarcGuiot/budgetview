package com.budgetview.server.cloud.services;

import java.util.Date;

public class CloudSubscription {

  public final String customerId;
  public final String subscriptionId;
  public Date currentPeriodEndDate;

  public CloudSubscription(String customerId, String subscriptionId, Date currentPeriodEndDate) {
    this.customerId = customerId;
    this.subscriptionId = subscriptionId;
    this.currentPeriodEndDate = currentPeriodEndDate;
  }
}
