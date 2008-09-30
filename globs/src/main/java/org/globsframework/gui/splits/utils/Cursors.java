package org.globsframework.gui.splits.utils;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public class Cursors {
  public static Cursor parse(String text) {
    if ("hand".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
    else if ("crosshair".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }
    else if ("default".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    else if ("move".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    else if ("text".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    }
    else if ("wait".equals(text)) {
      return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    }

    try {
      int type = Integer.parseInt(text);
      return Cursor.getPredefinedCursor(type);
    }
    catch (NumberFormatException e) {
      // fall through
    }

    throw new InvalidParameter("'" + text + "' is not a valid cursor");
  }
}
