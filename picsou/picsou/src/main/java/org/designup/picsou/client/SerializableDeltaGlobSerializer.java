package org.designup.picsou.client;

import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.globsframework.utils.MultiMap;
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

  public static void serialize(SerializedOutput output, MultiMap<String, ServerDelta> deltaGlobMap) {
    int globTypeCount = deltaGlobMap.keySet().size();
    output.write(globTypeCount);
    for (Map.Entry<String, List<ServerDelta>> stringListEntry : deltaGlobMap.entries()) {
      output.writeJavaString(stringListEntry.getKey());
      output.write(stringListEntry.getValue().size());
      for (ServerDelta deltaGlob : stringListEntry.getValue()) {
        output.write(deltaGlob.getState().getId());
        output.write(deltaGlob.getId());
        if (deltaGlob.getState() == ServerState.CREATED || deltaGlob.getState() == ServerState.UPDATED) {
          output.write(deltaGlob.getVersion());
          output.writeBytes(deltaGlob.getData());
        }
      }
    }
  }

  public MultiMap<String, ServerDelta> deserialize(SerializedInput serializedInput) {
    MultiMap<String, ServerDelta> multiMap = new MultiMap<String, ServerDelta>();
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int state = serializedInput.readNotNullInt();
        ServerState deltaState = ServerState.get(state);
        ServerDelta delta = new ServerDelta(serializedInput.readNotNullInt());
        delta.setState(deltaState);
        if (deltaState == ServerState.CREATED || deltaState == ServerState.UPDATED) {
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
