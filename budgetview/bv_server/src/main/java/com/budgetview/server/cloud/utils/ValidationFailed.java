package com.budgetview.server.cloud.utils;

import com.budgetview.shared.cloud.CloudValidationStatus;

public class ValidationFailed extends Exception {
  private CloudValidationStatus status;

  public ValidationFailed(CloudValidationStatus status) {
    super("Invalid request: " + status);
    this.status = status;
  }

  public CloudValidationStatus getStatus() {
    return status;
  }
}
