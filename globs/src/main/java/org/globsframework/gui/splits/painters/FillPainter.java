package org.globsframework.gui.splits.painters;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;

import java.awt.*;

public class FillPainter implements Painter, ColorChangeListener {
  private Object colorKey;
  private ColorService colorService;
  private Color fillColor;

  public FillPainter(Object colorKey, ColorService colorService) {
    this.colorKey = colorKey;
    this.colorService = colorService;
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    fillColor = colorLocator.get(colorKey);
  }

  public void paint(Graphics g, int width, int height) {
    g.setColor(fillColor);
    g.fillRect(0, 0, width, height);
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
