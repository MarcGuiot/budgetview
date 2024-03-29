package com.budgetview.session.states.impl;

import com.budgetview.client.exceptions.InvalidActionForState;
import com.budgetview.session.states.*;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Arrays;

public abstract class AbstractSessionState implements SessionState {
  private long lastAccess;
  private DefaultSessionStateHandler defaultSessionService;
  private Long sessionId;
  private byte[] privateId;

  public AbstractSessionState(DefaultSessionStateHandler defaultSessionService, Long sessionId, byte[] privateId) {
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

  public void register(SerializedInput input) {
    throw new InvalidActionForState("register", getStateName());
  }

  public long getLastAccess() {
    return lastAccess;
  }

  public CreatingUserState createUser() {
    throw new InvalidActionForState("createUser", getStateName());
  }

  public void deleteUser(SerializedInput input) {
    throw new InvalidActionForState("deleteUser", getStateName());
  }

  public void localDownload(SerializedInput input) {
    throw new InvalidActionForState("localDownload", getStateName());
  }

  public void setLang(SerializedInput input) {
    throw new InvalidActionForState("setLang", getStateName());
  }

  public abstract String getStateName();

  public ConnectingState connect(SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    throw new InvalidActionForState("connect", getStateName());
  }

  protected void checkPrivateId(SerializedInput input) {
    byte[] actualPrivateId = input.readBytes();
    if (!Arrays.equals(actualPrivateId, privateId)) {
      defaultSessionService.remove(sessionId);
      throw new InvalidData("Not reconized");
    }
  }

  public DefaultSessionStateHandler getDefaultSessionService() {
    return defaultSessionService;
  }

  public byte[] getPrivateId() {
    return privateId;
  }

  public Long getSessionId() {
    return sessionId;
  }
}
