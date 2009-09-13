package org.designup.picsou.server.session;

public interface IdentifiedState extends SessionState {
  byte[] getLinkInfo();

  boolean isRegistered();

}
