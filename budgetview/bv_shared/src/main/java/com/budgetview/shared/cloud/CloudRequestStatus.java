package com.budgetview.shared.cloud;

import org.globsframework.utils.exceptions.ItemNotFound;

public enum CloudRequestStatus {
  OK("ok"),
  UNKNOWN_CODE("unknown_code"),
  NO_SUBSCRIPTION("no_subscription"),
  TEMP_CODE_EXPIRED("temp_code_expired");

  private String name;

  CloudRequestStatus(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public static CloudRequestStatus get(String name) {
    for (CloudRequestStatus status : values()) {
      if (status.name.equalsIgnoreCase(name)) {
        return status;
      }
    }
    throw new ItemNotFound(name + " not associated to any CloudSubscriptionStatus enum value");
  }
}
