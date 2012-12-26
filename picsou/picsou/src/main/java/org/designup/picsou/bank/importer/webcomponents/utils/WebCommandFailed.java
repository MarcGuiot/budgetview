package org.designup.picsou.bank.importer.webcomponents.utils;

import org.globsframework.utils.exceptions.GlobsException;

public class WebCommandFailed extends GlobsException {
  public WebCommandFailed(String message) {
    super(message);
  }

  public WebCommandFailed(Exception e) {
    super(e);
  }

  public WebCommandFailed(Exception e, String message) {
    super(message, e);
  }
}
