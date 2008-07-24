package org.designup.picsou.server.session;

import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface SessionState {

  String getStateName();

  ConnectingState connect(SerializedInput input, SerializedOutput output) throws InvalidActionForState;

  IdentifiedState identify(SerializedInput input) throws InvalidActionForState;

  void confirmUser(SerializedInput input) throws InvalidActionForState;

  CreatingUserState createUser() throws InvalidActionForState;

  ConnectedState connected() throws InvalidActionForState;

  void disconnect(SerializedInput input);

  long getLastAccess();

  void register(SerializedInput input);
}
