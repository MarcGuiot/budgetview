package org.globsframework.utils;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class StringsTest extends TestCase {

  public void testIsNull() throws Exception {
    assertTrue(Strings.isNullOrEmpty(null));
    assertTrue(Strings.isNullOrEmpty(""));
    assertFalse(Strings.isNullOrEmpty("a"));
  }

  public void testCapitalize() throws Exception {
    assertEquals("TotoTiti", Strings.capitalize("totoTiti"));
    assertNull(Strings.capitalize(null));
    assertEquals("", Strings.capitalize(""));
  }

  public void testUncapitalize() throws Exception {
    assertEquals("TotoTiti", Strings.capitalize("totoTiti"));
    assertNull(Strings.uncapitalize(null));
    assertEquals("", Strings.uncapitalize(""));
  }

  public void testToLower() throws Exception {
    assertEquals("", Strings.toNiceLowerCase(""));
    assertEquals("name", Strings.toNiceLowerCase("name"));
    assertEquals("aName", Strings.toNiceLowerCase("A_NAME"));
    assertEquals("helloWorld", Strings.toNiceLowerCase("HELLO_WORLD"));
  }

  public void testToUpper() throws Exception {
    assertEquals("", Strings.toNiceUpperCase(""));
    assertEquals("NAME", Strings.toNiceUpperCase("name"));
    assertEquals("A_NAME_WITH_PARTS", Strings.toNiceUpperCase("aNameWithParts"));
    assertEquals("HELLO_WORLD", Strings.toNiceUpperCase("helloWorld"));
  }

  public void testMapToString() throws Exception {
    Map map = new HashMap();
    map.put("toto", 1);
    map.put(3, "4");
    assertEquals("'3' = '4'" + Strings.LINE_SEPARATOR
                 + "'toto' = '1'" + Strings.LINE_SEPARATOR,
                 Strings.toString(map));
  }
}
