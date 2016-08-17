package org.globsframework.utils.exceptions;

public class ParsingFailed extends GlobsException {
  public ParsingFailed(Exception e) {
    super(e);
  }

  public ParsingFailed(String message) {
    super(message);
  }

  public ParsingFailed(String message, Throwable cause) {
    super(message, cause);
  }
}
