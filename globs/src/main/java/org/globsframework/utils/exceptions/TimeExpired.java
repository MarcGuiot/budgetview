package org.globsframework.utils.exceptions;

public class TimeExpired extends GlobsException {

  public TimeExpired() {
  }

  public TimeExpired(Exception e) {
    super(e);
  }

  public TimeExpired(String message) {
    super(message);
  }

  public TimeExpired(String message, Throwable cause) {
    super(message, cause);
  }
}
