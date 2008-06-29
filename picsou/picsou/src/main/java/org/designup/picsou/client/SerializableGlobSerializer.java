package org.designup.picsou.client;

import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.DefaultGlob;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Map;

public class SerializableGlobSerializer {
  public void serialize(SerializedOutput output, MapOfMaps<String, Integer, Glob> deltaGlobMap) {
    int globTypeCount = deltaGlobMap.keys().size();
    output.write(globTypeCount);
    for (String stringListEntry : deltaGlobMap.keys()) {
      Map<Integer, Glob> map = deltaGlobMap.get(stringListEntry);
      output.writeString(stringListEntry);
      output.write(map.size());
      for (Glob deltaGlob : map.values()) {
        output.write(deltaGlob.get(SerializableGlobType.ID));
        output.write(deltaGlob.get(SerializableGlobType.VERSION));
        output.writeBytes(deltaGlob.get(SerializableGlobType.DATA));
      }
    }
  }

  public MapOfMaps<String, Integer, Glob> deserialize(SerializedInput serializedInput) {
    MapOfMaps<String, Integer, Glob> multiMap = new MapOfMaps<String, Integer, Glob>();
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        DefaultGlob defaultGlob = new DefaultGlob(SerializableGlobType.TYPE);
        int id = serializedInput.readNotNullInt();
        defaultGlob.set(SerializableGlobType.ID, id);
        defaultGlob.set(SerializableGlobType.GLOB_TYPE_NAME, globTypeName);
        defaultGlob.set(SerializableGlobType.VERSION, serializedInput.readNotNullInt());
        defaultGlob.set(SerializableGlobType.DATA, serializedInput.readBytes());
        multiMap.put(globTypeName, id, defaultGlob);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
    return multiMap;
  }
}
