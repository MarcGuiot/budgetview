package com.budgetview.session.states;

public interface ConnectingState extends SessionState {
  byte[] getPrivateId();

  Long getSessionId();
}
