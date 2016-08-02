package com.budgetview.persistence.direct;

import com.budgetview.client.serialization.SerializableGlobSerializer;
import com.budgetview.session.serialization.SerializableGlobType;
import junit.framework.TestCase;
import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializedDeltaState;
import org.globsframework.utils.Files;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class DirectAccountDataManagerTest extends TestCase {
  private static final String PATH = "tmp/test_prevayler_direct";
  private DirectAccountDataManager directAccountDataManager;

  protected void setUp() throws Exception {
    super.setUp();
    Files.deleteWithSubtree(new File(PATH));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    directAccountDataManager.close();
  }

  public void testDeltaOnUserData() throws Exception {
    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, SerializedDelta> globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 1, "A", SerializedDeltaState.CREATED);
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
    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(1);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, SerializedDelta> globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 1, "A", SerializedDeltaState.CREATED);
    createDelta(globMultiMap, 2, "A", SerializedDeltaState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.close();

    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(2);
    initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    createDelta(globMultiMap, 3, "A", SerializedDeltaState.UPDATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    String pathForUser = PATH + "/" + userId.toString();
    TestUtils.assertSetEquals(new String[]{"0000000000000000002.journal", "0000000000000000001.journal"},
                              new File(pathForUser).list());

    directAccountDataManager.takeSnapshot(userId);

    globMultiMap.clear();
    createDelta(globMultiMap, 2, "A", SerializedDeltaState.UPDATED);
    createDelta(globMultiMap, 3, "A", SerializedDeltaState.DELETED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);

    PrevaylerDirectory directory = new PrevaylerDirectory(pathForUser);
    File permanent = directory.snapshotFile(3, "snapshot");
    checkSnapshot(directAccountDataManager, permanent);

    TestUtils.assertSetEquals(new String[]{"0000000000000000003.snapshot", "0000000000000000002.journal",
                                           "0000000000000000001.journal", "0000000000000000003.journal"},
                              new File(pathForUser).list());
    directAccountDataManager.takeSnapshot(userId);
    permanent = directory.snapshotFile(4, "snapshot");
    checkSnapshot(directAccountDataManager, permanent);
    TestUtils.assertSetEquals(new String[]{"0000000000000000004.snapshot", "0000000000000000003.snapshot",
                                           "0000000000000000003.journal"},
                              new File(pathForUser).list());
    directAccountDataManager.close();
    continueWriting(pathForUser);
  }

  public void testTakeSnapshotVersionIfNoJournal() throws Exception {
    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(3);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, SerializedDelta> globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 1, "A", SerializedDeltaState.CREATED);
    createDelta(globMultiMap, 2, "A", SerializedDeltaState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    directAccountDataManager.close();

    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(3);
    userId = 123;
    initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    output = new SerializedByteArrayOutput();
    globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 3, "A", SerializedDeltaState.CREATED);
    createDelta(globMultiMap, 4, "A", SerializedDeltaState.CREATED);
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
    globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 5, "A", SerializedDeltaState.CREATED);
    createDelta(globMultiMap, 6, "A", SerializedDeltaState.CREATED);
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
    directAccountDataManager = new DirectAccountDataManager(PATH, false);
    directAccountDataManager.setCountFileNotToDelete(2);
    Integer userId = 123;
    SerializedOutput initialOutput = SerializedInputOutputFactory.init(new ByteArrayOutputStream());
    directAccountDataManager.getUserData(initialOutput, userId);
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    MultiMap<String, SerializedDelta> globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 3, "A", SerializedDeltaState.CREATED);
    createDelta(globMultiMap, 1, "B", SerializedDeltaState.CREATED);
    SerializableDeltaGlobSerializer.serialize(output.getOutput(), globMultiMap);
    directAccountDataManager.updateUserData(output.getInput(), userId);
    TestUtils.assertSetEquals(new String[]{"0000000000000000004.snapshot", "0000000000000000003.snapshot",
                                           "0000000000000000003.journal", "0000000000000000004.journal"},
                              new File(pathForUser).list());

    directAccountDataManager.takeSnapshot(userId);
    TestUtils.assertSetEquals(new String[]{"0000000000000000005.snapshot", "0000000000000000004.snapshot",
                                           "0000000000000000004.journal"},
                              new File(pathForUser).list());
    directAccountDataManager.getUserData(initialOutput, userId);
    output = new SerializedByteArrayOutput();
    globMultiMap = new MultiMap<String, SerializedDelta>();
    createDelta(globMultiMap, 3, "A", SerializedDeltaState.DELETED);
    createDelta(globMultiMap, 1, "B", SerializedDeltaState.UPDATED);
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

  private SerializedDelta createDelta(MultiMap<String, SerializedDelta> globMultiMap, int id,
                                      String globTypeName, SerializedDeltaState deltaState) {
    SerializedDelta deltaGlob = new SerializedDelta(id);
    deltaGlob.setState(deltaState);
    deltaGlob.setVersion(1);
    deltaGlob.setData("sdf".getBytes());
    globMultiMap.put(globTypeName, deltaGlob);
    return deltaGlob;
  }

}
