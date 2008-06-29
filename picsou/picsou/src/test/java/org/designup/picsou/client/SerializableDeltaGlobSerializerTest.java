package org.designup.picsou.client;

import junit.framework.TestCase;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

import java.util.List;

public class SerializableDeltaGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializableDeltaGlobSerializer deltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, DeltaGlob> map = new MultiMap<String, DeltaGlob>();

    map.put("Toto", createDelta(1, "Toto", DeltaState.CREATED));
    map.put("Toto", createDelta(2, "Toto", DeltaState.CREATED));
    map.put("Titi", createDelta(1, "Titi", DeltaState.DELETED));
    map.put("Titi", createDelta(2, "Titi", DeltaState.UPDATED));
    deltaGlobSerializer.serialize(output.getOutput(), map);
    MultiMap<String, DeltaGlob> result = deltaGlobSerializer.deserialize(output.getInput());
    assertEquals(4, result.size());
    List<DeltaGlob> totoGlobs = result.get("Toto");
    assertEquals(2, totoGlobs.size());
    assertEquals(DeltaState.CREATED, totoGlobs.get(0).getState());
    assertEquals(DeltaState.CREATED, totoGlobs.get(1).getState());
    List<DeltaGlob> titiGlobs = result.get("Titi");
    assertEquals(2, titiGlobs.size());
    assertEquals(DeltaState.DELETED, titiGlobs.get(0).getState());
    assertEquals(DeltaState.UPDATED, titiGlobs.get(1).getState());
  }

  private DefaultDeltaGlob createDelta(int id, String globTypeName, DeltaState deltaState) {
    Key key1 = KeyBuilder.init(SerializableGlobType.TYPE)
      .setValue(SerializableGlobType.ID, id).setValue(SerializableGlobType.GLOB_TYPE_NAME, globTypeName).get();
    DefaultDeltaGlob deltaGlob = new DefaultDeltaGlob(key1);
    deltaGlob.setState(deltaState);
    deltaGlob.set(SerializableGlobType.VERSION, 1);
    deltaGlob.set(SerializableGlobType.DATA, "sdf".getBytes());
    return deltaGlob;
  }
}
