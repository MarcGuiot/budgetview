package org.designup.picsou.server.session.impl;

import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.designup.picsou.server.session.ConnectingState;
import org.designup.picsou.server.session.CreatingUserState;
import org.designup.picsou.server.session.IdentifiedState;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

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

  public ConnectingState connect(SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    lastAccess();
    Boolean isLocal = input.readBoolean();
    if (isLocal) {
      persistence.connect(output);
    }
    else {
      output.write(false);
    }
    return this;
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
