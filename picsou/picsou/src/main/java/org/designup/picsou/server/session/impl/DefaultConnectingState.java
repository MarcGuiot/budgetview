package org.designup.picsou.server.session.impl;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.designup.picsou.server.session.ConnectingState;
import org.designup.picsou.server.session.CreatingUserState;
import org.designup.picsou.server.session.IdentifiedState;
import org.designup.picsou.server.session.Persistence;

public class DefaultConnectingState extends AbstractSessionState implements ConnectingState {
  private Persistence persistence;

  public DefaultConnectingState(Persistence persistence,
                                DefaultSessionService defaultSessionService,
                                byte[] privateId,
                                Long sessionId) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
    getDefaultSessionService().register(sessionId, this);
  }

  public IdentifiedState identify(SerializedInput input) {
    lastAccess();
    return new DefaultIdentifiedState(persistence, getDefaultSessionService(), getSessionId(),
                                      getPrivateId(), input);
  }

  public CreatingUserState createUser() {
    lastAccess();
    return new DefaultCreatingUserState(persistence, getDefaultSessionService(), getSessionId(), getPrivateId());
  }

  public String getStateName() {
    return "Connecting";
  }
}
