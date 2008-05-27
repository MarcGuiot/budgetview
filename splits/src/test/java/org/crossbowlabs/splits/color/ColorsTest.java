package org.crossbowlabs.splits.color;

import junit.framework.TestCase;

import java.awt.*;

public class ColorsTest extends TestCase {
  public void test() throws Exception {
    assertEquals("ff0000", Colors.toString(Color.RED));
    assertEquals(Color.BLUE, Colors.toColor("0000FF"));

    assertEquals(null, Colors.toColor(null));
    assertEquals("", Colors.toString(null));
  }
}
