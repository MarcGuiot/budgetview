package org.designup.picsou.server.model;

import junit.framework.TestCase;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class HiddenTransactionTest extends TestCase {

  public void testReadWriteV1() throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Glob expected = getHiddenTransaction();
    HiddenTransaction.write(SerializedInputOutputFactory.init(output), expected, HiddenTransaction.V1);
    Glob actual = HiddenTransaction.read(SerializedInputOutputFactory.init(output.toByteArray()));
    assertTrue(Arrays.equals(expected.toArray(), actual.toArray()));
  }

  public void testReadV1() throws Exception {
    Glob expected = getHiddenTransaction();
//    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
//    HiddenTransaction.write(byteArrayOutput.getOuput(), expected, HiddenTransaction.V1);
//    PrevaylerTestCase.dumpToStd(byteArrayOutput.toByteArray());
    byte[] value = new byte[]{
      1, 0, 3, 0, 0, 0, 12, 0, 0, 0, 104, 105, 100, 100, 101,
      110, 32, 108, 97, 98, 101, 108, 0, 3, 0, 0, 0, 4, 0, 0,
      0, 1, 2, 3, 4};
    Glob actual = HiddenTransaction.read(SerializedInputOutputFactory.init(value));
    assertTrue(Arrays.equals(expected.toArray(), actual.toArray()));
  }

  public void testReadV2() throws Exception {
    GlobBuilder builder = GlobBuilder.init(HiddenTransaction.TYPE);
    builder.set(HiddenTransaction.ENCRYPTED_INFO, new byte[]{1, 2, 3, 4});
    builder.set(HiddenTransaction.ID, 3);
    builder.set(HiddenTransaction.HIDDEN_USER_ID, -1);
    Glob expected = builder.get();
//    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
//    HiddenTransaction.write(byteArrayOutput.getOutput(), expected, HiddenTransaction.V2);
//    PrevaylerTestCase.dumpToStd(byteArrayOutput.toByteArray());
    byte[] value = new byte[]{
      2, 0, 3, 0, 0, 0, 4, 0, 0, 0, 1, 2, 3, 4};
    Glob actual = HiddenTransaction.read(SerializedInputOutputFactory.init(value));
    assertTrue((Arrays.equals(expected.toArray(), actual.toArray())));
  }

  private Glob getHiddenTransaction() {
    GlobBuilder builder = GlobBuilder.init(HiddenTransaction.TYPE);
    builder.set(HiddenTransaction.ENCRYPTED_INFO, new byte[]{1, 2, 3, 4});
    builder.set(HiddenTransaction.ID, 3);
    builder.set(HiddenTransaction.HIDDEN_USER_ID, -1);
    builder.set(HiddenTransaction.HIDDEN_LABEL, "hidden label");
    builder.set(HiddenTransaction.TRANSACTION_TYPE_ID, 3);
    return builder.get();
  }
}
