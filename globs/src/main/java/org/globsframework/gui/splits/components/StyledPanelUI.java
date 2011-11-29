package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

public class StyledPanelUI extends BasicPanelUI {
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color borderColor = Color.BLACK;
  private Color bevelTopColor;

  private int borderWidth = 0;

  private int cornerRadius = 0;
  private int shadowWidth = 0;
  private int distance = 0;
  private float opacity = 0.5f;
  private Image image;
  private int height;
  private int width;

  public StyledPanelUI() {
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
    this.image = null;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
    this.image = null;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
    this.image = null;
  }

  public void setBevelTopColor(Color bevelTopColor) {
    this.bevelTopColor = bevelTopColor;
    this.image = null;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
    this.image = null;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
    this.image = null;
  }

  public void setShadowWidth(int shadowWidth) {
    this.shadowWidth = shadowWidth;
    this.image = null;
  }

  public void setDistance(int distance) {
    this.distance = distance;
    this.image = null;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
    this.image = null;
  }

  public void paint(Graphics graphics, JComponent component) {
    component.setOpaque(false);
    if (height != component.getHeight() || width != component.getWidth() || image == null) {
      createImage(graphics, component);
    }
    if (image != null) {
      do{
        graphics.drawImage(image, 0, 0, null);
      } while (image != null && image instanceof VolatileImage && ((VolatileImage)image).contentsLost());
    }
  }

  private void createImage(Graphics graphics, JComponent c) {
    height = c.getHeight();
    width = c.getWidth();
    Graphics2D g2d;
    try {
      int type;
      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice device = env.getDefaultScreenDevice();
      GraphicsConfiguration config = device.getDefaultConfiguration();
      BufferedImage bufferedImage = config.createCompatibleImage(width, height, Transparency.BITMASK);
      g2d = bufferedImage.createGraphics();
      image = bufferedImage;
//      if (cornerRadius != 0) {
//        type = BufferedImage.TYPE_INT_ARGB;
//        BufferedImage bufferedImage = new BufferedImage(width, height, type);
//        image = bufferedImage;
//        g2d = bufferedImage.createGraphics();
//      }
//      else {
//        VolatileImage volatileImage = c.createVolatileImage(c.getWidth(), c.getHeight());
//        image = volatileImage;
//        g2d = volatileImage.createGraphics();
//      }
    }
    catch (OutOfMemoryError e) {
      image = null;
      g2d = (Graphics2D)graphics;
    }
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets vInsets = c.getInsets();

    int width = c.getWidth() - (vInsets.left + vInsets.right);
    int height = c.getHeight() - (vInsets.top + vInsets.bottom);

    int x = vInsets.left;
    int y = vInsets.top;

    Java2DUtils.fillShadow(g2d, Color.BLACK, opacity, shadowWidth, cornerRadius,
                           x + distance, y + distance, width - (distance * 2), height - (distance * 2));

    int rectWidth = width - (distance * 2);
    int rectHeight = height - (distance * 2);
    if (borderWidth > 0) {
      g2d.setColor(borderColor);
      g2d.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);
    }

    int innerWidth = rectWidth - 2 * borderWidth;
    int innerHeight = rectHeight - 2 * borderWidth;

    if (topColor.equals(bottomColor)) {
      g2d.setPaint(topColor);
    }
    else {
      GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor, true);
      g2d.setPaint(gradient);
    }

    if (cornerRadius > 0) {
      int widthRadius = Math.max(0, cornerRadius - borderWidth);
      int heightRadius = Math.max(0, cornerRadius - borderWidth);
      g2d.fillRoundRect(x + borderWidth, y + borderWidth, innerWidth, innerHeight, widthRadius, heightRadius);

      if (bevelTopColor != null) {
        paintBevel(x, y, innerHeight, g2d, innerWidth, rectHeight, widthRadius, heightRadius);
      }
    }
    else {
      g2d.fillRect(x + borderWidth, y + borderWidth, innerWidth, innerHeight);
    }
    if (image != null) {
      g2d.dispose();
    }
  }

  private void paintBevel(int x, int y, int innerHeight, Graphics2D g2d, int innerWidth, int rectHeight, int widthRadius, int heightRadius) {
    Color smoothColor = new Color(bevelTopColor.getRGBColorComponents(null)[0],
                                  bevelTopColor.getRGBColorComponents(null)[1],
                                  bevelTopColor.getRGBColorComponents(null)[2],
                                  .0f);

    GradientPaint vPaint = new GradientPaint(x + borderWidth,
                                             y + borderWidth,
                                             bevelTopColor,
                                             x + borderWidth,
                                             y + innerHeight / 2,
                                             smoothColor);
    g2d.setPaint(vPaint);

    g2d.clipRect(x + borderWidth, y + borderWidth, innerWidth, innerHeight / 2);
    g2d.drawRoundRect(x + borderWidth, y + borderWidth, innerWidth - 1, rectHeight - 1, widthRadius, heightRadius - 1);
  }
}
