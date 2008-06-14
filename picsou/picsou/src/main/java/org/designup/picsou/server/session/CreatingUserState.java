package org.designup.picsou.server.session;

import org.globsframework.utils.serialization.SerializedInput;

public interface CreatingUserState extends SessionState {

  void createUser(SerializedInput input);

  byte[] getPrivateId();

  Long getSessionId();

  Boolean getIsRegisteredUser();
}
