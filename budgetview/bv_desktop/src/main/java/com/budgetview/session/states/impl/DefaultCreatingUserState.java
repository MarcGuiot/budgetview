package com.budgetview.session.states.impl;

import com.budgetview.session.states.CreatingUserState;
import com.budgetview.session.states.Persistence;
import org.globsframework.utils.serialization.SerializedInput;

public class DefaultCreatingUserState extends AbstractSessionState implements CreatingUserState {
  private Persistence persistence;
  private boolean registered;

  public DefaultCreatingUserState(Persistence persistence,
                                  DefaultSessionStateHandler defaultSessionService, Long sessionId, byte[] privateId) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
  }

  public String getStateName() {
    return "CreatingUser";
  }

  public void createUser(SerializedInput input) {
    lastAccess();
    String name = input.readUtf8String();
    boolean autoLog = input.readBoolean();
    byte[] encryptedPassword = input.readBytes();
    byte[] linkInfo = input.readBytes();
    byte[] encryptedLinkInfo = input.readBytes();
    Persistence.UserInfo userInfo =
      persistence.createUser(name, autoLog, false, encryptedPassword, linkInfo, encryptedLinkInfo);
    new DefaultConnectedState(persistence, userInfo.userId, getDefaultSessionService(), getSessionId(), getPrivateId());
    registered = userInfo.isRegistered;
  }

  public Boolean getIsRegisteredUser() {
    return registered;
  }
}
