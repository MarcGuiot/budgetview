package com.budgetview.session.states.impl;

import com.budgetview.client.exceptions.IdentificationFailed;
import com.budgetview.session.model.User;
import com.budgetview.session.states.IdentifiedState;
import com.budgetview.session.states.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;

public class DefaultIdentifiedState extends AbstractSessionState implements IdentifiedState {
  private Glob user;
  private Persistence persistence;

  public DefaultIdentifiedState(Persistence persistence,
                                DefaultSessionStateHandler defaultSessionService,
                                Long sessionId,
                                byte[] privateId,
                                SerializedInput input) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
    String name = input.readUtf8String();
    byte[] cryptedPassword = input.readBytes();
    user = identify(name, cryptedPassword);
    defaultSessionService.register(sessionId, this);
  }

  private Glob identify(String name, byte[] cryptedPassword) {
    return persistence.identify(name, cryptedPassword);
  }

  public String getStateName() {
    return "Identified";
  }

  public void confirmUser(SerializedInput input) throws IdentificationFailed {
    lastAccess();
    checkPrivateId(input);
    byte[] cryptedLinkInfo = input.readBytes();
    Integer userId = persistence.confirmUser(Encoder.byteToString(cryptedLinkInfo));
    new DefaultConnectedState(persistence, userId, getDefaultSessionService(),
                              getSessionId(), getPrivateId());

  }

  public void deleteUser(SerializedInput input) {
    checkPrivateId(input);
    byte[] cryptedLinkInfo = input.readBytes();
    Integer userId = persistence.confirmUser(Encoder.byteToString(cryptedLinkInfo));
    persistence.delete(user.get(User.NAME), cryptedLinkInfo, userId);
  }

  public byte[] getLinkInfo() {
    return user.get(User.LINK_INFO);
  }

  public boolean isRegistered() {
    return user.isTrue(User.IS_REGISTERED_USER);
  }
}
