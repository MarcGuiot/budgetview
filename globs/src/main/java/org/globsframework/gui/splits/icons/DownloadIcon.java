package org.globsframework.gui.splits.icons;

import org.globsframework.gui.splits.utils.Java2DUtils;

import java.awt.*;
import java.awt.geom.GeneralPath;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class DownloadIcon extends SingleColorIcon {

  private int width;
  private int height;

  public DownloadIcon(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    Graphics2D g2 = (Graphics2D) graphics;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//
//    g2.setColor(Color.GREEN);
//    g2.fillRect(0, 0, width, height);

    g2.setPaint(getColor());
    g2.setColor(getColor());
    g2.setStroke(new BasicStroke(2));

    g2.scale((double) width / 100.0, (double) height / 100.0);
    g2.fillRect(30, 0, 40, 42);

    g2.fillRect(0, 80, 10, 20);
    g2.fillRect(10, 90, 80, 10);
    g2.fillRect(90, 80, 10, 20);

    GeneralPath triangle = createTriangle();
    Java2DUtils.resize(triangle, 100, 40);
    triangle.transform(getTranslateInstance(x, y + 40));
    g2.fill(triangle);
  }

  private GeneralPath createTriangle() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 0);
    shape.lineTo(10, 0);
    shape.lineTo(5, 10);
    shape.closePath();
    return shape;
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }
}
