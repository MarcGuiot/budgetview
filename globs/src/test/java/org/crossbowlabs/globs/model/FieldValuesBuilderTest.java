package org.crossbowlabs.globs.model;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

public class FieldValuesBuilderTest extends TestCase {
  public void testValuesMustComplyWithTheFieldType() throws Exception {
    try {
      FieldValuesBuilder.init().setObject(DummyObject.PRESENT, "a");
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Value 'a' (java.lang.String) is not authorized for field: " +
                   DummyObject.PRESENT.getName() + " (expected java.lang.Boolean)", e.getMessage());
    }
  }
}
