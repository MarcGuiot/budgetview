package org.designup.picsou.server.persistence.direct;

import junit.framework.TestCase;
import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.utils.Files;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class DirectAccountDataManagerTest extends TestCase {
  protected void setUp() throws Exception {
    super.setUp();
    Files.deleteSubtree(new File("tmp/test_prevayler"));
  }

  public void test() throws Exception {
    DirectAccountDataManager directAccountDataManager = new DirectAccountDataManager("tmp/test_prevayler", false);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, DeltaGlob> globMultiMap = new MultiMap<String, DeltaGlob>();
    createDelta(globMultiMap, 1, "A", DeltaState.CREATED);
//    serializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    SerializedByteArrayOutput actualOutput = new SerializedByteArrayOutput();
    directAccountDataManager.getUserData(actualOutput.getOutput(), userId);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    MapOfMaps<String, Integer, Glob> map = serializableGlobSerializer.deserialize(actualOutput.getInput());
    assertEquals(1, map.get("A").size());
    assertEquals(1, map.get("A").get(1).get(SerializableGlobType.VERSION).intValue());

  }

  private DefaultDeltaGlob createDelta(MultiMap<String, DeltaGlob> globMultiMap, int id, String globTypeName, DeltaState deltaState) {
    Key key1 = KeyBuilder.init(SerializableGlobType.TYPE)
      .setValue(SerializableGlobType.ID, id).setValue(SerializableGlobType.GLOB_TYPE_NAME, globTypeName).get();
    DefaultDeltaGlob deltaGlob = new DefaultDeltaGlob(key1);
    deltaGlob.setState(deltaState);
//    deltaGlob.set(SerializableGlobType.VERSION, 1);
//    deltaGlob.set(SerializableGlobType.DATA, "sdf".getBytes());
    globMultiMap.put(globTypeName, deltaGlob);
    return deltaGlob;
  }

}
