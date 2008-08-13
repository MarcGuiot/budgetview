package org.designup.picsou.server.session.impl;

import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.IdentifiedState;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;

public class DefaultIdentifiedState extends AbstractSessionState implements IdentifiedState {
  private Glob user;
  private Persistence persistence;

  public DefaultIdentifiedState(Persistence persistence,
                                DefaultSessionService defaultSessionService,
                                Long sessionId,
                                byte[] privateId,
                                SerializedInput input) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
    String name = input.readString();
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

  public byte[] getLinkInfo() {
    return user.get(User.LINK_INFO);
  }

  public boolean getIsRegistered() {
    return user.get(User.IS_REGISTERED_USER);
  }
}
