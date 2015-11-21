package org.globsframework.gui.splits.icons;

import org.globsframework.gui.splits.utils.Java2DUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Arrays;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class ArrowIcon extends SingleColorIcon {

  public enum Orientation {
    UP, DOWN, LEFT, RIGHT;

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

  public ArrowIcon(int iconWidth, int iconHeight, Orientation orientation) {
    this(iconWidth, iconHeight, iconWidth, iconHeight, orientation);
  }

  public ArrowIcon(int iconWidth, int iconHeight, Orientation orientation, Color foreground) {
    this(iconWidth, iconHeight, iconWidth, iconHeight, orientation);
    setColor(foreground);
  }

  public ArrowIcon(int iconWidth, int iconHeight, int arrowWidth, int arrowHeight, Orientation orientation) {
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.arrowWidth = arrowWidth;
    this.arrowHeight = arrowHeight;
    this.orientation = orientation;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {

    Graphics2D g2 = (Graphics2D) graphics;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    GeneralPath shape = createTriangleShape(orientation);

    Java2DUtils.resize(shape, arrowWidth, arrowHeight);

    Rectangle bounds = shape.getBounds();
    int areaWidth = iconWidth - 1;
    int areaHeight = iconHeight - 1;
    shape.transform(getTranslateInstance(x + areaWidth / 2 - bounds.width / 2,
                                         y + areaHeight / 2 - bounds.height / 2));

    g2.setColor(getColor());
    g2.fill(shape);
  }

  private GeneralPath createTriangleShape(Orientation orientation) {
    GeneralPath shape = new GeneralPath();
    switch (orientation) {
      case UP:
        shape.moveTo(5, 0);
        shape.lineTo(10, 10);
        shape.lineTo(0, 10);
        break;
      case DOWN:
        shape.moveTo(0, 0);
        shape.lineTo(10, 0);
        shape.lineTo(5, 10);
        break;
      case LEFT:
        shape.moveTo(10, 0);
        shape.lineTo(0, 5);
        shape.lineTo(10, 10);
        break;
      case RIGHT:
        shape.moveTo(0, 0);
        shape.lineTo(10, 5);
        shape.lineTo(0, 10);
        break;
    }
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

  public String toString() {
    return "ArrowIcon(" + orientation + ", " + iconWidth + ", " + iconHeight + ", " + arrowWidth + ", " + arrowHeight + ")";
  }
}
