package org.globsframework.gui.splits.icons;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class ArrowButtonIcon extends SingleColorIcon {

  private static final float ARROW_RATIO = 0.5f;
  public static final float STROKE = 1.0f;

  private int width;
  private int height;
  private GeneralPath triangle;

  public ArrowButtonIcon(int width, int height) {
    this.width = width;
    this.height = height;
    this.triangle = createTriangleShape();

    Rectangle triangleRect = triangle.getBounds();
    double widthRatio = ARROW_RATIO * width / (double) triangleRect.width;
    double heightRatio = ARROW_RATIO * height / (double) triangleRect.height;
    AffineTransform scaling = AffineTransform.getScaleInstance(widthRatio, heightRatio);
    triangle.transform(scaling);
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public void paintIcon(Component component, Graphics g, int x, int y) {

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setColor(getColor());
    g2d.setStroke(new BasicStroke(STROKE));
    g2d.drawRoundRect(x, y, width - 2, height - 2, 6, 6);

    Rectangle resizedRect = triangle.getBounds();
    int dx = x + width / 2 - resizedRect.width / 2 - 1;
    int dy = y + height / 2 - resizedRect.height / 2 - 1;
    g2d.translate(dx, dy);
    g2d.fill(triangle);

    g2d.translate(-dx, -dy);
  }

  private GeneralPath createTriangleShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 0);
    shape.lineTo(10, 0);
    shape.lineTo(5, 7);
    shape.closePath();
    return shape;
  }
}