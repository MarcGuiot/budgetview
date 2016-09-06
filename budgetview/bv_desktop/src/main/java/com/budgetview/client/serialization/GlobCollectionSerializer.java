package com.budgetview.client.serialization;

import com.budgetview.session.serialization.SerializedGlob;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Map;

public class GlobCollectionSerializer {
  public static void serialize(SerializedOutput output,
                               MapOfMaps<String, Integer, SerializedGlob> dataByGlobTypeAndId) {
    int globTypeCount = dataByGlobTypeAndId.keys().size();
    output.write(globTypeCount);
    for (String globTypeName : dataByGlobTypeAndId.keys()) {
      Map<Integer, SerializedGlob> dataById = dataByGlobTypeAndId.get(globTypeName);
      if (globTypeName.equals("r")) {
        globTypeName = "accountPositionError";
      }
      else if (globTypeName.equals("bb") || globTypeName.equals("ab")) {
        globTypeName = "csvMapping";
      }
      output.writeJavaString(globTypeName);
      output.write(dataById.size());
      for (SerializedGlob data : dataById.values()) {
        output.write(data.getId());
        output.write(data.getVersion());
        output.writeBytes(data.getData());
      }
    }
  }

  public static void deserialize(SerializedInput serializedInput,
                                 MapOfMaps<String, Integer, SerializedGlob> data) {
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String typeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int id = serializedInput.readNotNullInt();
        SerializedGlob defaultGlob = new SerializedGlob(typeName, id, serializedInput.readNotNullInt(), serializedInput.readBytes());
        data.put(typeName, id, defaultGlob);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
  }
}
