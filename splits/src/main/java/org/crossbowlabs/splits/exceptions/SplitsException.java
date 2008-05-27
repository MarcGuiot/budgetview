package org.crossbowlabs.splits.exceptions;

public class SplitsException extends RuntimeException {
  public SplitsException(String message) {
    super(message);
  }

  public SplitsException(String message, Throwable cause) {
    super(message, cause);
  }

  public SplitsException(Throwable cause) {
    super(cause);
  }
}
