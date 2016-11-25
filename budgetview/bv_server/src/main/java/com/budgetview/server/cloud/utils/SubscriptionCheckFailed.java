package com.budgetview.server.cloud.utils;

import com.budgetview.shared.cloud.CloudSubscriptionStatus;

public class SubscriptionCheckFailed extends Exception {

  private CloudSubscriptionStatus status;

  public SubscriptionCheckFailed(CloudSubscriptionStatus status) {
    this.status = status;
  }

  public CloudSubscriptionStatus getStatus() {
    return status;
  }

  public String toString() {
    return "subscriptionCheckFailed:" + status.getName();
  }
}
