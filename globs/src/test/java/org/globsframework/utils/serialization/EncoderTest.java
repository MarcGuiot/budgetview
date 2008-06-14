package org.globsframework.utils.serialization;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyModel;
import static org.globsframework.metamodel.DummyObject.*;
import org.globsframework.model.Glob;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;

import java.util.Arrays;
import java.util.Date;

public class EncoderTest extends TestCase {

  public void testEncodesPrimitiveTypes() throws Exception {
    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteArrayOutput.getOutput();
    output.write(1);
    output.writeInteger(null);
    output.write(1L);
    output.writeLong(null);
    output.writeBytes(new byte[]{2, 2, 3, 4});
    output.writeBytes(null);
    output.writeDouble(1.3);
    output.writeDouble(null);
    output.writeDate(new Date(123));
    output.writeDate(null);
    output.writeByte(-12);
    output.writeByte(-1);

    SerializedInput input = byteArrayOutput.getInput();
    assertEquals(1, input.readNotNullInt());
    assertNull(input.readInteger());
    assertEquals(1L, input.readNotNullLong());
    assertNull(input.readLong());
    assertTrue(Arrays.equals(new byte[]{2, 2, 3, 4}, input.readBytes()));
    assertNull(input.readBytes());
    assertEquals(1.3, input.readDouble());
    assertNull(input.readDouble());
    assertEquals(new Date(123), input.readDate());
    assertNull(input.readDate());
    assertEquals(-12, input.readByte());
    assertEquals(-1, input.readByte());
  }

  public void testEncodesGlobs() throws Exception {
    Date date = Dates.parse("2006/11/26");
    Date timestamp = Dates.parse("2000/12/25");
    int id = 1;
    int linkId = 33;
    String name = "aName";
    byte[] blob = TestUtils.SAMPLE_BYTE_ARRAY;
    boolean present = true;
    double value = 0.3;

    Glob glob = GlobBuilder
      .init(TYPE)
      .set(ID, id)
      .set(DATE, date)
      .set(LINK, linkId)
      .set(NAME, name)
      .set(PASSWORD, blob)
      .set(PRESENT, present)
      .set(TIMESTAMP, timestamp)
      .set(VALUE, value).get();

    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    output.getOutput().writeGlob(glob);
    SerializedInput input = output.getInput();
    Glob decodedGlob = input.readGlob(DummyModel.get());

    assertEquals(1, decodedGlob.get(ID).intValue());
    assertEquals(date, decodedGlob.get(DATE));
    assertEquals(linkId, decodedGlob.get(LINK).intValue());
    assertEquals(name, decodedGlob.get(NAME));
    assertTrue(Arrays.equals(blob, decodedGlob.get(PASSWORD)));
    assertEquals(present, decodedGlob.get(PRESENT).booleanValue());
    assertEquals(timestamp, decodedGlob.get(TIMESTAMP));
    assertEquals(value, decodedGlob.get(VALUE));
  }

  public void testEncodeCreateDeltaGlob() throws Exception {
    DefaultDeltaGlob createGlob = new DefaultDeltaGlob(newKey(TYPE, 1));
    createGlob.setState(DeltaState.CREATED);
    createGlob.set(NAME, "titi");
    createGlob.set(VALUE, 3.14);

    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    output.getOutput().writeDeltaGlob(createGlob);
    SerializedInput input = output.getInput();
    DeltaGlob decodedGlob = input.readDeltaGlob(DummyModel.get());
    assertEquals(DeltaState.CREATED, decodedGlob.getState());
    assertEquals(3, decodedGlob.size());
    assertEquals(1, decodedGlob.get(ID).intValue());
    assertEquals("titi", decodedGlob.get(NAME));
    assertEquals(3.14, decodedGlob.get(VALUE));
  }

  public void testEncodeUpdate() throws Exception {
    DefaultDeltaGlob createGlob = new DefaultDeltaGlob(newKey(TYPE, 1));
    createGlob.setState(DeltaState.UPDATED);
    Date date = Dates.parse("2006/10/10");
    createGlob.set(DATE, date);
    createGlob.set(LINK, 3);
    createGlob.set(PRESENT, Boolean.TRUE);

    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    output.getOutput().writeDeltaGlob(createGlob);
    SerializedInput input = output.getInput();
    DeltaGlob decodedGlob = input.readDeltaGlob(DummyModel.get());
    assertEquals(DeltaState.UPDATED, decodedGlob.getState());
    assertEquals(4, decodedGlob.size());
    assertEquals(1, decodedGlob.get(ID).intValue());
    assertEquals(date, decodedGlob.get(DATE));
    assertEquals(Boolean.TRUE, decodedGlob.get(PRESENT));
  }

  public void testEncodeDelete() throws Exception {
    DefaultDeltaGlob createGlob = new DefaultDeltaGlob(newKey(TYPE, 1));
    createGlob.setState(DeltaState.DELETED);
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    output.getOutput().writeDeltaGlob(createGlob);
    SerializedInput input = output.getInput();
    DeltaGlob decodedGlob = input.readDeltaGlob(DummyModel.get());
    assertEquals(DeltaState.DELETED, decodedGlob.getState());
    assertEquals(newKey(TYPE, 1), decodedGlob.getKey());
  }
}
