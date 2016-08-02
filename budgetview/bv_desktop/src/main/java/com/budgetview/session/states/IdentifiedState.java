package com.budgetview.session.states;

public interface IdentifiedState extends SessionState {
  byte[] getLinkInfo();

  boolean isRegistered();

}
