package org.designup.picsou.server.session.impl;

import org.designup.picsou.server.session.CreatingUserState;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.utils.serialization.SerializedInput;

public class DefaultCreatingUserState extends AbstractSessionState implements CreatingUserState {
  private Persistence persistence;
  private Persistence.UserInfo userInfo;

  public DefaultCreatingUserState(Persistence persistence,
                                  DefaultSessionService defaultSessionService, Long sessionId, byte[] privateId) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
  }

  public String getStateName() {
    return "CreatingUser";
  }

  public void createUser(SerializedInput input) {
    lastAccess();
    String name = input.readUtf8String();
    byte[] encryptedPassword = input.readBytes();
    byte[] linkInfo = input.readBytes();
    byte[] encryptedLinkInfo = input.readBytes();
    userInfo = persistence.createUser(name, false, encryptedPassword, linkInfo, encryptedLinkInfo);
    new DefaultConnectedState(persistence, userInfo.userId, getDefaultSessionService(), getSessionId(), getPrivateId());
  }

  public Boolean getIsRegisteredUser() {
    return userInfo.isRegistered;
  }
}
