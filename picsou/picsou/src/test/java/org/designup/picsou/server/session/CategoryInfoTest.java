package org.designup.picsou.server.session;

import junit.framework.TestCase;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class CategoryInfoTest extends TestCase {

  public void testSErialization() throws Exception {
    Persistence.CategoryInfo expected = new Persistence.CategoryInfo();
    expected.categories = new int[5];
    expected.categories[0] = 5;
    expected.categories[4] = 4;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SerializedOutput serializationOutput = SerializedInputOutputFactory.init(outputStream);
    Persistence.CategoryInfo.write(serializationOutput, expected, Persistence.CategoryInfo.V1);
    Persistence.CategoryInfo actual = Persistence.CategoryInfo.read(SerializedInputOutputFactory.init(outputStream.toByteArray()));
    Arrays.equals(expected.categories, actual.categories);
  }
}