package org.designup.picsou.client;

import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Map;

public class SerializableGlobSerializer {
  static public void serialize(SerializedOutput output,
                               MapOfMaps<String, Integer, SerializableGlobType> dataByGlobTypeAndId) {
    int globTypeCount = dataByGlobTypeAndId.keys().size();
    output.write(globTypeCount);
    for (String globTypeName : dataByGlobTypeAndId.keys()) {
      Map<Integer, SerializableGlobType> dataById = dataByGlobTypeAndId.get(globTypeName);
      output.writeString(globTypeName);
      output.write(dataById.size());
      for (SerializableGlobType data : dataById.values()) {
        output.write(data.getId());
        output.write(data.getVersion());
        output.writeBytes(data.getData());
      }
    }
  }

  static public void deserialize(SerializedInput serializedInput,
                                 MapOfMaps<String, Integer, SerializableGlobType> data) {
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        SerializableGlobType defaultGlob = new SerializableGlobType();
        int id = serializedInput.readNotNullInt();
        defaultGlob.setId(id);
        defaultGlob.setGlobTypeName(globTypeName);
        defaultGlob.setVersion(serializedInput.readNotNullInt());
        defaultGlob.setData(serializedInput.readBytes());
        data.put(globTypeName, id, defaultGlob);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
  }
}
