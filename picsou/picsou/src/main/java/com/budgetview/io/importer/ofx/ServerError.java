package com.budgetview.io.importer.ofx;

public class ServerError extends RuntimeException{
  public ServerError(String message) {
    super(message);
  }
}
