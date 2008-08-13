package org.globsframework.model;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.utils.exceptions.InvalidParameter;

public class FieldValuesBuilderTest extends TestCase {
  public void testValuesMustComplyWithTheFieldType() throws Exception {
    try {
      FieldValuesBuilder.init().setValue(DummyObject.PRESENT, "a");
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Value 'a' (java.lang.String) is not authorized for field: " +
                   DummyObject.PRESENT.getName() + " (expected java.lang.Boolean)", e.getMessage());
    }
  }
}
