package com.budgetview.server.cloud.utils;

import com.budgetview.shared.cloud.CloudRequestStatus;

public class CheckFailed extends Exception {
  private CloudRequestStatus status;

  public CheckFailed(CloudRequestStatus status) {
    super("Invalid request: " + status);
    this.status = status;
  }

  public CloudRequestStatus getStatus() {
    return status;
  }
}
