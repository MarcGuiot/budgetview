package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.model.GlobList;

import java.io.InputStream;
import java.util.List;
import java.util.Collections;

public class InMemoryAccountDataManager implements AccountDataManager {
  private MapOfMaps<String, Integer, SerializableGlobType> globs;

  public InMemoryAccountDataManager(InputStream inputStream) {
    globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    if (inputStream != null) {
      ReadOnlyAccountDataManager.readSnapshot(globs, inputStream);
    }
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    SerializableGlobSerializer.serialize(output, globs);
  }

  public void updateUserData(SerializedInput input, Integer userId) {
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    MultiMap<String, ServerDelta> data = serializableDeltaGlobSerializer.deserialize(input);
    ReadOnlyAccountDataManager.apply(globs, data);
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  public void delete(Integer userId) {
    globs = new MapOfMaps<String, Integer, SerializableGlobType>();
  }

  public void close() {
  }

  public void close(Integer userId) {
  }

  public void takeSnapshot(Integer userId) {
  }

  public boolean restore(SerializedInput input, Integer userId) {
    globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, globs);
    return true;
  }

  public boolean newData(Integer userId, SerializedInput input) {
    globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, globs);
    return true;
  }

  public List<SnapshotInfo> getSnapshotInfos(Integer userId) {
    return Collections.emptyList();
  }

  public void getSnapshotData(Integer userId, String snapshotInfo, SerializedOutput output) {
  }

  public GlobList getLocalUsers() {
    return GlobList.EMPTY;
  }
}
