package com.budgetview.server.cloud.services;

import java.util.Date;

public class CloudSubscription {

  public final String subscriptionId;
  public final Date currentPeriodEndDate;

  public CloudSubscription(String subscriptionId, Date currentPeriodEndDate) {
    this.subscriptionId = subscriptionId;
    this.currentPeriodEndDate = currentPeriodEndDate;
  }
}
