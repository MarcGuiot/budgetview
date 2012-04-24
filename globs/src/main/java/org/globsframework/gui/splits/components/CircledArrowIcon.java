package org.globsframework.gui.splits.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class CircledArrowIcon implements Icon {

  private static final int SIDE_LENGTH = 14;
  private static final float ARROW_RATIO = 0.6f;
  private static final int DIAMETER = SIDE_LENGTH - 3;

  private Color color = Color.BLUE;

  private GeneralPath shape;

  public CircledArrowIcon() {
    shape = createTriangleShape();

    Rectangle initialRectangle = shape.getBounds();
    float widthRatio = ARROW_RATIO * DIAMETER / (float)initialRectangle.width;
    float heightRatio = ARROW_RATIO * DIAMETER / (float)initialRectangle.height;
    AffineTransform scaling = AffineTransform.getScaleInstance(widthRatio, heightRatio);
    shape.transform(scaling);

    Rectangle resizedRectangle = shape.getBounds();
    shape.transform(getTranslateInstance(2 + resizedRectangle.width / 2,
                                         2 + resizedRectangle.height / 2));
  }

  public int getIconWidth() {
    return SIDE_LENGTH;
  }

  public int getIconHeight() {
    return SIDE_LENGTH;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public void paintIcon(Component component, Graphics g, int i, int i1) {

    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setColor(color);
    g2d.drawOval(2, 2, DIAMETER, DIAMETER);

    g2d.fill(shape);
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