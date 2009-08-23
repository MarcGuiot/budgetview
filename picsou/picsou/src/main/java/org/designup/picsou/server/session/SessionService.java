package org.designup.picsou.server.session;

import org.designup.picsou.client.exceptions.UnknownId;
import org.globsframework.utils.serialization.SerializedOutput;

public interface SessionService {
  ConnectingState createSessionState();

  SessionState getSessionState(Long sessionId) throws UnknownId;

  void flushStateBefore(long date);

  void getLocalUsers(SerializedOutput output);
}
