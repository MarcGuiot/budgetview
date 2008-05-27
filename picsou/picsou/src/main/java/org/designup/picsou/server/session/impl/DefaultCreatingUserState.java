package org.designup.picsou.server.session.impl;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.designup.picsou.server.session.CreatingUserState;
import org.designup.picsou.server.session.Persistence;

public class DefaultCreatingUserState extends AbstractSessionState implements CreatingUserState {
  private Persistence persistence;
  private RootDataManager.UserInfo userInfo;

  public DefaultCreatingUserState(Persistence persistence,
                                  DefaultSessionService defaultSessionService, Long sessionId, byte[] privateId) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
    defaultSessionService.register(sessionId, this);
  }

  public String getStateName() {
    return "CreatingUser";
  }

  public void createUser(SerializedInput input) {
    lastAccess();
    String name = input.readString();
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
