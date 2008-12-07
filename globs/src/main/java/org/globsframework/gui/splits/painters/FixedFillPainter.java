package org.globsframework.gui.splits.painters;

import java.awt.*;

public class FixedFillPainter implements Painter {
  private Color color;

  public FixedFillPainter(Color color) {
    this.color = color;
  }

  public void paint(Graphics g, int width, int height) {
    g.setColor(color);
    g.fillRect(0, 0, width, height);
  }
}
