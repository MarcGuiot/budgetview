package org.globsframework.utils;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

  public void testJoin() throws Exception {
    String[] input = {"b", "c"};
    String[] result = {"a", "b", "c"};
    TestUtils.assertEquals(result, Utils.join("a", input));
  }

  public void testMinMax() throws Exception {
    assertEquals(null, Utils.min());
    assertEquals("a", Utils.min("a", "b"));
    assertEquals("a", Utils.min("a", null, "b"));

    assertEquals(null, Utils.max());
    assertEquals("b", Utils.max("a", "b"));
    assertEquals("b", Utils.max("a", null, "b"));
  }

  public void testMinInt() throws Exception {
    assertEquals(0, Utils.minInt());
    assertEquals(1, Utils.minInt(4, 1, 2, 3));
  }

  public void testCompare() throws Exception {
    assertTrue(Utils.compare("a", "b") < 0);
    assertTrue(Utils.compare("a", "a") == 0);
    assertTrue(Utils.compare("b", "a") > 0);
    assertTrue(Utils.compare(null, "a") < 0);
    assertTrue(Utils.compare("a", null) > 0);
  }

  public void testCompareIgnoreCase() throws Exception {
    assertTrue(Utils.compareIgnoreCase("a", "b") < 0);
    assertTrue(Utils.compareIgnoreCase("a", "B") < 0);
    assertTrue(Utils.compareIgnoreCase("a", "A") == 0);
    assertTrue(Utils.compareIgnoreCase("B", "a") > 0);
    assertTrue(Utils.compareIgnoreCase(null, "a") < 0);
    assertTrue(Utils.compareIgnoreCase("a", null) > 0);
  }

  public void testAppend() throws Exception {
    ArrayTestUtils.assertEquals(new int[]{1, 3, 1, 5, 6},
                                Utils.append(new int[]{1, 3}, new int[]{1, 5, 6}));
  }

  public void testRange() throws Exception {
    ArrayTestUtils.assertEquals(new Integer[]{1}, Utils.range(1, 1));
    ArrayTestUtils.assertEquals(new Integer[]{-1, 0, 1, 2}, Utils.range(-1, 2));

    try {
      Utils.range(2, 1);
    }
    catch (Exception e) {
      assertEquals("Lower bound 2 should be less than 1", e.getMessage());
    }
  }
}
