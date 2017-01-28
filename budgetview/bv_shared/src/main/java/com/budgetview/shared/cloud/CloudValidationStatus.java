package com.budgetview.shared.cloud;

import org.globsframework.utils.exceptions.ItemNotFound;

public enum CloudValidationStatus {
  OK("ok"),
  UNKNOWN_VALIDATION_CODE("unknown_code"),
  NO_SUBSCRIPTION("no_subscription"),
  TEMP_VALIDATION_CODE_EXPIRED("temp_code_expired");

  private String name;

  CloudValidationStatus(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public static CloudValidationStatus get(String name) {
    for (CloudValidationStatus status : values()) {
      if (status.name.equalsIgnoreCase(name)) {
        return status;
      }
    }
    throw new ItemNotFound(name + " not associated to any CloudValidationStatus enum value");
  }
}
