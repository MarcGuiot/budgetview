package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.YANBuffereInputStream;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ReadOnlyAccountDataManager {

  public static class SnapshotInfo{
    public final long version;
    public final long timestamp;
    public final char[] password; //of autolog user

    public SnapshotInfo(long version, char[] password, long timestamp) {
      this.version = version;
      this.password = password;
      this.timestamp = timestamp;
    }
  }

  public static SnapshotInfo readSnapshot(MapOfMaps<String, Integer, SerializableGlobType> globs, InputStream fileStream) {
    InputStream inputStream = null;
    try {
      inputStream = new YANBuffereInputStream(fileStream);
      SerializedInput serializedInput = SerializedInputOutputFactory.init(inputStream);
      String version = serializedInput.readJavaString();
      if ("2".equals(version)) {
        if (globs != null){
          readVersion2(serializedInput, globs);
        }
        return new SnapshotInfo(19, null, -1);
      }
      if ("3".equals(version)) {
        return readVersion3(serializedInput, globs);
      }
      if ("4".equals(version)){
        return readVersion4(serializedInput, globs);
      }
      if ("5".equals(version)){
        return readVersion5(serializedInput, globs);
      }
    }
    finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        }
        catch (IOException e) {
        }
      }
    }
    return null;
  }

  private static void readVersion2(SerializedInput serializedInput,
                                   MapOfMaps<String, Integer, SerializableGlobType> data) {
    SerializableGlobSerializer.deserialize(serializedInput, data);
  }

  private static SnapshotInfo readVersion3(SerializedInput serializedInput,
                                     MapOfMaps<String, Integer, SerializableGlobType> data) {
    String password = serializedInput.readJavaString();
    long version = serializedInput.readNotNullLong();
    if (data != null){
      SerializableGlobSerializer.deserialize(serializedInput, data);
    }
    return new SnapshotInfo(version, password == null ? null : password.toCharArray(), -1);
  }

  private static SnapshotInfo readVersion4(SerializedInput serializedInput,
                                           MapOfMaps<String, Integer, SerializableGlobType> data) {
    String password = serializedInput.readJavaString();
    long version = serializedInput.readNotNullLong();
    long timestamp = serializedInput.readNotNullLong();
    if (data != null){
      SerializableGlobSerializer.deserialize(serializedInput, data);
    }
    return new SnapshotInfo(version, password == null ? null : password.toCharArray(), timestamp);
  }

   private static SnapshotInfo readVersion5(SerializedInput serializedInput,
                                           MapOfMaps<String, Integer, SerializableGlobType> data) {
    String password = serializedInput.readUtf8String();
    long version = serializedInput.readNotNullLong();
    long timestamp = serializedInput.readNotNullLong();
    if (data != null){
      SerializableGlobSerializer.deserialize(serializedInput, data);
    }
    return new SnapshotInfo(version, password == null ? null : password.toCharArray(), timestamp);
  }


  public static void writeSnapshot(MapOfMaps<String, Integer, SerializableGlobType> data, File file,
                                      char[] password, long version, long timestamp) throws IOException {
    writeSnapshot_V5(data, file, password, version, timestamp);
  }

  public static void writeSnapshot_V5(MapOfMaps<String, Integer, SerializableGlobType> data, File file,
                                      char[] password, long version, long timestamp) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(file);
    SerializedOutput serializedOutput = SerializedInputOutputFactory.init(outputStream);
    serializedOutput.writeJavaString("5");
    if (password != null) {
      serializedOutput.writeUtf8String(new String(password));
    }
    else {
      serializedOutput.writeUtf8String(null);
    }
    serializedOutput.write(version);
    serializedOutput.write(timestamp);
    SerializableGlobSerializer.serialize(serializedOutput, data);
    outputStream.close();
  }

  protected static void apply(MapOfMaps<String, Integer, SerializableGlobType> globs, MultiMap<String, ServerDelta> map) {
    for (Map.Entry<String, List<ServerDelta>> stringListEntry : map.entries()) {
      Map<Integer, SerializableGlobType> globToMerge = globs.get(stringListEntry.getKey());
      for (ServerDelta deltaGlob : stringListEntry.getValue()) {
        if (deltaGlob.getState() == ServerState.DELETED) {
          globToMerge.remove(deltaGlob.getId());
        }
        else {
          globToMerge.put(deltaGlob.getId(), new SerializableGlobType(stringListEntry.getKey(), deltaGlob));
        }
      }
    }
  }
}
