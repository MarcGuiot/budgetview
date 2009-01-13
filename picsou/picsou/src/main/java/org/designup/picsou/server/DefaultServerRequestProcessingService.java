package org.designup.picsou.server;

import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.designup.picsou.server.session.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class DefaultServerRequestProcessingService implements ServerRequestProcessingService {
  private SessionService sessionService;

  public DefaultServerRequestProcessingService(Directory directory) {
    sessionService = directory.get(SessionService.class);
  }

  public void connect(SerializedInput input, SerializedOutput output) {
    ConnectingState connectingState = sessionService.createSessionState();
    connectingState.connect(input, output);
    output.writeLong(connectingState.getSessionId());
    output.writeBytes(connectingState.getPrivateId());
  }

  public void identify(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState connectingState = sessionService.getSessionState(sessionId);
    IdentifiedState identifiedState = connectingState.identify(input);
    output.writeBytes(identifiedState.getLinkInfo());
    output.writeBoolean(identifiedState.getIsRegistered());
  }

  public void createUser(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState connectingState = sessionService.getSessionState(sessionId);
    CreatingUserState creatingUserState = connectingState.createUser();
    creatingUserState.createUser(input);
    output.writeBoolean(creatingUserState.getIsRegisteredUser());
  }

  public void confirmUser(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState state = sessionService.getSessionState(sessionId);
    state.confirmUser(input);
  }

  public void register(Long sessionId, SerializedInput input) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.register(input);
  }

  public void getUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState sessionState = sessionService.getSessionState(sessionId);
    ConnectedState connected = sessionState.connected();
    connected.getUserData(input, output);
  }

  public void getUserId(Long sessionId, SerializedInput input, SerializedOutput output) {
    SessionState sessionState = sessionService.getSessionState(sessionId);
    ConnectedState connected = sessionState.connected();
    connected.getUserId(input, output);
  }

  public void addUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().updateData(input, output);
  }

  public void disconnect(Long sessionId, SerializedInput input) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.disconnect(input);
  }

  public void takeSnapshot(Long sessionId, SerializedInput input) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().takeSnapshot(input);
  }

  public void restore(Long sessionId, SerializedInput input, SerializedOutput output) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().restore(input, output);
  }
}
