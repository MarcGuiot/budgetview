package org.designup.picsou.bank.connectors.webcomponents.utils;

public class WebCommandFailed extends Exception {
  public WebCommandFailed(String message) {
    super(message);
  }

  public WebCommandFailed(Exception e) {
    super(e);
  }

  public WebCommandFailed(Throwable e, String message) {
    super(message, e);
  }
}
