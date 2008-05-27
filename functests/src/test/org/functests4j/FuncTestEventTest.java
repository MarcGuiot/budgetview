package org.functests4j;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class FuncTestEventTest extends TestCase {

  public void testSetAttributeShouldBeRetraivable() throws Exception {
    DefaultFuncTestEvent event = new DefaultFuncTestEvent("name");
    event.setAttributes("att1", "val1");
    event.setAttributes("att2", "val2");
    final Map actual = new HashMap();
    event.visitAttribute(new DefaultFuncTestEvent.AttributeValue() {
      public void process(String attributeName, Object value) {
        actual.put(attributeName, value);
      }
    });
    assertEquals(2, actual.size());
    assertEquals("val1", actual.get("att1"));
    assertEquals("val2", actual.get("att2"));
  }
}
