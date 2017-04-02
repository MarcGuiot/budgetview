package org.globsframework.utils.exceptions;

public class ItemAlreadyUsed extends GlobsException {

  public ItemAlreadyUsed() {
  }

  public ItemAlreadyUsed(Exception e) {
    super(e);
  }

  public ItemAlreadyUsed(String message) {
    super(message);
  }

  public ItemAlreadyUsed(String message, Throwable cause) {
    super(message, cause);
  }
}
