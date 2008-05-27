package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.delta.DeltaGlob;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

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
