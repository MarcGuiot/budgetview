package org.designup.picsou.server.persistence.prevayler.users;

import junit.framework.TestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.File;

public abstract class PrevaylerTestCase extends TestCase {
  protected String TMP_PREVAYLER;
  protected File file;

  protected void setUp() throws Exception {
    super.setUp();
    TMP_PREVAYLER = TestUtils.getFileName(this, "prevayler");
    file = new File(TMP_PREVAYLER);
    Files.deleteWithSubtree(file);
    file.mkdirs();
  }

  protected void tearDown() throws Exception {
    Files.deleteWithSubtree(file);
  }

  public static void dumpToStd(byte[] bytes) {
//     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//     HiddenTransaction.write(new SerializedOutput(outputStream), expected);
//     dumpToStd(outputStream);

    StringBuilder builder = new StringBuilder();
    builder.append("byte[] value = new byte[]{");
    for (int i = 0; i < bytes.length; i++) {
      if (i != 0) {
        builder.append(" ,");
      }
      if ((i % 15) == 0) {
        builder.append("\n");
      }
      builder.append((int)bytes[i]);
    }
    builder.append("};");
    System.out.println(builder.toString());
  }
}
