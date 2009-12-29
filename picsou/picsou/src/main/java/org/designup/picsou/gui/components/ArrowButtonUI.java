package org.designup.picsou.gui.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import java.awt.geom.GeneralPath;

public class ArrowButtonUI extends BasicButtonUI {

  private boolean doubleArrow = false;
  private int rotation = 0;
  private Dimension shapeSize;

  private Color pressedColor = Color.GRAY;
  private Color rolloverColor = Color.BLUE;
  private Color disabledColor = Color.DARK_GRAY;

  public void installUI(JComponent c) {
    super.installUI(c);
    JButton button = (JButton)c;
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setBorder(null);
    button.setRolloverEnabled(true);
    if (shapeSize == null) {
      shapeSize = button.getPreferredSize();
    }
  }

  public void paint(Graphics g, JComponent button) {
    super.paint(g, button);

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    initColor(g2, button);

    int shapeHeight = shapeSize.height;

    int areaWidth = button.getWidth() - 1;
    int areaHeight = button.getHeight() - 1;

    GeneralPath shape = createTriangleShape();
    Rectangle initialRectangle = shape.getBounds();
    float ratio = shapeHeight / (float)initialRectangle.height;
    AffineTransform scaling = AffineTransform.getScaleInstance(ratio, ratio);
    shape.transform(scaling);

    Rectangle rectangle = shape.getBounds();
    float middleY = (float)areaHeight / 2;

    if (rotation != 0) {
      shape.transform(AffineTransform.getRotateInstance(Math.PI, rectangle.width / 2, rectangle.height / 2));
    }

    if (doubleArrow) {
      shape.transform(getTranslateInstance((areaWidth - rectangle.width) / 2 - rectangle.width / 2,
                                           middleY - rectangle.height / 2 - initialRectangle.y));

      g2.fill(shape);
      shape.transform(getTranslateInstance(rectangle.width, 0));
      g2.fill(shape);
    }
    else {
      shape.transform(getTranslateInstance((areaWidth - rectangle.width) / 2,
                                           middleY - rectangle.height / 2 - initialRectangle.y));
      g2.fill(shape);
    }
  }

  private void initColor(Graphics2D g2, JComponent c) {
    JButton button = (JButton)c;
    ButtonModel model = button.getModel();
    if (!model.isEnabled()) {
      g2.setColor(disabledColor);
    }
    else if (model.isPressed()) {
      g2.setColor(pressedColor);
    }
    else if (model.isRollover()) {
      g2.setColor(rolloverColor);
    }
    else {
      g2.setColor(c.getForeground());
    }
  }

  private GeneralPath createTriangleShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 0);
    shape.lineTo(10, 5);
    shape.lineTo(0, 10);
    shape.closePath();
    return shape;
  }

  public void setPressedColor(Color pressedColor) {
    this.pressedColor = pressedColor;
  }

  public void setRolloverColor(Color rolloverColor) {
    this.rolloverColor = rolloverColor;
  }

  public void setDisabledColor(Color disabledColor) {
    this.disabledColor = disabledColor;
  }

  public void setDoubleArrow(boolean doubleArrow) {
    this.doubleArrow = doubleArrow;
  }

  public void setRotation(int degrees) {
    this.rotation = degrees;
  }

  public void setSize(Dimension size) {
    this.shapeSize = size;
  }
}
