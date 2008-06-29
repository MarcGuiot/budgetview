package org.designup.picsou.client;

import junit.framework.TestCase;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobTestUtils;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

public class SerializableGlobSerializerTest extends TestCase {

  public void test() throws Exception {
    SerializableGlobSerializer serializer = new SerializableGlobSerializer();
    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    serializer.serialize(byteArrayOutput.getOutput(), init());
    MapOfMaps<String, Integer, Glob> actual = serializer.deserialize(byteArrayOutput.getInput());
    assertEquals(1, actual.get("A").size());
    assertEquals(1, actual.get("B").size());
    assertEquals(2, actual.get("A").get(1).get(SerializableGlobType.VERSION).intValue());
    assertEquals(1, actual.get("B").get(2).get(SerializableGlobType.VERSION).intValue());
  }

  private MapOfMaps<String, Integer, Glob> init() {
    DefaultGlobRepository repository = new DefaultGlobRepository();
    GlobTestUtils.parse(ServerModel.get(), repository,
                        "<serializableGlobType id='1' globTypeName='A' version='2'/>" +
                        "<serializableGlobType id='2' globTypeName='B' version='1'/>" +
                        "");
    MapOfMaps<String, Integer, Glob> globMapOfMaps = new MapOfMaps<String, Integer, Glob>();
    GlobList list = repository.getAll(SerializableGlobType.TYPE);
    for (Glob glob : list) {
      globMapOfMaps.put(glob.get(SerializableGlobType.GLOB_TYPE_NAME),
                        glob.get(SerializableGlobType.ID), glob);
    }
    return globMapOfMaps;
  }
}
