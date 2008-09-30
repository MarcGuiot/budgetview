package org.globsframework.gui.splits.utils;

import junit.framework.TestCase;
import org.apache.commons.collections.list.CursorableLinkedList;

import java.awt.*;

public class CursorsTest extends TestCase {
  public void test() throws Exception {
    assertEquals(Cursor.HAND_CURSOR, Cursors.parse("hand").getType());
    assertEquals(Cursor.WAIT_CURSOR, Cursors.parse("wait").getType());

    assertEquals(Cursor.WAIT_CURSOR, Cursors.parse(Integer.toString(Cursor.WAIT_CURSOR)).getType());

    try {
      Cursors.parse("unknown");
      fail();
    }
    catch (Exception e) {
      assertEquals("'unknown' is not a valid cursor", e.getMessage());
    }
  }
}
