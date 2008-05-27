package org.crossbowlabs.globs.metamodel.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObject2;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public class GlobTypeUtilsTest extends TestCase {
  public void testGetType() throws Exception {
    assertSame(DummyObject.TYPE, GlobTypeUtils.getType(DummyObject.class));

    try {
      GlobTypeUtils.getType(String.class);
      fail();
    }
    catch (Exception e) {
      assertEquals("Class java.lang.String does not define a GlobType", e.getMessage());
    }
  }

  public void testGetNamingField() throws Exception {
    assertSame(DummyObject.NAME, GlobTypeUtils.getNamingField(DummyObject.TYPE));

    try {
      GlobTypeUtils.getNamingField(DummyObject2.TYPE);
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Type 'dummyObject2' has no naming field", e.getMessage());
    }
  }

  public void testFindNamingField() throws Exception {
    assertSame(DummyObject.NAME, GlobTypeUtils.findNamingField(DummyObject.TYPE));
    assertNull(GlobTypeUtils.findNamingField(DummyObject2.TYPE));
  }
}
