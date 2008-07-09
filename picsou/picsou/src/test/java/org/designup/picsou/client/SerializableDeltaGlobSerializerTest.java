package org.designup.picsou.client;

import junit.framework.TestCase;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

import java.util.List;

public class SerializableDeltaGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializableDeltaGlobSerializer deltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> map = new MultiMap<String, ServerDelta>();

    map.put("Toto", createDelta(1, ServerState.CREATED));
    map.put("Toto", createDelta(2, ServerState.CREATED));
    map.put("Titi", createDelta(1, ServerState.DELETED));
    map.put("Titi", createDelta(2, ServerState.UPDATED));
    deltaGlobSerializer.serialize(output.getOutput(), map);
    MultiMap<String, ServerDelta> result = deltaGlobSerializer.deserialize(output.getInput());
    assertEquals(4, result.size());
    List<ServerDelta> totoGlobs = result.get("Toto");
    assertEquals(2, totoGlobs.size());
    assertEquals(ServerState.CREATED, totoGlobs.get(0).getState());
    assertEquals(ServerState.CREATED, totoGlobs.get(1).getState());
    List<ServerDelta> titiGlobs = result.get("Titi");
    assertEquals(2, titiGlobs.size());
    assertEquals(ServerState.DELETED, titiGlobs.get(0).getState());
    assertEquals(ServerState.UPDATED, titiGlobs.get(1).getState());
  }

  private ServerDelta createDelta(int id, ServerState deltaState) {
    ServerDelta deltaGlob = new ServerDelta(id);
    deltaGlob.setState(deltaState);
    deltaGlob.setVersion(1);
    deltaGlob.setData("sdf".getBytes());
    return deltaGlob;
  }
}
