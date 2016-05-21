package com.budgetview.importer.ofx;

public class ServerError extends RuntimeException{
  public ServerError(String message) {
    super(message);
  }
}
