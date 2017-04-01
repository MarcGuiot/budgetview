package com.budgetview.shared.cloud;

import org.globsframework.utils.exceptions.ItemNotFound;

public enum CloudSubscriptionStatus {
  OK("ok"),
  /** @deprecated */
  UNKNOWN_USER("unknown"),
  NO_SUBSCRIPTION("not_purchased"),
  EXPIRED("expired");

  private String name;

  CloudSubscriptionStatus(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public static CloudSubscriptionStatus get(String name) {
    for (CloudSubscriptionStatus status : values()) {
      if (status.name.equalsIgnoreCase(name)) {
        return status;
      }
    }
    throw new ItemNotFound(name + " not associated to any CloudSubscriptionStatus enum value");
  }
}
