package org.designup.picsou.server.session;

public interface ConnectingState extends SessionState {
  byte[] getPrivateId();

  Long getSessionId();
}
