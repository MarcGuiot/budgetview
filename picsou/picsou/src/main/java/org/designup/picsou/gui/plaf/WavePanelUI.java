package org.designup.picsou.gui.plaf;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class WavePanelUI extends BasicPanelUI {

  private Color topColor;
  private Color bottomColor;
  private Color waveColor;
  private static float WAVE_ALPHA = 0.2f;
  protected GeneralPath path;
  private int w;
  private int h;
  private BufferedImage image;

  public WavePanelUI() {
    path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    path.moveTo(0.0f, 0.0f);
    path.lineTo(1, 0.0f);
    path.curveTo(0.2f, 0.1f, 0.3f, 0.8f, 0, 1);
    path.closePath();
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public void setWaveColor(Color waveColor) {
    this.waveColor = waveColor;
  }

  public void paint(Graphics g, JComponent c) {
    c.setOpaque(false);
    Dimension d = c.getSize();
    if (h != d.height || w != d.width) {
      w = d.width;
      h = d.height;

      image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_USHORT_565_RGB); //TYPE_INT_RGB);
      Graphics2D g2 = image.createGraphics();

      Rectangle rect = new Rectangle(0, 0, w, h);
      GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, h, bottomColor, true);
      drawGradient(g2, rect, gradient);

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WAVE_ALPHA));

      AffineTransform transform = new AffineTransform();
      transform.setToScale(w * 0.9, h * 0.3);
      Shape transformedShape = path.createTransformedShape(transform);
      drawGradient(g2, transformedShape, new GradientPaint(0, 0, waveColor, 0, h * 0.3f, bottomColor, true));

      transform.setToScale(w * 0.3, h * 2.5);
      transformedShape = path.createTransformedShape(transform);
      drawGradient(g2, transformedShape, new GradientPaint(0, 0, waveColor, 0, h * 2.5f, bottomColor, true));
    }
    g.drawImage(image, 0, 0, null);
  }

  private void drawGradient(Graphics2D g2d, Shape rect, GradientPaint gradient) {
    g2d.setPaint(gradient);
    g2d.fill(rect);
  }
}