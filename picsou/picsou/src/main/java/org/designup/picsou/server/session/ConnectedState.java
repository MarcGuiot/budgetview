package org.designup.picsou.server.session;

import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface ConnectedState extends SessionState {
  void getUserData(SerializedInput input, SerializedOutput output);

  void updateData(SerializedInput transactionsInput, SerializedOutput output);

  void getUserId(SerializedInput input, SerializedOutput output);

  void takeSnapshot(SerializedInput input);

  void restore(SerializedInput input, SerializedOutput output);
}
