package org.globsframework.gui.splits;


import org.globsframework.gui.splits.exceptions.IconNotFound;

import javax.swing.*;

public interface IconLocator {
  ImageIcon get(String name) throws IconNotFound;

  IconLocator NULL = new IconLocator() {
    public ImageIcon get(String name) {
      throw new IconNotFound("No IconLocator available - cannot find icon: " + name);
    }
  };
}
