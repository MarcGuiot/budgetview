package org.globsframework.utils.serialization;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyModel;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.*;
import org.globsframework.model.delta.*;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.ArrayTestUtils;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;

import java.io.*;
import java.util.Date;

public class SerializationTest extends TestCase {
  protected SerializedOutput output;
  protected OutputStream outputStream;
  protected InputStream inputStream;
  protected SerializedInput input;
  private Date currentDate;

  protected void setUp() throws Exception {
    String fileName = TestUtils.getFileName(this, "sample.dat");
//    String fileName = "/tmp/output.bin";
    new File(fileName).getParentFile().mkdirs();

    outputStream = new BufferedOutputStream(new FileOutputStream(fileName));
    output = new SerializedOutputChecker(new DefaultSerializationOutput(outputStream));

//    inputStream = new FileInputStream(fileName);
//    inputStream = new BufferedInputStream(new FileInputStream(fileName));
    inputStream = new YANBuffereInputStream(new FileInputStream(fileName));
    input = new SerializationInputChecker(new DefaultSerializationInput(inputStream));

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

    output.writeJavaString("blah");
    output.writeBoolean(Boolean.TRUE);
    output.writeDouble(6.33);
    output.writeDate(currentDate);
    output.writeInteger(4);
    output.writeLong(666L);
    outputStream.close();

    assertEquals("blah", input.readJavaString());
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
    output.writeJavaString("end");
    outputStream.close();

    Glob newGlob = input.readGlob(DummyModel.get());
    assertNotSame(glob, newGlob);

    assertEquals(glob.getValues(), newGlob.getValues());
    assertEquals(glob.getKey(), newGlob.getKey());
    assertEquals("end", input.readJavaString());
    inputStream.close();
  }

  public void testChangeSet() throws Exception {
    MutableChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(KeyBuilder.newKey(DummyObject.TYPE, 1),
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 1)
                                .set(DummyObject.NAME, "name1")
                                .set(DummyObject.DATE, currentDate)
                                .get());
    changeSet.processUpdate(KeyBuilder.newKey(DummyObject.TYPE, 2), DummyObject.NAME, "name2", null);
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
                                     "<update type='dummyObject' id='2' name='name2' _name='(null)'/>" +
                                     "<delete type='dummyObject' id='3' _name='name3' _value='3.14'/>");
  }

//  public void testBigWrite() throws Exception {
//    for (int i = 0; i < 550000; i++){
//      output.writeJavaString("blah");
//      output.writeBoolean(Boolean.TRUE);
//      output.writeDouble(6.33);
//      output.writeDate(currentDate);
//      output.writeInteger(4);
//      output.writeLong(666L);
//      output.write(new long[100]);
//    }
//    outputStream.close();
//  }
//
//
//  public void testBigRead() throws Exception {
//    for (int i = 0; i < 250000; i++){
//      input.readJavaString();
//      input.readBoolean();
//      input.readDouble();
//      input.readDate();
//      input.readInteger();
//      assertEquals(666L, (long)input.readLong());
//      input.readLongArray();
//    }
//  }

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

  public static final String TMP_OUTPUT_SER = "/tmp/output1G.ser";
  final static long SIZE = 1000;

  public static void main(String[] args) throws FileNotFoundException {
    File file = new File(TMP_OUTPUT_SER);
    SerializedInput serizalizedInput = SerializedInputOutputFactory.init(new BufferedInputStream(new FileInputStream(file)));
    long start = System.nanoTime();
    for (long i = 0; i < SIZE * (1024l * 1024l / 8l); i++) {
      serizalizedInput.readNotNullDouble();
    }
    long end = System.nanoTime();
    System.out.println("PerfRead.read " + file.length() / ((end - start) / 1000000000.) / 1024 / 1024);

  }
}
