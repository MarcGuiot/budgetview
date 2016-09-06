package com.budgetview.client;

import com.budgetview.client.serialization.GlobCollectionSerializer;
import com.budgetview.session.serialization.SerializedGlob;
import junit.framework.TestCase;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

public class SerializableGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    GlobCollectionSerializer.serialize(byteArrayOutput.getOutput(), init());
    MapOfMaps<String, Integer, SerializedGlob> actual = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(byteArrayOutput.getInput(), actual);
    assertEquals(1, actual.get("A").size());
    assertEquals(1, actual.get("B").size());
    assertEquals(2, actual.get("A").get(1).getVersion());
    assertEquals(1, actual.get("B").get(2).getVersion());
  }

  private MapOfMaps<String, Integer, SerializedGlob> init() {
    MapOfMaps<String, Integer, SerializedGlob> globMapOfMaps = new MapOfMaps<String, Integer, SerializedGlob>();
    globMapOfMaps.put("A", 1, create("A", 1, 2));
    globMapOfMaps.put("B", 2, create("B", 2, 1));
    return globMapOfMaps;
  }

  private SerializedGlob create(String typeName, int id, int version) {
    return new SerializedGlob(typeName, id, version, new byte[]{0});
  }
}
