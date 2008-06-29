package org.designup.picsou.client;

import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.model.impl.TwoFieldKey;
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

  public void serialize(SerializedOutput output, MultiMap<String, DeltaGlob> deltaGlobMap) {
    int globTypeCount = deltaGlobMap.keySet().size();
    output.write(globTypeCount);
    for (Map.Entry<String, List<DeltaGlob>> stringListEntry : deltaGlobMap.values()) {
      output.writeString(stringListEntry.getKey());
      output.write(stringListEntry.getValue().size());
      for (DeltaGlob deltaGlob : stringListEntry.getValue()) {
        write(output, deltaGlob.getState());
        output.write(deltaGlob.get(SerializableGlobType.ID));
        if (deltaGlob.getState() == DeltaState.CREATED || deltaGlob.getState() == DeltaState.UPDATED) {
          output.write(deltaGlob.get(SerializableGlobType.VERSION));
          output.writeBytes(deltaGlob.get(SerializableGlobType.DATA));
        }
      }
    }
  }

  public MultiMap<String, DeltaGlob> deserialize(SerializedInput serializedInput) {
    MultiMap<String, DeltaGlob> multiMap = new MultiMap<String, DeltaGlob>();
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int state = serializedInput.readNotNullInt();
        DeltaState deltaState = null;
        if (state == 0) {
          deltaState = DeltaState.CREATED;
        }
        if (state == 1) {
          deltaState = DeltaState.UPDATED;
        }
        if (state == 2) {
          deltaState = DeltaState.DELETED;
        }
        if (state == 3) {
          deltaState = DeltaState.UNCHANGED;
        }
        DeltaGlob delta =
          new DefaultDeltaGlob(new TwoFieldKey(SerializableGlobType.ID, serializedInput.readNotNullInt(),
                                               SerializableGlobType.GLOB_TYPE_NAME, globTypeName));
        delta.setState(deltaState);
        if (deltaState == DeltaState.CREATED || deltaState == DeltaState.UPDATED) {
          delta.set(SerializableGlobType.VERSION, serializedInput.readNotNullInt());
          delta.set(SerializableGlobType.DATA, serializedInput.readBytes());
        }
        multiMap.put(globTypeName, delta);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
    return multiMap;
  }

  private void write(SerializedOutput output, DeltaState state) {
    if (state == DeltaState.CREATED) {
      output.write(0);
    }
    else if (state == DeltaState.UPDATED) {
      output.write(1);
    }
    else if (state == DeltaState.DELETED) {
      output.write(2);
    }
    else if (state == DeltaState.UNCHANGED) {
      output.write(3);
    }
  }
}
