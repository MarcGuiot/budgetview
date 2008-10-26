package org.globsframework.gui.splits;


import org.globsframework.gui.splits.exceptions.IconNotFound;

import javax.swing.*;

public interface ImageLocator {
  ImageIcon get(String name) throws IconNotFound;

  ImageLocator NULL = new ImageLocator() {
    public ImageIcon get(String name) {
      throw new IconNotFound("No IconLocator available - cannot find icon: " + name);
    }
  };
}
