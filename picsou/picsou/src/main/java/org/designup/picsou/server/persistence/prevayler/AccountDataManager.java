package org.designup.picsou.server.persistence.prevayler;

import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface AccountDataManager {
  void getUserData(SerializedOutput output, Integer userId);

  void updateUserData(SerializedInput input, Integer userId);

  Integer getNextId(String globTypeName, Integer userId, Integer count);

  void delete(Integer userId);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);

  boolean restore(SerializedInput input, Integer userId);
}
