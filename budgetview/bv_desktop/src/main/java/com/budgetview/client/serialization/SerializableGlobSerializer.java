package com.budgetview.client.serialization;

import com.budgetview.server.model.SerializableGlobType;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Map;

public class SerializableGlobSerializer {
  public static void serialize(SerializedOutput output,
                               MapOfMaps<String, Integer, SerializableGlobType> dataByGlobTypeAndId) {
    int globTypeCount = dataByGlobTypeAndId.keys().size();
    output.write(globTypeCount);
    for (String globTypeName : dataByGlobTypeAndId.keys()) {
      Map<Integer, SerializableGlobType> dataById = dataByGlobTypeAndId.get(globTypeName);
      if (globTypeName.equals("r")){
        globTypeName = "accountPositionError";
      }else if (globTypeName.equals("bb") || globTypeName.equals("ab")){
        globTypeName = "csvMapping";
      }
      output.writeJavaString(globTypeName);
      output.write(dataById.size());
      for (SerializableGlobType data : dataById.values()) {
        output.write(data.getId());
        output.write(data.getVersion());
        output.writeBytes(data.getData());
      }
    }
  }

  public static void deserialize(SerializedInput serializedInput,
                                 MapOfMaps<String, Integer, SerializableGlobType> data) {
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
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
