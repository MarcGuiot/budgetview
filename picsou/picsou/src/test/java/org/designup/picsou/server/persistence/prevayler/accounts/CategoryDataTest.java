package org.designup.picsou.server.persistence.prevayler.accounts;

import junit.framework.TestCase;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

import java.util.Arrays;
import java.util.List;

public class CategoryDataTest extends TestCase {

  public void testReadWrite() throws Exception {
    CategoryData expected = new CategoryData();
    expected.addCategory("some info", 3);
    expected.addCategory("some info", 5);
    expected.addCategory("some other info", 4);
    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    CategoryData.write(byteArrayOutput.getOutput(), expected);
    CategoryData actual = CategoryData.read(byteArrayOutput.getInput());
    List<Persistence.CategoryInfo> categoryInfos =
      actual.getAssociatedCategory(Arrays.asList("some info", "some other info"));
    assertEquals(2, categoryInfos.size());
    int[] someInfoCategories = categoryInfos.get(0).getCategories();
    assertEquals(2, someInfoCategories.length);
    assertEquals(3, someInfoCategories[0]);
    assertEquals(5, someInfoCategories[1]);
    assertEquals(4, categoryInfos.get(1).getCategories()[0]);
  }

}
