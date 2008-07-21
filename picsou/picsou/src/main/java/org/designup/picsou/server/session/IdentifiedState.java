package org.designup.picsou.server.session;

public interface IdentifiedState extends SessionState {
  byte[] getPrivateId();

  Long getSessionId();

  byte[] getLinkInfo();

  boolean getIsRegistered();

  byte[] getMail();

  byte[] getKey();
}
