package com.budgetview.server.session;

public interface IdentifiedState extends SessionState {
  byte[] getLinkInfo();

  boolean isRegistered();

}
