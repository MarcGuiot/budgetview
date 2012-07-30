package org.globsframework.gui.splits.components;

import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Arrays;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class ArrowIcon implements Icon {

  public enum Orientation {
    UP(-90),
    DOWN(90),
    LEFT(180),
    RIGHT(0);

    private final int rotation;

    Orientation(int rotation) {
      this.rotation = rotation;
    }

    public static Orientation get(String text) {
      for (Orientation orientation : values()) {
        if (orientation.name().toLowerCase().equals(text.toLowerCase())) {
          return orientation;
        }
      }
      throw new InvalidParameter("Unexpected arrow orientation: can be one of " + Arrays.toString(values()));
    }
  }

  private int iconWidth;
  private int iconHeight;
  private int arrowWidth;
  private int arrowHeight;
  private Orientation orientation;
  private Color color = Color.BLACK;

  public ArrowIcon(int iconWidth, int iconHeight, Orientation orientation) {
    this.iconWidth = iconWidth;
    this.arrowWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.arrowHeight = iconHeight;
    this.orientation = orientation;
  }

  public ArrowIcon(int iconWidth, int iconHeight, int arrowWidth, int arrowHeight, Orientation orientation) {
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.arrowWidth = arrowWidth;
    this.arrowHeight = arrowHeight;
    this.orientation = orientation;
  }
  
  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {

    Graphics2D g2 = (Graphics2D)graphics;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int areaWidth = iconWidth - 1;
    int areaHeight = iconHeight - 1;

    GeneralPath shape = createTriangleShape();

    Rectangle initialRectangle = shape.getBounds();
    float widthRatio = ((float)arrowWidth) / ((float)initialRectangle.width);
    float heightRatio = (float)arrowHeight / (float)initialRectangle.height;
    AffineTransform scaling = AffineTransform.getScaleInstance(widthRatio, heightRatio);
    shape.transform(scaling);

    Rectangle rectangle = shape.getBounds();

    int rotation = orientation.rotation;
    if (rotation != 0) {
      shape.transform(AffineTransform.getRotateInstance(2 * Math.PI * rotation / 360, rectangle.width / 2, rectangle.height / 2));
    }

    Rectangle rotatedRectangle = shape.getBounds();
    shape.transform(getTranslateInstance(x + areaWidth / 2 - rotatedRectangle.width / 2,
                                         y + areaHeight / 2 - rotatedRectangle.height / 2));

    g2.setColor(color);
    g2.fill(shape);
  }

  private GeneralPath createTriangleShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 0);
    shape.lineTo(10, 5);
    shape.lineTo(0, 10);
    shape.closePath();
    return shape;
  }

  public int getIconWidth() {
    return iconWidth;
  }

  public int getIconHeight() {
    return iconHeight;
  }

  public int getArrowWidth() {
    return arrowWidth;
  }

  public int getArrowHeight() {
    return arrowHeight;
  }

  public Orientation getOrientation() {
    return orientation;
  }
}
