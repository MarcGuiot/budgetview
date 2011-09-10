package org.designup.picsou.importer.ofx;

public class ServerError extends RuntimeException{
  public ServerError(String message) {
    super(message);
  }
}
