package org.designup.picsou.server;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.designup.picsou.server.session.*;

public class DefaultServerRequestProcessingService implements ServerRequestProcessingService {
  private SessionService sessionService;

  public DefaultServerRequestProcessingService(Directory directory) {
    sessionService = directory.get(SessionService.class);
  }

  public void identify(SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    ConnectingState connectingState = sessionService.createSessionState();
    IdentifiedState identifiedState = connectingState.identify(input);
    output.writeLong(identifiedState.getSessionId());
    output.writeBytes(identifiedState.getPrivateId());
    output.writeBytes(identifiedState.getLinkInfo());
    output.writeBoolean(identifiedState.getIsRegistered());
  }

  public void createUser(SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    ConnectingState sessionState = sessionService.createSessionState();
    CreatingUserState creatingUserState = sessionState.createUser();
    creatingUserState.createUser(input);
    output.writeLong(creatingUserState.getSessionId());
    output.writeBytes(creatingUserState.getPrivateId());
    output.writeBoolean(creatingUserState.getIsRegisteredUser());
  }

  public void confirmUser(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState state = sessionService.getSessionState(sessionId);
    state.confirmUser(input);
  }

  public void getUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState sessionState = sessionService.getSessionState(sessionId);
    ConnectedState connected = sessionState.connected();
    connected.getUserData(input, output);
  }

  public void addUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().updateData(input, output);
  }

  public void getNextId(Long sessionId, SerializedInput input, SerializedOutput output) throws InvalidActionForState {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().getNextId(input, output);
  }

  public void disconnect(Long sessionId, SerializedInput input) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.disconnect(input);
  }

  public void takeSnapshot(Long sessionId, SerializedInput input) {
    SessionState state = sessionService.getSessionState(sessionId);
    state.connected().takeSnapshot(input);
  }
}
