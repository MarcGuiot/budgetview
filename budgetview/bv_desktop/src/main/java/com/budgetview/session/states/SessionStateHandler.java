package com.budgetview.session.states;

import com.budgetview.client.exceptions.UnknownId;
import org.globsframework.utils.serialization.SerializedOutput;

public interface SessionStateHandler {
  ConnectingState createSessionState();

  SessionState getSessionState(Long sessionId) throws UnknownId;

  void flushStateBefore(long date);

  void getLocalUsers(SerializedOutput output);
}
