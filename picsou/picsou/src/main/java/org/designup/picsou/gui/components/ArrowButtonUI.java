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
  private Color pressedColor = Color.GRAY;
  private Color rolloverColor = Color.BLUE;
  private Color disabledColor = Color.DARK_GRAY;

  public void installUI(JComponent c) {
    super.installUI(c);
    JButton button = (JButton)c;
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setRolloverEnabled(true);
  }

  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);

    GeneralPath shape = createTriangleShape();

    int width = c.getWidth() - 1;
    int height = c.getHeight() - 1;

    Rectangle initialRectangle = shape.getBounds();
    float ratio = height / (float)initialRectangle.height;
    AffineTransform scaling =
      AffineTransform.getScaleInstance(ratio, ratio);
    shape.transform(scaling);

    Rectangle rectangle = shape.getBounds();
    float middleY = (float)height / 2;

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

    if (rotation != 0)
    shape.transform(AffineTransform.getRotateInstance(Math.PI, rectangle.width / 2, rectangle.height / 2));

    if (doubleArrow) {
      shape.transform(getTranslateInstance((width - rectangle.width) / 2 - rectangle.width / 2,
                                           middleY - height / 2 - initialRectangle.y));

      g2.fill(shape);
      shape.transform(getTranslateInstance(rectangle.width, 0));
      g2.fill(shape);
    }
    else {
      shape.transform(getTranslateInstance((width - rectangle.width) / 2,
                                           middleY - height / 2 - initialRectangle.y));
      g2.fill(shape);
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
}
