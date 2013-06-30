package org.designup.picsou.server.persistence.prevayler;

import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.List;

public interface AccountDataManager {
  void getUserData(SerializedOutput output, Integer userId);

  void updateUserData(SerializedInput input, Integer userId);

  Integer getNextId(String globTypeName, Integer userId, Integer count);

  void delete(Integer userId);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);

  boolean restore(SerializedInput input, Integer userId);

  boolean newData(Integer userId, SerializedInput input);

  List<SnapshotInfo> getSnapshotInfos(Integer userId);

  void getSnapshotData(Integer userId, String snapshotInfo, final SerializedOutput output);

  boolean hasChanged(Integer userId);

  public class SnapshotInfo {
    public final long timestamp;
    public char[] password;
    public long version;
    public final String fileName;

    public SnapshotInfo(long timestamp, char[] password, long version, String fileName) {
      this.timestamp = timestamp;
      this.password = password;
      this.version = version;
      this.fileName = fileName;
    }
  }
}
