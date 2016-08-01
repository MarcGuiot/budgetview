package com.budgetview.gui.components.ui;

import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public abstract class SlidingToggleUI extends BasicToggleButtonUI {

  private Color unselectedBgColor = Colors.toColor("F0F0F0");
  private Color selectedBgColor = Color.WHITE;
  private Color bgBorderColor = Colors.toColor("808080");
  private Color selectedBorderColor = Color.BLACK;

  private Color selectedTextColor = Color.BLACK;
  private Color rolloverTextColor = Color.RED;
  private Color unselectedTextColor = Color.GRAY;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorder(null);
    button.setBorderPainted(false);
    button.setText("");
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void paint(Graphics g, JComponent c) {
    JToggleButton button = (JToggleButton)c;
    button.setOpaque(false);

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = button.getSize();

    int width = size.width - 1;
    int height = size.height - 1;
    if (GuiUtils.isMacOSX()) {
      height -= 1;
    }

    GeneralPath shape = createBackgroundShape();
    Rectangle initialRectangle = shape.getBounds();

    AffineTransform scaling =
      AffineTransform.getScaleInstance(width / (float)initialRectangle.width,
                                       height / (float)initialRectangle.height);
    shape.transform(scaling);

    Rectangle newRectangle = shape.getBounds();
    shape.transform(getTranslateInstance(-newRectangle.x, -newRectangle.y));

    g2.setStroke(new BasicStroke(1.0f));
    boolean selected = button.isSelected();

    g2.setColor(unselectedBgColor);
    g2.fill(shape);
    g2.setColor(bgBorderColor);
    g2.draw(shape);

    int xOffset = selected ? 0 : width - (width / 2);
    g2.setColor(selectedBgColor);
    g2.fillOval(xOffset, 0, width / 2, height);
    g2.setColor(selectedBorderColor);
    g2.drawOval(xOffset, 0, width / 2, height);

    g2.setColor(selected ? selectedTextColor : unselectedTextColor);
    drawSelectedSign(g2, 0, width / 2, height);

    g2.setColor(selected ? unselectedTextColor : selectedTextColor);
    drawUnselectedSign(g2, width - (width / 2), width / 2, height);

    g2.dispose();
  }

  protected abstract void drawSelectedSign(Graphics2D g2, int xOffset, int width, int height);

  protected abstract void drawUnselectedSign(Graphics2D g2, int xOffset, int width, int height);

  private GeneralPath createBackgroundShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 5);
    shape.quadTo(0, 0, 5, 0);
    shape.lineTo(15, 0);
    shape.quadTo(20, 0, 20, 5);
    shape.quadTo(20, 10, 15, 10);
    shape.lineTo(5, 10);
    shape.quadTo(0, 10, 0, 5);
    shape.closePath();
    return shape;
  }

  public void setSelectedBgColor(Color fgColor) {
    this.selectedBgColor = fgColor;
  }

  public void setSelectedBorderColor(Color fgBorderColor) {
    this.selectedBorderColor = fgBorderColor;
  }

  public void setSelectedTextColor(Color selectedTextColor) {
    this.selectedTextColor = selectedTextColor;
  }

  public void setUnselectedBgColor(Color bgColor) {
    this.unselectedBgColor = bgColor;
  }

  public void setUnselectedBorderColor(Color bgBorderColor) {
    this.bgBorderColor = bgBorderColor;
  }

  public void setUnselectedTextColor(Color unselectedTextColor) {
    this.unselectedTextColor = unselectedTextColor;
  }

  public void setRolloverTextColor(Color rolloverTextColor) {
    this.rolloverTextColor = rolloverTextColor;
  }
}
