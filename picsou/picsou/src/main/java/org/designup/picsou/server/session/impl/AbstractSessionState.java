package org.designup.picsou.server.session.impl;

import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.designup.picsou.server.session.ConnectedState;
import org.designup.picsou.server.session.CreatingUserState;
import org.designup.picsou.server.session.IdentifiedState;
import org.designup.picsou.server.session.SessionState;

import java.util.Arrays;

public abstract class AbstractSessionState implements SessionState {
  private long lastAccess;
  private DefaultSessionService defaultSessionService;
  private Long sessionId;
  private byte[] privateId;

  public AbstractSessionState(DefaultSessionService defaultSessionService, Long sessionId, byte[] privateId) {
    lastAccess = System.currentTimeMillis();
    this.defaultSessionService = defaultSessionService;
    this.sessionId = sessionId;
    this.privateId = privateId;
  }

  protected void lastAccess() {
    lastAccess = System.currentTimeMillis();
  }

  public IdentifiedState identify(SerializedInput input) {
    throw new InvalidActionForState("identify", getStateName());
  }

  public void confirmUser(SerializedInput request) {
    throw new InvalidActionForState("confirmUser", getStateName());
  }

  public ConnectedState connected() {
    throw new InvalidActionForState("connected", getStateName());
  }

  public void disconnect(SerializedInput input) {
    checkPrivateId(input);
    defaultSessionService.remove(sessionId);
  }

  public long getLastAccess() {
    return lastAccess;
  }

  public CreatingUserState createUser() {
    throw new InvalidActionForState("createUser", getStateName());
  }

  public void getNextId(SerializedInput input, SerializedOutput response) {
    throw new InvalidActionForState("getNextId", getStateName());
  }

  public abstract String getStateName();

  protected void checkPrivateId(SerializedInput input) {
    byte[] actualPrivateId = input.readBytes();
    if (!Arrays.equals(actualPrivateId, privateId)) {
      defaultSessionService.remove(sessionId);
      throw new InvalidData("Not reconized");
    }
  }

  public DefaultSessionService getDefaultSessionService() {
    return defaultSessionService;
  }

  public byte[] getPrivateId() {
    return privateId;
  }

  public Long getSessionId() {
    return sessionId;
  }
}
