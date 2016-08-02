package com.budgetview.session.states.impl;

import com.budgetview.client.exceptions.InvalidActionForState;
import com.budgetview.session.states.CreatingUserState;
import com.budgetview.session.states.IdentifiedState;
import com.budgetview.session.states.ConnectingState;
import com.budgetview.session.states.Persistence;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class DefaultConnectingState extends AbstractSessionState implements ConnectingState {
  private Persistence persistence;

  public DefaultConnectingState(Persistence persistence,
                                DefaultSessionStateHandler defaultSessionService,
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
      long version = input.readNotNullLong();
      persistence.connect(output, version);
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

  public void register(SerializedInput input) {
    lastAccess();
    checkPrivateId(input);
    persistence.register(input.readBytes(), input.readBytes(), input.readJavaString());
  }

  public void localDownload(SerializedInput input) {
    lastAccess();
    checkPrivateId(input);
    long version = input.readNotNullLong();
    persistence.setDownloadedVersion(version);
  }

  public String getStateName() {
    return "Connecting";
  }
}
