package org.designup.picsou.server.persistence.prevayler;

import org.globsframework.model.GlobList;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.List;

public interface AccountDataManager {
  void getUserData(SerializedOutput output, Integer userId);

  void updateUserData(List<DeltaGlob> output, Integer userId);

  Integer getNextId(String globTypeName, Integer userId, Integer count);

  void delete(Integer userId);

  GlobList getUserData(Integer userId);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);
}
