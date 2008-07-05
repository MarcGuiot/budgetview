package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public interface FontLocator {
  Font get(String name) throws ItemNotFound, InvalidParameter;

  public static final FontLocator NULL = new FontLocator() {
    public Font get(String name) throws ItemNotFound, InvalidParameter {
      throw new ItemNotFound("No font locator defined");
    }
  };
}
