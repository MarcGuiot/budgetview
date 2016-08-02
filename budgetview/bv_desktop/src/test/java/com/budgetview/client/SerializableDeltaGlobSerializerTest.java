package com.budgetview.client;

import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import junit.framework.TestCase;
import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializedDeltaState;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

import java.util.List;

public class SerializableDeltaGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializableDeltaGlobSerializer deltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, SerializedDelta> map = new MultiMap<String, SerializedDelta>();

    map.put("Toto", createDelta(1, SerializedDeltaState.CREATED));
    map.put("Toto", createDelta(2, SerializedDeltaState.CREATED));
    map.put("Titi", createDelta(1, SerializedDeltaState.DELETED));
    map.put("Titi", createDelta(2, SerializedDeltaState.UPDATED));
    deltaGlobSerializer.serialize(output.getOutput(), map);
    MultiMap<String, SerializedDelta> result = deltaGlobSerializer.deserialize(output.getInput());
    assertEquals(4, result.size());
    List<SerializedDelta> totoGlobs = result.get("Toto");
    assertEquals(2, totoGlobs.size());
    assertEquals(SerializedDeltaState.CREATED, totoGlobs.get(0).getState());
    assertEquals(SerializedDeltaState.CREATED, totoGlobs.get(1).getState());
    List<SerializedDelta> titiGlobs = result.get("Titi");
    assertEquals(2, titiGlobs.size());
    assertEquals(SerializedDeltaState.DELETED, titiGlobs.get(0).getState());
    assertEquals(SerializedDeltaState.UPDATED, titiGlobs.get(1).getState());
  }

  private SerializedDelta createDelta(int id, SerializedDeltaState deltaState) {
    SerializedDelta deltaGlob = new SerializedDelta(id);
    deltaGlob.setState(deltaState);
    deltaGlob.setVersion(1);
    deltaGlob.setData("sdf".getBytes());
    return deltaGlob;
  }
}
