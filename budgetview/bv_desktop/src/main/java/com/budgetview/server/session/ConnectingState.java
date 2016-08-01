package com.budgetview.server.session;

public interface ConnectingState extends SessionState {
  byte[] getPrivateId();

  Long getSessionId();
}
