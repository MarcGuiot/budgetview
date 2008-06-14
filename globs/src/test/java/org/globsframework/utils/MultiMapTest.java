package org.globsframework.utils;

import junit.framework.TestCase;

public class MultiMapTest extends TestCase {
  private MultiMap<String, Integer> map = new MultiMap<String, Integer>();

  public void testStandardUsage() throws Exception {
    map.put("a", 1);
    map.put("a", 2);
    map.put("b", 3);
    assertEquals(3, map.size());
    TestUtils.assertEquals(map.get("a"), 1, 2);
    TestUtils.assertEquals(map.get("b"), 3);
  }

  public void testReturnedListsCannotBeModified() throws Exception {
    map.put("a", 1);
    try {
      map.get("a").add(2);
      fail();
    }
    catch (UnsupportedOperationException e) {
    }
  }
}
