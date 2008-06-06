package org.designup.picsou.gui.components;

import org.crossbowlabs.splits.utils.Java2DUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class JRoundedPanel extends JPanel {
  private Color topColor = new Color(175, 165, 225);
  private Color bottomColor = new Color(10, 0, 40);
  private Color borderColor = new Color(20, 0, 60);
  private Color shadowColor = new Color(100, 100, 100);
  private Color topTitleColor = new Color(90, 90, 90);
  private Color topMidTitleColor = new Color(0, 0, 0);
  private Color bottomMidTitleColor = new Color(0, 0, 0);
  private Color bottomTitleColor = new Color(70, 70, 70);
  private int arc;
  private int shadowWidth;
  private int distance;
  private float opacity;
  private String title;
  private Font titleFont;
  private int bottomTitle;
  private java.util.List components = new ArrayList();

  public JRoundedPanel() {
    this(20, 3, 2, 0.5f);
  }

  public JRoundedPanel(int arc, int shadowWidth, int distance, float opacity) {
    setOpaque(false);
    this.arc = arc;
    this.shadowWidth = shadowWidth;
    this.distance = distance;
    this.opacity = opacity;
  }

  public void setTitle(String title) {
    setTitle(title, new Font("Arial", Font.BOLD, 24));
  }

  public void setTitle(String title, Font font) {
    this.title = title;
    this.titleFont = font;
  }

  protected void addImpl(Component component, Object object, int i) {
    super.addImpl(component, object, i);
    components.add(component);
  }

  protected void paintComponent(Graphics graphics) {
    Graphics2D g2d = (Graphics2D) graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets vInsets = getInsets();

    int width = getWidth() - (vInsets.left + vInsets.right);
    int height = getHeight() - (vInsets.top + vInsets.bottom);

    int x = vInsets.left;
    int y = vInsets.top;

    int shadowX = x + distance;
    int shadowY = y + distance;

    Java2DUtils.fillShadow(g2d, Color.BLACK, opacity, shadowWidth, arc, shadowX, shadowY,
                           width - (distance * 2), height - (distance * 2));

    int rectX = (distance == 0) ? x + shadowWidth : x;
    int rectY = (distance == 0) ? y + shadowWidth : y;
    int rectWidth = (distance == 0) ? width - (shadowWidth * 2) : width - (distance * 2);
    int rectHeight = (distance == 0) ? height - (shadowWidth * 2) : height - (distance * 2);

    g2d.setColor(borderColor);
    g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, arc, arc);

    GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor);
    g2d.setPaint(gradient);

    if (title == null) {
      g2d.fillRoundRect(rectX + 2, rectY + 2, rectWidth - 4, rectHeight - 4, arc - 2, arc - 2);
    }
    else {
      g2d.fillRoundRect(rectX + 1, rectY + 5, rectWidth - 2, rectHeight - 5 - 1, arc - 2, arc - 2);
      drawTitle(g2d, rectX + 1, rectY + 1, rectWidth - 2);
      setInsideComponentsLocation();
    }
  }

  private void setInsideComponentsLocation() {
    for (Iterator iterator = components.iterator(); iterator.hasNext();) {
      Component component = (Component) iterator.next();
      component.setLocation(new Point(component.getX(), bottomTitle + 5));
    }
  }

  private void drawTitle(Graphics2D g2d, int x, int y, int width) {
    g2d.setFont(titleFont);
    FontMetrics metrics = g2d.getFontMetrics();
    int fontHeight = (metrics.getMaxAscent() - metrics.getMaxDescent());

    int titleX = x + 10;
    int titleY = y + 15 + fontHeight;
    bottomTitle = titleY + 15;
    int titleHeight = bottomTitle - y;

    g2d.setPaint(new GradientPaint(x, y, topTitleColor, x, y + (titleHeight / 2), topMidTitleColor));
    g2d.fillRoundRect(x, y, width, titleHeight - 1, arc, arc);
    g2d.setPaint(new GradientPaint(x, y + (titleHeight / 2), bottomMidTitleColor, x, bottomTitle, bottomTitleColor));
    g2d.fillRect(x, y + (titleHeight / 2) + 1, width, titleHeight / 2);

    g2d.setColor(shadowColor);
    g2d.drawString(title, titleX + 1, titleY + 1);

    g2d.setColor(Color.WHITE);
    g2d.drawString(title, titleX, titleY);

    g2d.setColor(borderColor);
    g2d.drawLine(x, bottomTitle, x + width, bottomTitle);

    g2d.setColor(shadowColor);
    g2d.drawLine(x, bottomTitle + 1, x + width, bottomTitle + 1);
  }
}