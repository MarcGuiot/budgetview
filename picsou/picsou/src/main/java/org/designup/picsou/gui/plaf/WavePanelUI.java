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
  private int width;
  private int height;
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
    this.image = null;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
    this.image = null;
  }

  public void setWaveColor(Color waveColor) {
    this.waveColor = waveColor;
    this.image = null;
  }

  public void paint(Graphics g, JComponent component) {
    component.setOpaque(false);
    Dimension dimension = component.getSize();
    if (height != dimension.height || width != dimension.width || image == null) {
      createImage(component, dimension);
    }
    g.drawImage(image, 0, 0, null);
  }

  private void createImage(JComponent component, Dimension dimension) {
    width = dimension.width;
    height = dimension.height;

    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();

    Rectangle rect = new Rectangle(0, 0, width, height);
    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, height, bottomColor, true);
    drawGradient(g2, rect, gradient);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WAVE_ALPHA));

    AffineTransform transform = new AffineTransform();
    transform.setToScale(width * 0.9, height * 0.3);
    Shape transformedShape = path.createTransformedShape(transform);
    drawGradient(g2, transformedShape, new GradientPaint(0, 0, waveColor, 0, height * 0.3f, bottomColor, true));

    transform.setToScale(width * 0.3, height * 2.5);
    transformedShape = path.createTransformedShape(transform);
    drawGradient(g2, transformedShape, new GradientPaint(0, 0, waveColor, 0, height * 2.5f, bottomColor, true));
  }

  private void drawGradient(Graphics2D g2d, Shape rect, GradientPaint gradient) {
    g2d.setPaint(gradient);
    g2d.fill(rect);
  }
}