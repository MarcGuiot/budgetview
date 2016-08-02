package com.budgetview.client.serialization;

import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializedDeltaState;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.List;
import java.util.Map;

public class SerializableDeltaGlobSerializer {
  // format :
  // count globType
  //  globTypeName
  //  count Glob
  //    deltaType : create/update/delete
  //    SerializableGlobType.ID
  //    SerializableGlobType.VERSION (for create and update)
  //    SerializableGlobType.DATA (for create and update)

  public static void serialize(SerializedOutput output, MultiMap<String, SerializedDelta> deltaGlobMap) {
    int globTypeCount = deltaGlobMap.keySet().size();
    output.write(globTypeCount);
    for (Map.Entry<String, List<SerializedDelta>> stringListEntry : deltaGlobMap.entries()) {
      output.writeJavaString(stringListEntry.getKey());
      output.write(stringListEntry.getValue().size());
      for (SerializedDelta deltaGlob : stringListEntry.getValue()) {
        output.write(deltaGlob.getState().getId());
        output.write(deltaGlob.getId());
        if (deltaGlob.getState() == SerializedDeltaState.CREATED || deltaGlob.getState() == SerializedDeltaState.UPDATED) {
          output.write(deltaGlob.getVersion());
          output.writeBytes(deltaGlob.getData());
        }
      }
    }
  }

  public static MultiMap<String, SerializedDelta> deserialize(SerializedInput serializedInput) {
    MultiMap<String, SerializedDelta> multiMap = new MultiMap<String, SerializedDelta>();
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int state = serializedInput.readNotNullInt();
        SerializedDeltaState deltaState = SerializedDeltaState.get(state);
        SerializedDelta delta = new SerializedDelta(serializedInput.readNotNullInt());
        delta.setState(deltaState);
        if (deltaState == SerializedDeltaState.CREATED || deltaState == SerializedDeltaState.UPDATED) {
          delta.setVersion(serializedInput.readNotNullInt());
          delta.setData(serializedInput.readBytes());
        }
        multiMap.put(globTypeName, delta);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
    return multiMap;
  }
}
