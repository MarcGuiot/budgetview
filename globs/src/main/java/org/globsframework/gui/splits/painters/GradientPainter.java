package org.globsframework.gui.splits.painters;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;

import java.awt.*;

public class GradientPainter implements Painter, ColorChangeListener {
  private Object topColorKey;
  private Object bottomColorKey;
  private Object borderColorKey;
  private ColorService colorService;
  private Color topColor;
  private Color bottomColor;
  private Color borderColor;

  public GradientPainter(Object topColorKey, Object bottomColorKey, Object borderColorKey,
                         ColorService colorService) {
    this.topColorKey = topColorKey;
    this.bottomColorKey = bottomColorKey;
    this.borderColorKey = borderColorKey;
    this.colorService = colorService;
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    topColor = colorLocator.get(topColorKey);
    bottomColor = colorLocator.get(bottomColorKey);
    borderColor = colorLocator.get(borderColorKey);
  }

  public void paint(Graphics g, int width, int height) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setPaint(new GradientPaint(0, 0, topColor, 0, height, bottomColor));
    g2.fillRect(0, 0, width, height);
    g2.setColor(borderColor);
    g2.drawLine(0, 0, width, 0);
    g2.drawLine(0, height - 1, width, height - 1);
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
