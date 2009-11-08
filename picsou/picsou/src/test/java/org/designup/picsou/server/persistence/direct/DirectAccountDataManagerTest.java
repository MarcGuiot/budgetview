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
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class DirectAccountDataManagerTest extends TestCase {
  private static final String PATH = "tmp/test_prevayler_direct";

  protected void setUp() throws Exception {
    super.setUp();
    Files.deleteSubtree(new File(PATH));
  }

  public void testDeltaOnUserData() throws Exception {
    DirectAccountDataManager directAccountDataManager = new DirectAccountDataManager(PATH, false);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 1, "A", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    SerializedByteArrayOutput actualOutput = new SerializedByteArrayOutput();
    directAccountDataManager.getUserData(actualOutput.getOutput(), userId);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(actualOutput.getInput(), data);
    assertEquals(1, data.get("A").size());
    assertEquals(1, data.get("A").get(1).getVersion());
  }

  public void testTakeSnapshot() throws Exception {
    DirectAccountDataManager directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(1);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 1, "A", ServerState.CREATED);
    createDelta(globMultiMap, 2, "A", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.close();

    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(2);
    initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    createDelta(globMultiMap, 3, "A", ServerState.UPDATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    String pathForUser = PATH + "/" + userId.toString();
    TestUtils.assertSetEquals(new String[]{"0000000000000000002.journal", "0000000000000000001.journal"},
                              new File(pathForUser).list());

    directAccountDataManager.takeSnapshot(userId);

    globMultiMap.clear();
    createDelta(globMultiMap, 2, "A", ServerState.UPDATED);
    createDelta(globMultiMap, 3, "A", ServerState.DELETED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    PrevaylerDirectory directory = new PrevaylerDirectory(pathForUser);
    File permanent = directory.snapshotFile(3, "snapshot");
    checkSnapshot(directAccountDataManager, permanent);

    TestUtils.assertSetEquals(new String[]{"0000000000000000003.snapshot", "0000000000000000002.journal",
                                           "0000000000000000001.journal"},
                              new File(pathForUser).list());
    directAccountDataManager.takeSnapshot(userId);
    permanent = directory.snapshotFile(4, "snapshot");
    checkSnapshot(directAccountDataManager, permanent);
    TestUtils.assertSetEquals(new String[]{"0000000000000000004.snapshot", "0000000000000000003.snapshot",
                                           "0000000000000000002.journal"},
                              new File(pathForUser).list());
    continueWriting(pathForUser);
  }

  public void testTakeSnapshotVersionIfNoJournal() throws Exception {
    DirectAccountDataManager directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(3);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 1, "A", ServerState.CREATED);
    createDelta(globMultiMap, 2, "A", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.close();

    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(3);
    userId = 123;
    initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    output = new SerializedByteArrayOutput();
    globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 3, "A", ServerState.CREATED);
    createDelta(globMultiMap, 4, "A", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.takeSnapshot(userId);
    directAccountDataManager.close();

    assertTrue(new File(PATH + "/" + userId + "/0000000000000000002.journal").delete());

    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(3);
    userId = 123;
    initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    output = new SerializedByteArrayOutput();
    globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 5, "A", ServerState.CREATED);
    createDelta(globMultiMap, 6, "A", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    assertTrue(new File(PATH + "/" + userId + "/0000000000000000003.journal").exists());

    directAccountDataManager.takeSnapshot(userId);
    assertTrue(new File(PATH + "/" + userId + "/0000000000000000004.snapshot").exists());
    
  }

  private void checkSnapshot(DirectAccountDataManager directAccountDataManager, File permanent) {
    assertNotNull(permanent);
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    directAccountDataManager.readSnapshot(data, permanent);
    assertEquals(2, data.size());
    assertNotNull(data.get("A", 1));
    assertNotNull(data.get("A", 2));
  }

  private void continueWriting(String pathForUser) {
    DirectAccountDataManager directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(2);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, ServerDelta> globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 3, "A", ServerState.CREATED);
    createDelta(globMultiMap, 1, "B", ServerState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    TestUtils.assertSetEquals(new String[]{"0000000000000000004.snapshot", "0000000000000000003.snapshot",
                                           "0000000000000000002.journal", "0000000000000000004.journal"},
                              new File(pathForUser).list());

    directAccountDataManager.takeSnapshot(userId);
    TestUtils.assertSetEquals(new String[]{"0000000000000000005.snapshot", "0000000000000000004.snapshot",
                                           "0000000000000000004.journal"},
                              new File(pathForUser).list());
    directAccountDataManager.getUserData(initialOutput, userId);
    output = new SerializedByteArrayOutput();
    globMultiMap = new MultiMap<String, ServerDelta>();
    createDelta(globMultiMap, 3, "A", ServerState.DELETED);
    createDelta(globMultiMap, 1, "B", ServerState.UPDATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.close();
    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(1);
    SerializedByteArrayOutput actualOutput = new SerializedByteArrayOutput();
    directAccountDataManager.getUserData(actualOutput.getOutput(), userId);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(actualOutput.getInput(), data);
    assertEquals(1, data.get("B").size());
    assertEquals(1, data.get("B").get(1).getVersion());
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
