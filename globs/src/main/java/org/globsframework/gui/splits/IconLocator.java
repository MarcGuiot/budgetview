package org.globsframework.gui.splits;


import org.globsframework.gui.splits.exceptions.IconNotFound;

import javax.swing.*;

public interface IconLocator {
  Icon get(String name) throws IconNotFound;

  IconLocator NULL = new IconLocator() {
    public Icon get(String name) {
      throw new IconNotFound("No IconLocator available - cannot find icon: " + name);
    }
  };
}
