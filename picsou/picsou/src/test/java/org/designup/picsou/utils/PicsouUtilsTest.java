package org.designup.picsou.utils;

import junit.framework.TestCase;

public class PicsouUtilsTest extends TestCase {
  public void test() throws Exception {
    assertEquals("1000-2000-3000-4000", PicsouUtils.splitCardNumber("1000200030004000"));
    assertEquals("1000-2000-3000", PicsouUtils.splitCardNumber("100020003000"));
    assertEquals("123", PicsouUtils.splitCardNumber("123"));
    assertEquals("12345", PicsouUtils.splitCardNumber("12345"));
    assertEquals(null, PicsouUtils.splitCardNumber(null));
  }
}
