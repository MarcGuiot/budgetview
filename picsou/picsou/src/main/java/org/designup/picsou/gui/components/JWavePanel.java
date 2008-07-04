package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class JWavePanel extends JPanel implements ColorChangeListener {

  private Color topColor;
  private Color bottomColor;
  private Color waveColor;
  private static float WAVE_ALPHA = 0.2f;
  protected GeneralPath path;

  public JWavePanel(ColorService colorService) {
    colorService.addListener(this);

    path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    path.moveTo(0.0f, 0.0f);
    path.lineTo(1, 0.0f);
    path.curveTo(0.2f, 0.1f, 0.3f, 0.8f, 0, 1);
    path.closePath();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    topColor = colorLocator.get(PicsouColors.WAVE_PANEL_TOP);
    bottomColor = colorLocator.get(PicsouColors.WAVE_PANEL_BOTTOM);
    waveColor = colorLocator.get(PicsouColors.WAVE_PANEL_WAVE);
  }

  protected void paintComponent(Graphics g) {
    setOpaque(false);
    Dimension d = getSize();
    int w = d.width;
    int h = d.height;

    Graphics2D g2 = (Graphics2D)g.create();
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Rectangle rect = new Rectangle(0, 0, w, h);
    JideFastGradientPainter.drawGradient(g2, rect, topColor, bottomColor, true);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WAVE_ALPHA));

    AffineTransform transform = new AffineTransform();
    transform.setToScale(w * 0.9, h * 0.3);
    g2.transform(transform);
    JideFastGradientPainter.drawGradient(g2, path, waveColor, bottomColor, true);

    transform.setToScale(0.3, 2.5);
    g2.transform(transform);
    JideFastGradientPainter.drawGradient(g2, path, waveColor, bottomColor, true);
  }
}
