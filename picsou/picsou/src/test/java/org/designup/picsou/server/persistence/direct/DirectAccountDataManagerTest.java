package org.designup.picsou.server.persistence.direct;

import junit.framework.TestCase;
import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
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
    DirectAccountDataManagerWithSnapshot directAccountDataManager = new DirectAccountDataManagerWithSnapshot("tmp/test_prevayler", false);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 1, "A", ServerState.CREATED);
    serializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    SerializedByteArrayOutput actualOutput = new SerializedByteArrayOutput();
    directAccountDataManager.getUserData(actualOutput.getOutput(), userId);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    MapOfMaps<String, Integer, SerializableGlobType> map = serializableGlobSerializer.deserialize(actualOutput.getInput());
    assertEquals(1, map.get("A").size());
    assertEquals(1, map.get("A").get(1).getVersion());

  }

  private ServerDelta createDelta(MultiMap<String, ServerDelta> globMultiMap, int id,
                                  String globTypeName, ServerState deltaState) {
    ServerDelta deltaGlob = new ServerDelta(id);
    deltaGlob.setState(deltaState);
    deltaGlob.setVersion(1);
    deltaGlob.setData("sdf".getBytes());
    globMultiMap.put(globTypeName, deltaGlob);
    return deltaGlob;
  }

}
