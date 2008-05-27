package org.crossbowlabs.globs.utils.serialization;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyModel;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.*;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.ArrayTestUtils;
import org.crossbowlabs.globs.utils.TestUtils;
import org.crossbowlabs.globs.utils.Dates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class SerializationTest extends TestCase {
  protected SerializedOutput output;
  protected FileOutputStream outputStream;
  protected FileInputStream inputStream;
  protected SerializedInput input;
  private Date currentDate;

  protected void setUp() throws Exception {
    String fileName = TestUtils.getFileName(this, "sample.dat");

    new File(fileName).getParentFile().mkdirs();

    outputStream = new FileOutputStream(fileName);
    output = new DefaultSerializationOutput(outputStream);

    inputStream = new FileInputStream(fileName);
    input = new DefaultSerializationInput(inputStream);

    currentDate = new Date();
  }

  public void testSimpleValues() throws Exception {
    output.write(3);
    output.write(33L);
    output.write(6.33);
    output.write(false);
    output.write(new int[]{3, 5, 9});
    outputStream.close();

    assertEquals(3, input.readNotNullInt());
    assertEquals(33L, input.readNotNullLong());
    assertEquals(6.33, input.readNotNullDouble());
    assertEquals(false, input.readBoolean().booleanValue());
    ArrayTestUtils.assertEquals(new int[]{3, 5, 9}, input.readIntArray());
    inputStream.close();
  }

  public void testObjects() throws Exception {

    output.writeString("blah");
    output.writeBoolean(Boolean.TRUE);
    output.writeDouble(6.33);
    output.writeDate(currentDate);
    output.writeInteger(4);
    output.writeLong(666L);
    outputStream.close();

    assertEquals("blah", input.readString());
    assertEquals(Boolean.TRUE, input.readBoolean());
    assertEquals(6.33, input.readDouble().doubleValue());
    assertEquals(currentDate, input.readDate());
    assertEquals(4, input.readInteger().intValue());
    assertEquals(666L, input.readLong().longValue());
    inputStream.close();
  }

  public void testGlob() throws Exception {
    Glob glob = GlobBuilder.init(DummyObject.TYPE, createSampleValues()).get();

    output.writeGlob(glob);
    output.writeString("end");
    outputStream.close();

    Glob newGlob = input.readGlob(DummyModel.get());
    assertNotSame(glob, newGlob);

    assertEquals(glob.getValues(true), newGlob.getValues(true));
    assertEquals("end", input.readString());
    inputStream.close();
  }

  public void testDeltaGlobWithCreatedState() throws Exception {
    checkDeltaGlobWithValues(DeltaState.CREATED);
  }

  public void testDeltaGlobWithUpdateState() throws Exception {
    checkDeltaGlobWithValues(DeltaState.UPDATED);
  }

  public void testDeltaGlobWithDeletedState() throws Exception {
    checkDeletedDeltaGlobWithoutValues(DeltaState.DELETED);
  }

  public void testDeltaGlobWithUnchangedState() throws Exception {
    checkDeletedDeltaGlobWithoutValues(DeltaState.DELETED);
  }

  public void testChangeSet() throws Exception {
    MutableChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(KeyBuilder.newKey(DummyObject.TYPE, 1),
                              FieldValuesBuilder.init()
                                      .set(DummyObject.ID, 1)
                                      .set(DummyObject.NAME, "name1")
                                      .set(DummyObject.DATE, currentDate)
                                      .get());
    changeSet.processUpdate(KeyBuilder.newKey(DummyObject.TYPE, 2), DummyObject.NAME, "name2");
    changeSet.processDeletion(KeyBuilder.newKey(DummyObject.TYPE, 3),
                              FieldValuesBuilder.init()
                                      .set(DummyObject.ID, 3)
                                      .set(DummyObject.NAME, "name3")
                                      .set(DummyObject.VALUE, 3.14156)
                                      .get());
    output.writeChangeSet(changeSet);
    outputStream.close();

    ChangeSet readChangeSet = input.readChangeSet(DummyModel.get());
    GlobTestUtils.assertChangesEqual(readChangeSet,
                                     "<create type='dummyObject' id='1' name='name1' date='" + Dates.getStandardDate(currentDate) + "'/>" +
                                     "<update type='dummyObject' id='2' name='name2'/>" +
                                     "<delete type='dummyObject' id='3' name='name3' value='3.14'/>"
    );
  }

  private void checkDeltaGlobWithValues(DeltaState state) throws IOException {
    DeltaGlob delta = new DefaultDeltaGlob(Key.create(DummyObject.TYPE, 1));
    delta.setState(state);
    delta.setObject(DummyObject.NAME, "obj");

    output.writeDeltaGlob(delta);
    output.writeString("end");
    outputStream.close();

    DeltaGlob newDelta = input.readDeltaGlob(DummyModel.get());
    assertNotSame(delta, newDelta);

    assertEquals(state, newDelta.getState());
    assertEquals(1, newDelta.get(DummyObject.ID).intValue());
    assertEquals("obj", newDelta.get(DummyObject.NAME));

    assertEquals("end", input.readString());
    inputStream.close();
  }

  private void checkDeletedDeltaGlobWithoutValues(DeltaState state) throws IOException {
    DeltaGlob delta = new DefaultDeltaGlob(Key.create(DummyObject.TYPE, 1));
    delta.setState(state);
    delta.setObject(DummyObject.NAME, "obj");

    output.writeDeltaGlob(delta);
    output.writeString("end");
    outputStream.close();

    DeltaGlob newDelta = input.readDeltaGlob(DummyModel.get());
    assertNotSame(delta, newDelta);

    assertEquals(state, newDelta.getState());
    assertEquals(1, newDelta.get(DummyObject.ID).intValue());
    assertTrue(newDelta.isSet(DummyObject.NAME));

    assertEquals("end", input.readString());
    inputStream.close();
  }

  private FieldValues createSampleValues() {
    return FieldValuesBuilder.init()
            .set(DummyObject.ID, 1)
            .set(DummyObject.NAME, "obj1")
            .set(DummyObject.DATE, new Date())
            .set(DummyObject.LINK, 7)
            .set(DummyObject.PRESENT, false)
            .set(DummyObject.TIMESTAMP, new Date())
            .set(DummyObject.VALUE, 6.2)
            .get();
  }
}
