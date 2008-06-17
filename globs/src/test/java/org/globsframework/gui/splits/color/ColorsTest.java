package org.globsframework.gui.splits.color;

import org.uispec4j.UISpecTestCase;

import java.awt.*;

public class ColorsTest extends UISpecTestCase {
  public void test() throws Exception {
    assertEquals("ff0000", Colors.toString(Color.RED));
    assertEquals(Color.BLUE, Colors.toColor("0000FF"));

    assertEquals(null, Colors.toColor(null));
    assertEquals("", Colors.toString(null));
  }
}
