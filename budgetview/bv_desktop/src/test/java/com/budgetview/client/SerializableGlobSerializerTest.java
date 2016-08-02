package com.budgetview.client;

import com.budgetview.client.serialization.SerializableGlobSerializer;
import com.budgetview.session.serialization.SerializableGlobType;
import junit.framework.TestCase;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

public class SerializableGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    SerializableGlobSerializer.serialize(byteArrayOutput.getOutput(), init());
    MapOfMaps<String, Integer, SerializableGlobType> actual = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(byteArrayOutput.getInput(), actual);
    assertEquals(1, actual.get("A").size());
    assertEquals(1, actual.get("B").size());
    assertEquals(2, actual.get("A").get(1).getVersion());
    assertEquals(1, actual.get("B").get(2).getVersion());
  }

  private MapOfMaps<String, Integer, SerializableGlobType> init() {
    MapOfMaps<String, Integer, SerializableGlobType> globMapOfMaps = new MapOfMaps<String, Integer, SerializableGlobType>();
    globMapOfMaps.put("A", 1, create("A", 1, 2));
    globMapOfMaps.put("B", 2, create("B", 2, 1));
    return globMapOfMaps;
  }

  private SerializableGlobType create(String typeName, int id, int version) {
    SerializableGlobType data = new SerializableGlobType();
    data.setGlobTypeName(typeName);
    data.setVersion(version);
    data.setId(id);
    return data;
  }
}
