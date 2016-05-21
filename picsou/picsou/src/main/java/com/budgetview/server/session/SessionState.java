package com.budgetview.server.session;

import com.budgetview.client.exceptions.InvalidActionForState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface SessionState {

  String getStateName();

  ConnectingState connect(SerializedInput input, SerializedOutput output) throws InvalidActionForState;

  IdentifiedState identify(SerializedInput input) throws InvalidActionForState;

  void confirmUser(SerializedInput input) throws InvalidActionForState;

  CreatingUserState createUser() throws InvalidActionForState;

  void deleteUser(SerializedInput input);

  ConnectedState connected() throws InvalidActionForState;

  void disconnect(SerializedInput input);

  long getLastAccess();

  void register(SerializedInput input);

  void localDownload(SerializedInput input);

  void setLang(SerializedInput input);
}
