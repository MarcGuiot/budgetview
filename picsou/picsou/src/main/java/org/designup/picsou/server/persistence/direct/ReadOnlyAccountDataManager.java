package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ReadOnlyAccountDataManager {

  public static void readSnapshot(MapOfMaps<String, Integer, SerializableGlobType> globs, InputStream fileStream) {
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(fileStream);
      SerializedInput serializedInput = SerializedInputOutputFactory.init(inputStream);
      String version = serializedInput.readString();
      if ("2".equals(version)) {
        readVersion2(serializedInput, globs);
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
  }

  private static void readVersion2(SerializedInput serializedInput,
                                   MapOfMaps<String, Integer, SerializableGlobType> data) {
    SerializableGlobSerializer.deserialize(serializedInput, data);
  }

  public static void writeSnapshot_V2(MapOfMaps<String, Integer, SerializableGlobType> data, File file) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(file);
    SerializedOutput serializedOutput = SerializedInputOutputFactory.init(outputStream);
    serializedOutput.writeString("2");
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
