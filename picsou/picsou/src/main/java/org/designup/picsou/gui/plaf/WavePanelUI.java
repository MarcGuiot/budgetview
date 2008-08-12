package org.designup.picsou.gui.plaf;

import org.designup.picsou.gui.components.jide.JideFastGradientPainter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class WavePanelUI extends BasicPanelUI {

  private Color topColor;
  private Color bottomColor;
  private Color waveColor;
  private static float WAVE_ALPHA = 0.2f;
  protected GeneralPath path;

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
    int w = d.width;
    int h = d.height;

    Graphics2D g2 = (Graphics2D)g.create();

    Rectangle rect = new Rectangle(0, 0, w, h);
    JideFastGradientPainter.drawGradient(g2, rect, topColor, bottomColor, true);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WAVE_ALPHA));

    AffineTransform transform = new AffineTransform();
    transform.setToScale(w * 0.9, h * 0.3);
    Shape transformedShape = path.createTransformedShape(transform);
    JideFastGradientPainter.drawGradient(g2, transformedShape, waveColor, bottomColor, true);

    transform.setToScale(w * 0.3, h * 2.5);
    transformedShape = path.createTransformedShape(transform);
    JideFastGradientPainter.drawGradient(g2, transformedShape, waveColor, bottomColor, true);
  }
}