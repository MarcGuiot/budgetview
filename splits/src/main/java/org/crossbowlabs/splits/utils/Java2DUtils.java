package org.crossbowlabs.splits.utils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Java2DUtils {

  private Java2DUtils() {
  }

  public static void drawBorder(Graphics2D g2d, Color borderColor, int x, int y, int width, int height) {
    g2d.setColor(borderColor);
    g2d.drawRect(0, 0, width, height);
  }

  public static void drawShadowedString(Graphics2D g2d, String label, Color textColor, Color shadowColor, int x, int y) {
    g2d.setColor(shadowColor);
    g2d.drawString(label, x + 1, y + 1);
    g2d.setColor(textColor);
    g2d.drawString(label, x, y);
  }

  public static void fillShadow(Graphics2D g2d, Color topColor, float opacity, int shadowWidth, int cornerRadius,
                                int x, int y, int width, int height) {
    if (shadowWidth == 0) {
      return;
    }
    g2d.setColor(new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), ((int)(opacity * 255)) & 0xFF));
    float alpha = 0.1f;
    float step = (float)(0.9 / shadowWidth);
    for (int i = 1; i <= shadowWidth; i++) {
      if (alpha >= 0.9f) {
        alpha = 1f;
      }
      else {
        alpha += step;
      }
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      RoundRectangle2D shadowShape = new RoundRectangle2D.Float(x + i, y + i, width - (i * 2),
                                                                height - (i * 2), cornerRadius, cornerRadius);
      g2d.fill(shadowShape);
    }
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }
}
