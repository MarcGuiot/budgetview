package com.budgetview.server.cloud.utils;

import com.budgetview.shared.cloud.CloudSubscriptionStatus;

public class CloudSubscriptionException extends Throwable {

  private CloudSubscriptionStatus status;

  public CloudSubscriptionException(CloudSubscriptionStatus status) {
    this.status = status;
  }

  public CloudSubscriptionStatus getStatus() {
    return status;
  }

  public String toString() {
    return "cloudSubscriptionException:" + status.getName();
  }
}
