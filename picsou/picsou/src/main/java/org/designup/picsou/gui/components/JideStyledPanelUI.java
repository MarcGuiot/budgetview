package org.designup.picsou.gui.components;

import org.designup.picsou.gui.components.jide.JideFastGradientPainter;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.components.StyledPanelUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JideStyledPanelUI extends BasicPanelUI {
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color borderColor = Color.BLACK;

  private int borderWidth = 0;

  private int cornerRadius = 0;
  private int distance = 0;

  public JideStyledPanelUI() {
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public Color getTopColor() {
    return topColor;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public Color getBottomColor() {
    return bottomColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public int getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public int getCornerRadius() {
    return cornerRadius;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
  }

  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  public void paint(Graphics graphics, JComponent c) {
    c.setOpaque(false);
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets vInsets = c.getInsets();

    int width = c.getWidth() - (vInsets.left + vInsets.right);
    int height = c.getHeight() - (vInsets.top + vInsets.bottom);

    int x = vInsets.left;
    int y = vInsets.top;

    int rectWidth = width - (distance * 2);
    int rectHeight = height - (distance * 2);
    if (borderWidth > 0) {
      g2d.setColor(borderColor);
      g2d.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);
    }

    int innerWidth = rectWidth - 2 * borderWidth;
    int innerHeight = rectHeight - 2 * borderWidth;
    int widthRadius = Math.max(0, cornerRadius - borderWidth);
    int heightRadius = Math.max(0, cornerRadius - borderWidth);

    if (topColor.equals(bottomColor)) {
      g2d.setColor(topColor);
      g2d.fillRoundRect(x + borderWidth, y + borderWidth,
                        innerWidth, innerHeight,
                        widthRadius, heightRadius);
    }
    else {

      RoundRectangle2D rect = new RoundRectangle2D.Double(x + borderWidth, y + borderWidth,
                                                          innerWidth, innerHeight,
                                                          widthRadius, heightRadius);
      JideFastGradientPainter.drawGradient(g2d, rect, topColor, bottomColor, true);
    }
  }

  public static void main(String[] args) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
    panel.setPreferredSize(new Dimension(600,500));
//    StyledPanelUI ui = new StyledPanelUI();
    JideStyledPanelUI ui = new JideStyledPanelUI();
    ui.setBorderColor(Color.BLACK);
    ui.setBorderWidth(2);
    ui.setBottomColor(Color.YELLOW);
    ui.setTopColor(Color.RED);
    ui.setCornerRadius(20);
    panel.setUI(ui);
    GuiUtils.show(panel);
  }
}
