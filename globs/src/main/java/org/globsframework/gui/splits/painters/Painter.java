package org.globsframework.gui.splits.painters;

import java.awt.*;

public interface Painter {
  void paint(Graphics g, int width, int height);

  static Painter NULL = new Painter() {
    public void paint(Graphics g, int width, int height) {
    }
  };
}
