package org.designup.picsou.server.session;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

public interface ConnectedState extends SessionState {
  void getUserData(SerializedInput input, SerializedOutput output);

  void updateData(SerializedInput transactionsInput, SerializedOutput output);

  void getNextId(SerializedInput input, SerializedOutput output);

  void takeSnapshot(SerializedInput input);
}
