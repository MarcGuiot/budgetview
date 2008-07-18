package org.designup.picsou.server.session;

import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.globsframework.model.Glob;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface Persistence {
  UserInfo createUser(String name, boolean isRegisteredUser,
                      byte[] encryptedPassword, byte[] linkInfo, byte[] encryptedLinkInfo);

  void getData(SerializedOutput output, Integer userId);

  void updateData(SerializedInput input, SerializedOutput output, Integer userId);

  Glob identify(String name, byte[] encryptedPassword);

  Integer confirmUser(String b64LinkInfo) throws IdentificationFailed;

  void delete(String name, byte[] encryptedPassword, byte[] linkInfo, byte[] encryptedLinkInfo, Integer userId);

  Glob getUser(String name);

  Glob getHiddenUser(byte[] encryptedLinkInfo);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);

  class UserInfo {
    final public Integer userId;
    final public boolean isRegistered;

    public UserInfo(Integer userId, boolean registered) {
      this.userId = userId;
      isRegistered = registered;
    }
  }
}
