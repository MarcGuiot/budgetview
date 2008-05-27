package org.designup.picsou.server.session;

import org.designup.picsou.client.exceptions.UnknownId;

public interface SessionService {
  ConnectingState createSessionState();

  SessionState getSessionState(Long sessionId) throws UnknownId;

  void flushStateBefore(long date);
}
