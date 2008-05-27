package org.designup.picsou.server.session;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.designup.picsou.client.exceptions.InvalidActionForState;

public interface SessionState {

  String getStateName();

  IdentifiedState identify(SerializedInput input) throws InvalidActionForState;

  void confirmUser(SerializedInput input) throws InvalidActionForState;

  CreatingUserState createUser() throws InvalidActionForState;

  ConnectedState connected() throws InvalidActionForState;

  void disconnect(SerializedInput input);

  long getLastAccess();
}
