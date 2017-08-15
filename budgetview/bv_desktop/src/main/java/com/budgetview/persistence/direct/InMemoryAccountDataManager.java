package com.budgetview.persistence.direct;

import com.budgetview.client.serialization.GlobCollectionSerializer;
import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import com.budgetview.persistence.prevayler.AccountDataManager;
import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializedGlob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class InMemoryAccountDataManager implements AccountDataManager {
  private MapOfMaps<String, Integer, SerializedGlob> globs;

  public InMemoryAccountDataManager(InputStream inputStream) {
    globs = new MapOfMaps<String, Integer, SerializedGlob>();
    if (inputStream != null) {
      ReadOnlyAccountDataManager.readSnapshot(globs, inputStream);
    }
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    GlobCollectionSerializer.serialize(output, globs);
  }

  public void updateUserData(SerializedInput input, Integer userId) {
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    MultiMap<String, SerializedDelta> data = serializableDeltaGlobSerializer.deserialize(input);
    ReadOnlyAccountDataManager.apply(globs, data);
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  public void delete(Integer userId) {
    globs = new MapOfMaps<String, Integer, SerializedGlob>();
  }

  public void close() {
  }

  public void close(Integer userId) {
  }

  public void takeSnapshot(Integer userId) {
  }

  public boolean restore(SerializedInput input, Integer userId) {
    globs = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, globs);
    return true;
  }

  public boolean newData(Integer userId, SerializedInput input) {
    globs = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, globs);
    return true;
  }

  public List<SnapshotInfo> getSnapshotInfos(Integer userId) {
    return Collections.emptyList();
  }

  public void getSnapshotData(Integer userId, String snapshotInfo, SerializedOutput output) {
  }

  public boolean hasChanged(Integer userId) {
    return false;
  }

  public GlobList getLocalUsers() {
    return GlobList.EMPTY;
  }
}
