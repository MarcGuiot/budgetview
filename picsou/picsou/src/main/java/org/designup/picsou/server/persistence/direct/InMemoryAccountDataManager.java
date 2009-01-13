package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.InputStream;

public class InMemoryAccountDataManager implements AccountDataManager {
  private InputStream inputStream;

  public InMemoryAccountDataManager(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    ReadOnlyAccountDataManager.readSnapshot(globs, inputStream);
    SerializableGlobSerializer.serialize(output, globs);
  }

  public void updateUserData(SerializedInput input, Integer userId) {
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  public void delete(Integer userId) {
  }

  public void close() {
  }

  public void close(Integer userId) {
  }

  public void takeSnapshot(Integer userId) {
  }

  public boolean restore(SerializedInput input, Integer userId) {
    return false;
  }
}
